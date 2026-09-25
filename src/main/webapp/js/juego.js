// FILE: src/main/webapp/js/juego.js
const sessionToken = sessionStorage.getItem('sessionToken');
const nickname = sessionStorage.getItem('nickname');

const playerNameDisplay = document.getElementById('playerNameDisplay');
const wordDisplay = document.getElementById('wordDisplay');
const taskList = document.getElementById('taskList');
const timerDisplay = document.getElementById('timerDisplay');

playerNameDisplay.textContent = `Jugando como ${nickname || '...'}`;

let endTimeMillis = 0;

async function loadScreen() {
    try {
        const response = await fetch('/api/player/screen', {
            headers: { 'X-Player-Token': sessionToken || '' },
            cache: 'no-store'
        });
        const data = await response.json();

        if (!response.ok) {
            wordDisplay.textContent = data.message || 'Error';
            return;
        }

        wordDisplay.textContent = data.word;
        sessionStorage.setItem('roomCode', data.roomCode);
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
        const checkbox = document.createElement('div');
        checkbox.className = 'task-checkbox' + (task.completed ? ' checked' : '');
        checkbox.textContent = task.completed ? '✓' : '';

        const taskText = document.createElement('div');
        taskText.className = 'task-text';
        taskText.textContent = String(task.description || '');

        li.appendChild(checkbox);
        li.appendChild(taskText);
        li.addEventListener('click', () => toggleTask(task.id, !task.completed, li));
        taskList.appendChild(li);
    });
}

async function toggleTask(taskId, newCompleted, liElement) {
    liElement.classList.toggle('completed', newCompleted);
    liElement.querySelector('.task-checkbox').classList.toggle('checked', newCompleted);
    liElement.querySelector('.task-checkbox').textContent = newCompleted ? '✓' : '';

    try {
        const response = await fetch('/api/player/task', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Player-Token': sessionToken || ''
            },
            body: JSON.stringify({ taskId, completed: newCompleted })
        });
        if (!response.ok) {
            liElement.classList.toggle('completed', !newCompleted);
            liElement.querySelector('.task-checkbox').classList.toggle('checked', !newCompleted);
            liElement.querySelector('.task-checkbox').textContent = newCompleted ? '' : '✓';
        }
    } catch (err) {
        liElement.classList.toggle('completed', !newCompleted);
        liElement.querySelector('.task-checkbox').classList.toggle('checked', !newCompleted);
        liElement.querySelector('.task-checkbox').textContent = newCompleted ? '' : '✓';
    }
}

function updateTimer() {
    if (!endTimeMillis) return;
    const remaining = endTimeMillis - Date.now();
    if (remaining <= 0) {
        window.location.href = `fin.html?code=${sessionStorage.getItem('roomCode') || ''}`;
        return;
    }
    const hours = Math.floor(remaining / 3600000);
    const minutes = Math.floor((remaining % 3600000) / 60000);
    const seconds = Math.floor((remaining % 60000) / 1000);
    timerDisplay.textContent = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
}

loadScreen();
setInterval(updateTimer, 1000);