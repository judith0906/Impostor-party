// FILE: src/main/webapp/js/sala.js
const params = new URLSearchParams(window.location.search);
const roomCode = params.get('code');
const sessionToken = sessionStorage.getItem('sessionToken');

document.getElementById('roomCodeDisplay').textContent = roomCode || '------';

const lobbyControls = document.getElementById('lobbyControls');
const hostControls = document.getElementById('hostControls');
const playerList = document.getElementById('playerList');
const errorMsg = document.getElementById('errorMsg');
const startGameBtn = document.getElementById('startGameBtn');
const readyBtn = document.getElementById('readyBtn');

const configPanel = document.getElementById('configPanel');
const configImpostors = document.getElementById('configImpostors');
const configChallenge = document.getElementById('configChallenge');
const configDuration = document.getElementById('configDuration');
const configDurationLabel = document.getElementById('configDurationLabel');
const configStatus = document.getElementById('configStatus');
const saveConfigBtn = document.getElementById('saveConfigBtn');

const MIN_IMPOSTORS = 1;
const MAX_IMPOSTORS = 3;
const MIN_HOURS = 1;
const MAX_HOURS = 12;
const POLL_INTERVAL = 2500;

let isHost = false;
let myReady = false;
let readyPending = false;
let configDirty = false;
let savingConfig = false;
let startingGame = false;
let gameStarted = false;
let polling = false;
let stateRevision = 0;

const roomConfig = {
    numImpostors: 1,
    challengeType: 'normales',
    durationHours: 3
};

function authHeaders() {
    return sessionToken ? { 'X-Player-Token': sessionToken } : {};
}

async function readJson(response) {
    try {
        return await response.json();
    } catch (err) {
        return null;
    }
}

function hasOption(select, value) {
    return Array.prototype.some.call(select.options, option => option.value === value);
}

function clamp(value, min, max) {
    if (!Number.isFinite(value)) return min;
    return Math.min(max, Math.max(min, value));
}

function readConfigPanel() {
    return {
        numImpostors: parseInt(configImpostors.value, 10) || MIN_IMPOSTORS,
        challengeType: configChallenge.value,
        durationHours: parseInt(configDuration.value, 10) || MIN_HOURS
    };
}

function setConfigStatus(text, state) {
    configStatus.textContent = text;
    configStatus.className = 'config-status' + (state ? ' ' + state : '');
}

function configIsValid() {
    return hasOption(configChallenge, roomConfig.challengeType)
        && configChallenge.value === roomConfig.challengeType;
}

function updateStartButton() {
    startGameBtn.disabled = !isHost || configDirty || !configIsValid()
        || savingConfig || startingGame || gameStarted;
}

function setReady(value) {
    myReady = value === true;
    readyBtn.textContent = myReady ? 'Cancelar' : 'Estoy listo';
}

function applyServerConfig(data) {
    const numImpostors = clamp(parseInt(data.numImpostors, 10), MIN_IMPOSTORS, MAX_IMPOSTORS);
    const durationHours = clamp(parseInt(data.durationHours, 10), MIN_HOURS, MAX_HOURS);
    const challengeType = typeof data.challengeType === 'string' ? data.challengeType : '';

    roomConfig.numImpostors = numImpostors;
    roomConfig.durationHours = durationHours;
    if (challengeType) {
        roomConfig.challengeType = challengeType;
    }

    configImpostors.value = String(numImpostors);
    configDuration.value = String(durationHours);
    configDurationLabel.textContent = String(durationHours);
    if (challengeType && hasOption(configChallenge, challengeType)) {
        configChallenge.value = challengeType;
    }
}

function applyServerState(data, revision) {
    isHost = data.isHost === true;
    const isLobby = data.status === 'LOBBY';

    lobbyControls.style.display = isLobby ? 'block' : 'none';
    hostControls.style.display = isHost && isLobby ? 'block' : 'none';
    configPanel.hidden = !isHost || !isLobby;

    if (revision === stateRevision && isLobby && !readyPending) {
        setReady(data.myReady === true);
    }

    if (revision === stateRevision && isLobby && isHost && !configDirty && !savingConfig) {
        applyServerConfig(data);
    }

    updateStartButton();
}

function markConfigDirty() {
    const panel = readConfigPanel();
    configDurationLabel.textContent = String(panel.durationHours);
    configDirty = panel.numImpostors !== roomConfig.numImpostors
        || panel.challengeType !== roomConfig.challengeType
        || panel.durationHours !== roomConfig.durationHours;

    if (configDirty) {
        setConfigStatus('Cambios sin guardar', 'pending');
    } else if (configStatus.classList.contains('pending')) {
        setConfigStatus('', '');
    }
    updateStartButton();
}

