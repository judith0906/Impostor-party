// FILE: src/main/java/com/impostorparty/servlet/RegisterServlet.java
package com.impostorparty.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONObject;

import com.impostorparty.dao.UserDAO;
import com.impostorparty.model.User;
import com.impostorparty.util.JsonUtil;
import com.impostorparty.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JSONObject body = readJsonBody(request);
        String username = body.optString("username", "").trim();
        String password = body.optString("password", "").trim();

        if (username.isEmpty() || password.isEmpty()) {
            JsonUtil.sendError(response, 400, "Usuario y contrasena son obligatorios");
            return;
        }
        if (password.length() < 4) {
            JsonUtil.sendError(response, 400, "La contrasena debe tener al menos 4 caracteres");
            return;
        }

        try {
            User existing = userDAO.findByUsername(username);
            if (existing != null) {
                JsonUtil.sendError(response, 409, "Ese nombre de usuario ya existe");
                return;
            }

            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword(password, salt);
            int newId = userDAO.create(username, hash, salt);

            request.getSession().setAttribute("userId", newId);
            request.getSession().setAttribute("username", username);

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("username", username);
            JsonUtil.sendJson(response, 201, result);

        } catch (SQLException e) {
            JsonUtil.sendError(response, 500, "Error de base de datos: " + e.getMessage());
        }
    }

    // Lee el cuerpo de la peticion como JSON
    private JSONObject readJsonBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return new JSONObject(sb.toString());
    }
}