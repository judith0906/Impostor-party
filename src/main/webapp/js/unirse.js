// FILE: src/main/webapp/js/unirse.js
const joinForm = document.getElementById('joinForm');
const errorMsg = document.getElementById('errorMsg');

joinForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorMsg.textContent = '';

    const code = document.getElementById('code').value.trim().toUpperCase();
    const nickname = document.getElementById('nickname').value.trim();

    try {
        const response = await fetch('/api/rooms/join', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code, nickname })
        });

        const data = await response.json();

        if (!response.ok) {
            errorMsg.textContent = data.message || 'No se pudo unir a la sala';
            return;
        }

        localStorage.setItem('sessionToken', data.sessionToken);
        localStorage.setItem('nickname', nickname);
        window.location.href = `sala.html?code=${data.code}`;

    } catch (err) {
        errorMsg.textContent = 'No se pudo conectar con el servidor';
    }
});