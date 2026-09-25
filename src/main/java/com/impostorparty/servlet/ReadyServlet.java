package com.impostorparty.servlet;

import com.impostorparty.dao.PlayerDAO;
import com.impostorparty.model.Player;
import com.impostorparty.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/rooms/ready")
public class ReadyServlet extends HttpServlet {
    private final PlayerDAO playerDAO = new PlayerDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JSONObject body;
        try {
            body = readJsonBody(request);
        } catch (JSONException e) {
            JsonUtil.sendError(response, 400, "Cuerpo JSON invalido");
            return;
        }

        String token = request.getHeader("X-Player-Token");
        if (token == null || token.trim().isEmpty()) {
            JsonUtil.sendError(response, 401, "Falta el token del jugador");
            return;
        }
        token = token.trim();
        boolean ready = body.optBoolean("ready", false);

        try {
            Player player = playerDAO.findBySessionToken(token);
            if (player == null) {
                JsonUtil.sendError(response, 404, "Jugador no encontrado");
                return;
            }
            if (!playerDAO.setReadyIfLobby(player.getRoomId(), player.getId(), ready)) {
                JsonUtil.sendError(response, 409, "La sala ya no esta en LOBBY");
                return;
            }

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("ready", ready);
            JsonUtil.sendJson(response, 200, result);
        } catch (SQLException e) {
            JsonUtil.sendError(response, 500, "Error de base de datos");
        }
    }

    private JSONObject readJsonBody(HttpServletRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return new JSONObject(body.toString());
    }
}
