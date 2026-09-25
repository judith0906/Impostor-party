// FILE: src/main/java/com/impostorparty/servlet/JoinRoomServlet.java
package com.impostorparty.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONObject;

import com.impostorparty.dao.PlayerDAO;
import com.impostorparty.dao.RoomDAO;
import com.impostorparty.model.Player;
import com.impostorparty.model.Room;
import com.impostorparty.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/rooms/join")
public class JoinRoomServlet extends HttpServlet {

    private final RoomDAO roomDAO = new RoomDAO();
    private final PlayerDAO playerDAO = new PlayerDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JSONObject body = readJsonBody(request);
        String code = body.optString("code", "").trim().toUpperCase();
        String nickname = body.optString("nickname", "").trim();

        if (code.isEmpty() || nickname.isEmpty()) {
            JsonUtil.sendError(response, 400, "Codigo y nombre son obligatorios");
            return;
        }

        try {
            Room room = roomDAO.findByCode(code);
            if (room == null) {
                JsonUtil.sendError(response, 404, "No existe ninguna sala con ese codigo");
                return;
            }
            if (!"LOBBY".equals(room.getStatus())) {
                JsonUtil.sendError(response, 409, "Esa partida ya ha empezado o ha terminado");
                return;
            }

            Player player = playerDAO.addPlayer(room.getId(), nickname);

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("sessionToken", player.getSessionToken());
            result.put("code", room.getCode());
            JsonUtil.sendJson(response, 201, result);

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("unique_nick_per_room")) {
                JsonUtil.sendError(response, 409, "Ese nombre ya esta en uso en esta sala, prueba otro");
            } else {
                JsonUtil.sendError(response, 500, "Error de base de datos: " + e.getMessage());
            }
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