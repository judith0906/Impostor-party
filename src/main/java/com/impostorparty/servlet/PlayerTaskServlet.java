// FILE: src/main/java/com/impostorparty/servlet/PlayerTaskServlet.java
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

@WebServlet("/api/player/task")
public class PlayerTaskServlet extends HttpServlet {

    private final PlayerDAO playerDAO = new PlayerDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JSONObject body;
        try {
            body = readJsonBody(request);
        } catch (RuntimeException e) {
            JsonUtil.sendError(response, 400, "Cuerpo JSON invalido");
            return;
        }

        String token = request.getHeader("X-Player-Token");
        if (token == null || token.trim().isEmpty()) {
            JsonUtil.sendError(response, 401, "Falta el token del jugador");
            return;
        }
        token = token.trim();
        int taskId = body.optInt("taskId", -1);
        boolean completed = body.optBoolean("completed", false);

        if (taskId < 0) {
            JsonUtil.sendError(response, 400, "Falta el id de la tarea");
            return;
        }

        try {
            Player player = playerDAO.findBySessionToken(token);
            if (player == null) {
                JsonUtil.sendError(response, 404, "Jugador no encontrado");
                return;
            }

            PlayerDAO.TaskUpdateResult result = playerDAO.markTaskCompleted(player.getId(), taskId, completed);
            if (result == PlayerDAO.TaskUpdateResult.NOT_FOUND) {
                JsonUtil.sendError(response, 404, "La tarea no pertenece a este jugador");
                return;
            }
            if (result == PlayerDAO.TaskUpdateResult.NOT_IN_PROGRESS) {
                JsonUtil.sendError(response, 409, "La partida no esta en curso");
                return;
            }

            JSONObject responseBody = new JSONObject();
            responseBody.put("success", true);
            JsonUtil.sendJson(response, 200, responseBody);
        } catch (SQLException e) {
            JsonUtil.sendError(response, 500, "Error de base de datos");
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
