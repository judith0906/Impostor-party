// FILE: src/main/java/com/impostorparty/servlet/RoomStatusServlet.java
package com.impostorparty.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONArray;
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

// Endpoint de polling: el lobby lo consulta cada pocos segundos
@WebServlet("/api/rooms/status")
public class RoomStatusServlet extends HttpServlet {

    private final RoomDAO roomDAO = new RoomDAO();
    private final PlayerDAO playerDAO = new PlayerDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String code = request.getParameter("code");
        if (code == null || code.trim().isEmpty()) {
            JsonUtil.sendError(response, 400, "Falta el codigo de sala");
            return;
        }

        try {
            Room room = roomDAO.findByCode(code.trim().toUpperCase());
            if (room == null) {
                JsonUtil.sendError(response, 404, "Sala no encontrada");
                return;
            }

            List<Player> players = playerDAO.findByRoomId(room.getId());

            JSONArray playersJson = new JSONArray();
            for (Player p : players) {
                JSONObject pj = new JSONObject();
                pj.put("nickname", p.getNickname());
                pj.put("ready", p.isReady());
                playersJson.put(pj);
            }

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("status", room.getStatus());
            result.put("numImpostors", room.getNumImpostors());
            result.put("challengeType", room.getChallengeType());
            result.put("durationHours", room.getDurationHours());
            result.put("players", playersJson);

            JsonUtil.sendJson(response, 200, result);

        } catch (SQLException e) {
            JsonUtil.sendError(response, 500, "Error de base de datos: " + e.getMessage());
        }
    }
}