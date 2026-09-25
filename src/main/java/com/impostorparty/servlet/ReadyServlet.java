// FILE: src/main/java/com/impostorparty/servlet/ReadyServlet.java
package com.impostorparty.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONObject;

import com.impostorparty.dao.PlayerDAO;
import com.impostorparty.model.Player;
import com.impostorparty.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/rooms/ready")
public class ReadyServlet extends HttpServlet {

    private final PlayerDAO playerDAO = new PlayerDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JSONObject body = readJsonBody(request);
        String token = body.optString("sessionToken", "");
        boolean ready = body.optBoolean("ready", false);

        if (token.isEmpty()) {
            JsonUtil.sendError(response, 400, "Falta el token de sesion del jugador");
            return;
        }

        try {
            Player player = playerDAO.findBySessionToken(token);
            if (player == null) {
                JsonUtil.sendError(response, 404, "Jugador no encontrado");
                return;
            }
            playerDAO.setReady(player.getId(), ready);

            JSONObject result = new JSONObject();
            result.put("success", true);
            JsonUtil.sendJson(response, 200, result);

        } catch (SQLException e) {
            JsonUtil.sendError(response, 500, "Error de base de datos: " + e.getMessage());
        }
    }

    private JSONObject readJsonBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return new JSONObject(sb.toString());
    }
}