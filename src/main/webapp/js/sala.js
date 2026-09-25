// FILE: src/main/webapp/js/sala.js
const params = new URLSearchParams(window.location.search);
const roomCode = params.get('code');
const isHost = sessionStorage.getItem('isHost') === 'true';
const sessionToken = localStorage.getItem('sessionToken');

document.getElementById('roomCodeDisplay').textContent = roomCode || '------';

const hostControls = document.getElementById('hostControls');
const playerControls = document.getElementById('playerControls');
const playerList = document.getElementById('playerList');
const errorMsg = document.getElementById('errorMsg');
const startGameBtn = document.getElementById('startGameBtn');
const readyBtn = document.getElementById('readyBtn');

let myReady = false;

if (isHost) {
    hostControls.style.display = 'block';
} else {
    playerControls.style.display = 'block';
}

async function pollStatus() {
    try {
        const response = await fetch(`/api/rooms/status?code=${roomCode}`);
        const data = await response.json();

        if (!response.ok) {
            errorMsg.textContent = data.message || 'Error al consultar la sala';
            return;
        }

        renderPlayers(data.players);

        if (data.status === 'IN_PROGRESS') {
            window.location.href = 'juego.html';
        }

    } catch (err) {
        errorMsg.textContent = 'Se perdio la conexion con el servidor';
    }
}

function renderPlayers(players) {
    playerList.innerHTML = '';
    players.forEach(p => {
        const li = document.createElement('li');
        const statusClass = p.ready ? 'ready' : 'waiting';
        const statusText = p.ready ? 'Listo' : 'Esperando';
        li.innerHTML = `<span>${p.nickname}</span><span class="player-status ${statusClass}">${statusText}</span>`;
        playerList.appendChild(li);
    });
}

readyBtn.addEventListener('click', async () => {
    myReady = !myReady;
    readyBtn.textContent = myReady ? 'Cancelar' : 'Estoy listo';

    try {
        await fetch('/api/rooms/ready', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sessionToken, ready: myReady })
        });
    } catch (err) {
        errorMsg.textContent = 'No se pudo actualizar tu estado';
    }
});

startGameBtn.addEventListener('click', async () => {
    startGameBtn.disabled = true;
    startGameBtn.textContent = 'Iniciando...';

    try {
        const response = await fetch('/api/rooms/start', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: roomCode })
        });

        const data = await response.json();

        if (!response.ok) {
            errorMsg.textContent = data.message || 'No se pudo iniciar la partida';
            startGameBtn.disabled = false;
            startGameBtn.textContent = 'Iniciar partida';
            return;
        }

        errorMsg.textContent = '';
        startGameBtn.textContent = 'Partida en curso...';

    } catch (err) {
        errorMsg.textContent = 'No se pudo conectar con el servidor';
        startGameBtn.disabled = false;
        startGameBtn.textContent = 'Iniciar partida';
    }
});

pollStatus();
setInterval(pollStatus, 2500);