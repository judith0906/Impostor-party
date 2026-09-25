// FILE: src/main/webapp/js/fin.js
const params = new URLSearchParams(window.location.search);
const roomCode = params.get('code');
const sessionToken = sessionStorage.getItem('sessionToken');
const MAX_RESULT_RETRIES = 30;
let resultRetries = 0;

const impostorNames = document.getElementById('impostorNames');
const impostorLabel = document.getElementById('impostorLabel');
const wordsSummary = document.getElementById('wordsSummary');
const impostorWords = document.getElementById('impostorWords');

function addWordRow(name, word) {
    const li = document.createElement('li');
    const nameSpan = document.createElement('span');
    const wordSpan = document.createElement('span');

    nameSpan.className = 'impostor-word-name';
    nameSpan.textContent = name;

    wordSpan.className = 'impostor-word-value';
    wordSpan.textContent = word;

    li.appendChild(nameSpan);
    li.appendChild(wordSpan);
    impostorWords.appendChild(li);
}

function renderImpostorWords(data) {
    impostorWords.textContent = '';
    const details = Array.isArray(data.impostorDetails) ? data.impostorDetails : [];

    if (details.length > 0) {
        details.forEach(detail => {
            const nickname = String(detail.nickname || '');
            const word = detail.word ? '"' + detail.word + '"' : 'palabra no revelada';
            addWordRow(nickname, word);
        });
        return;
    }

    if (data.impostorWord) {
        addWordRow('Palabra de los impostores', '"' + data.impostorWord + '"');
    }
}

function renderNormalWord(normalWord) {
    if (normalWord) {
        wordsSummary.textContent = 'Palabra de los jugadores: "' + normalWord + '"';
    } else {
        wordsSummary.textContent = 'No se revelo la palabra de los jugadores';
    }
}

async function loadResult() {
    if (!roomCode) {
        impostorNames.textContent = 'No se encontro la sala';
        return;
    }
    if (!sessionToken) {
        impostorNames.textContent = 'Tu sesion de jugador no esta disponible';
        return;
    }

    try {
        const response = await fetch(`/api/rooms/result?code=${encodeURIComponent(roomCode)}`, {
            headers: { 'X-Player-Token': sessionToken || '' },
            cache: 'no-store'
        });
        const data = await response.json();

        if (!response.ok) {
            if (response.status === 409) {
                if (resultRetries < MAX_RESULT_RETRIES) {
                    resultRetries += 1;
                    window.setTimeout(loadResult, 1000);
                    return;
                }
                impostorNames.textContent = (data && data.message)
                    || 'La partida aun no tiene un resultado disponible';
                return;
            }
            impostorNames.textContent = (data && data.message) || 'Error al cargar el resultado';
            return;
        }

        resultRetries = 0;
        const impostors = Array.isArray(data.impostors) ? data.impostors : [];

        if (impostors.length > 1) {
            impostorLabel.textContent = 'Los impostores eran';
        } else if (impostors.length === 1) {
            impostorLabel.textContent = 'El impostor era';
        } else {
            impostorLabel.textContent = 'No habia impostores';
        }

        impostorNames.textContent = impostors.join(', ');
        renderNormalWord(data.normalWord);
        renderImpostorWords(data);

    } catch (err) {
        impostorNames.textContent = 'No se pudo conectar con el servidor';
    }
}

loadResult();
