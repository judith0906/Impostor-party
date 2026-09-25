// FILE: src/main/webapp/js/fin.js
const params = new URLSearchParams(window.location.search);
const roomCode = params.get('code');

const impostorNames = document.getElementById('impostorNames');
const impostorLabel = document.getElementById('impostorLabel');
const wordsSummary = document.getElementById('wordsSummary');

async function loadResult() {
    if (!roomCode) {
        impostorNames.textContent = 'No se encontro la sala';
        return;
    }

    try {
        const response = await fetch(`/api/rooms/result?code=${roomCode}`);
        const data = await response.json();

        if (!response.ok) {
            impostorNames.textContent = data.message || 'Error al cargar el resultado';
            return;
        }

        impostorLabel.textContent = data.impostors.length > 1 ? 'Los impostores eran' : 'El impostor era';
        impostorNames.textContent = data.impostors.join(', ');
        wordsSummary.textContent = `Palabra de los jugadores: "${data.normalWord}" · Palabra del impostor: "${data.impostorWord}"`;

    } catch (err) {
        impostorNames.textContent = 'No se pudo conectar con el servidor';
    }
}

loadResult();