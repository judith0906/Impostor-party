<!-- FILE: src/main/webapp/index.html -->
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Impostor Party</title>
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <div class="app-container">
        <div class="logo-area">
            <div class="mask-icon">?</div>
            <h1>Impostor <span class="accent">Party</span></h1>
            <p class="tagline">¿Quien es el impostor esta noche?</p>
        </div>

        <div class="card auth-card">
            <div class="tabs">
                <button class="tab-btn active" id="tabLogin">Entrar</button>
                <button class="tab-btn" id="tabRegister">Crear cuenta</button>
            </div>

            <form id="authForm">
                <input type="text" id="username" placeholder="Nombre de usuario" required>
                <input type="password" id="password" placeholder="Contrasena" required>
                <p class="error-msg" id="errorMsg"></p>
                <button type="submit" class="btn-primary" id="submitBtn">Entrar</button>
            </form>
        </div>
    </div>

    <script src="js/auth.js"></script>
</body>
</html>