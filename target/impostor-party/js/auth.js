// FILE: src/main/webapp/js/auth.js
const tabLogin = document.getElementById('tabLogin');
const tabRegister = document.getElementById('tabRegister');
const authForm = document.getElementById('authForm');
const submitBtn = document.getElementById('submitBtn');
const errorMsg = document.getElementById('errorMsg');

let mode = 'login'; // 'login' o 'register'

tabLogin.addEventListener('click', () => switchMode('login'));
tabRegister.addEventListener('click', () => switchMode('register'));

function switchMode(newMode) {
    mode = newMode;
    tabLogin.classList.toggle('active', mode === 'login');
    tabRegister.classList.toggle('active', mode === 'register');
    submitBtn.textContent = mode === 'login' ? 'Entrar' : 'Crear cuenta';
    errorMsg.textContent = '';
}

authForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorMsg.textContent = '';

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();

    const endpoint = mode === 'login' ? '/api/login' : '/api/register';

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (!response.ok) {
            errorMsg.textContent = data.message || 'Algo ha ido mal';
            return;
        }

        // Login/registro correcto -> vamos a la pantalla de crear/unirse a sala
        window.location.href = 'crear-sala.html';

    } catch (err) {
        errorMsg.textContent = 'No se pudo conectar con el servidor';
    }
});