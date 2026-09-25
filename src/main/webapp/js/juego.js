// FILE: src/main/webapp/js/juego.js
const sessionToken = localStorage.getItem('sessionToken');
const nickname = localStorage.getItem('nickname');

const playerNameDisplay = document.getElementById('playerNameDisplay');
const wordDisplay = document.getElementById('wordDisplay');
const taskList = document.getElementById('taskList');
const timerDisplay = document.getElementById('timerDisplay');

playerNameDisplay.textContent = `Jugando como ${nickname || '...'}`;

let endTimeMillis = 0;

async function loadScreen() {
    try {
        const response = await fetch(`/api/player/screen?sessionToken=${sessionToken}`);
        const data = await response.json();

        if (!response.ok) {
            wordDisplay.textContent = data.message || 'Error';
            return;
        }

        wordDisplay.textContent = data.word;
        localStorage.setItem('roomCode', data.roomCode);
        endTimeMillis = data.endTime;
        renderTasks(data.tasks);

    } catch (err) {
        wordDisplay.textContent = 'Sin conexion';
    }
}

function renderTasks(tasks) {
    taskList.innerHTML = '';
    tasks.forEach(task => {
        const li = document.createElement('li');
        li.className = 'task-item' + (task.completed ? ' completed' : '');
        li.innerHTML = `
            <div class="task-checkbox ${task.completed ? 'checked' : ''}">${task.completed ? '✓' : ''}</div>
            <div class="task-text">${task.description}</div>
        `;
        li.addEventListener('click', () => toggleTask(task.id, !task.completed, li));
        taskList.appendChild(li);
    });
}

async function toggleTask(taskId, newCompleted, liElement) {
    liElement.classList.toggle('completed', newCompleted);
    liElement.querySelector('.task-checkbox').classList.toggle('checked', newCompleted);
    liElement.querySelector('.task-checkbox').textContent = newCompleted ? '✓' : '';

    try {
        await fetch('/api/player/task', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ taskId, completed: newCompleted })
        });
    } catch (err) {
        // si falla, lo dejamos como esta, no es critico
    }
}

function updateTimer() {
    if (!endTimeMillis) return;
    const remaining = endTimeMillis - Date.now();
    if (remaining <= 0) {
        window.location.href = `fin.html?code=${localStorage.getItem('roomCode') || ''}`;
        return;
    }
    const hours = Math.floor(remaining / 3600000);
    const minutes = Math.floor((remaining % 3600000) / 60000);
    const seconds = Math.floor((remaining % 60000) / 1000);
    timerDisplay.textContent = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
}

loadScreen();
setInterval(updateTimer, 1000);