function renderPlayers(players) {
    playerList.textContent = '';
    if (!Array.isArray(players)) return;

    players.forEach(player => {
        const li = document.createElement('li');
        const name = document.createElement('span');
        const status = document.createElement('span');

        name.className = 'player-name';
        name.textContent = player.isHost === true ? player.nickname + ' (host)' : String(player.nickname || '');

        const isReady = player.ready === true;
        status.className = 'player-status ' + (isReady ? 'ready' : 'waiting');
        status.textContent = isReady ? 'Listo' : 'Esperando';

        li.appendChild(name);
        li.appendChild(status);
        playerList.appendChild(li);
    });
}

async function pollStatus() {
    if (polling) return;
    polling = true;
    const requestRevision = stateRevision;

    try {
        const response = await fetch(`/api/rooms/status?code=${encodeURIComponent(roomCode || '')}`, {
            headers: authHeaders(),
            cache: 'no-store'
        });
        const data = await readJson(response);

        if (!response.ok) {
            errorMsg.textContent = (data && data.message) || 'Error al consultar la sala';
            return;
        }

        if (data.status === 'IN_PROGRESS') {
            window.location.href = 'juego.html';
            return;
        }
        if (data.status === 'FINISHED') {
            window.location.href = `fin.html?code=${encodeURIComponent(roomCode || '')}`;
            return;
        }
        if (requestRevision !== stateRevision) return;

        renderPlayers(data.players);
        applyServerState(data, requestRevision);

    } catch (err) {
        errorMsg.textContent = 'Se perdio la conexion con el servidor';
    } finally {
        polling = false;
    }
}

configImpostors.addEventListener('change', markConfigDirty);
configChallenge.addEventListener('change', markConfigDirty);
configDuration.addEventListener('input', markConfigDirty);

saveConfigBtn.addEventListener('click', async () => {
    if (savingConfig) return;

    const pending = readConfigPanel();
    stateRevision += 1;
    savingConfig = true;
    saveConfigBtn.disabled = true;
    setConfigStatus('Guardando...', 'saving');
    updateStartButton();

    try {
        const response = await fetch('/api/rooms/config', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                code: roomCode,
                numImpostors: pending.numImpostors,
                challengeType: pending.challengeType,
                durationHours: pending.durationHours
            })
        });
        const data = await readJson(response);

        if (!response.ok) {
            setConfigStatus((data && data.message) || 'No se pudo guardar la configuracion', 'error');
            return;
        }

        roomConfig.numImpostors = pending.numImpostors;
        roomConfig.challengeType = pending.challengeType;
        roomConfig.durationHours = pending.durationHours;
        configDirty = false;
        setConfigStatus('Cambios guardados', 'ok');
        errorMsg.textContent = '';

    } catch (err) {
        setConfigStatus('No se pudo conectar con el servidor', 'error');
    } finally {
        stateRevision += 1;
        savingConfig = false;
        saveConfigBtn.disabled = false;
        updateStartButton();
    }
});

readyBtn.addEventListener('click', async () => {
    if (readyPending) return;

    const previous = myReady;
    stateRevision += 1;
    readyPending = true;
    readyBtn.disabled = true;
    setReady(!previous);

    try {
        const response = await fetch('/api/rooms/ready', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Player-Token': sessionToken || ''
            },
            body: JSON.stringify({ ready: myReady })
        });
        const data = await readJson(response);

        if (!response.ok) {
            setReady(previous);
            errorMsg.textContent = (data && data.message) || 'No se pudo actualizar tu estado';
            return;
        }

        errorMsg.textContent = '';

    } catch (err) {
        setReady(previous);
        errorMsg.textContent = 'No se pudo conectar con el servidor';
    } finally {
        stateRevision += 1;
        readyPending = false;
        readyBtn.disabled = false;
    }
});

startGameBtn.addEventListener('click', async () => {
    if (startingGame || configDirty || savingConfig) return;

    startingGame = true;
    updateStartButton();
    startGameBtn.textContent = 'Iniciando...';

    try {
        const response = await fetch('/api/rooms/start', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: roomCode })
        });
        const data = await readJson(response);

        if (!response.ok) {
            errorMsg.textContent = (data && data.message) || 'No se pudo iniciar la partida';
            startGameBtn.textContent = 'Iniciar partida';
            return;
        }

        errorMsg.textContent = '';
        gameStarted = true;
        startGameBtn.textContent = 'Partida en curso...';

    } catch (err) {
        errorMsg.textContent = 'No se pudo conectar con el servidor';
        startGameBtn.textContent = 'Iniciar partida';
    } finally {
        startingGame = false;
        updateStartButton();
    }
});

if (!roomCode) {
    errorMsg.textContent = 'Falta el codigo de sala';
} else if (!sessionToken) {
    errorMsg.textContent = 'Tu sesion de jugador no esta disponible, vuelve a unirte a la sala';
} else {
    pollStatus();
    setInterval(pollStatus, POLL_INTERVAL);
}
