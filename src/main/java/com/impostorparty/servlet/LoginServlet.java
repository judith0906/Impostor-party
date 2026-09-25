// FILE: src/main/java/com/impostorparty/servlet/LoginServlet.java
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

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JSONObject body = readJsonBody(request);
        String username = body.optString("username", "").trim();
        String password = body.optString("password", "").trim();

        try {
            User user = userDAO.findByUsername(username);
            if (user == null || !PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
                JsonUtil.sendError(response, 401, "Usuario o contrasena incorrectos");
                return;
            }

            request.getSession().setAttribute("userId", user.getId());
            request.getSession().setAttribute("username", user.getUsername());

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("username", user.getUsername());
            JsonUtil.sendJson(response, 200, result);

        } catch (SQLException e) {
            JsonUtil.sendError(response, 500, "Error de base de datos: " + e.getMessage());
        }
    }

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