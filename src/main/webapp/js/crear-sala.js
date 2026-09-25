// FILE: src/main/webapp/js/crear-sala.js
const form = document.getElementById('createRoomForm');
const chips = document.querySelectorAll('.chip');
const hoursInput = document.getElementById('durationHours');
const hoursLabel = document.getElementById('hoursLabel');
const errorMsg = document.getElementById('errorMsg');

let selectedChallenge = 'normales';

chips.forEach(chip => {
    chip.addEventListener('click', () => {
        chips.forEach(c => c.classList.remove('active'));
        chip.classList.add('active');
        selectedChallenge = chip.dataset.value;
    });
});

hoursInput.addEventListener('input', () => {
    hoursLabel.textContent = hoursInput.value;
});

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorMsg.textContent = '';

    const numImpostors = parseInt(document.getElementById('numImpostors').value, 10);
    const durationHours = parseInt(hoursInput.value, 10);

    try {
        const response = await fetch('/api/rooms', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ numImpostors, challengeType: selectedChallenge, durationHours })
        });

        const data = await response.json();

        if (!response.ok) {
            errorMsg.textContent = data.message || 'No se pudo crear la sala';
            return;
        }

        if (data.sessionToken) {
            sessionStorage.setItem('sessionToken', data.sessionToken);
        }
        if (data.nickname) {
            sessionStorage.setItem('nickname', data.nickname);
        }
        sessionStorage.setItem('roomCode', data.code);
        sessionStorage.removeItem('isHost');
        window.location.href = `sala.html?code=${data.code}`;

    } catch (err) {
        errorMsg.textContent = 'No se pudo conectar con el servidor';
    }
});