package com.impostorparty.servlet;

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
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

@WebServlet("/api/rooms/status")
public class RoomStatusServlet extends HttpServlet {
    private final RoomDAO roomDAO = new RoomDAO();
    private final PlayerDAO playerDAO = new PlayerDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getHeader("X-Player-Token");
        if (token == null || token.trim().isEmpty()) {
            JsonUtil.sendError(response, 401, "Falta el token del jugador");
            return;
        }

        String code = request.getParameter("code");
        if (code == null || code.trim().isEmpty()) {
            JsonUtil.sendError(response, 400, "Falta el codigo de sala");
            return;
        }

        try {
            Room room = roomDAO.findByCode(code.trim().toUpperCase(Locale.ROOT));
            if (room == null) {
                JsonUtil.sendError(response, 404, "Sala no encontrada");
                return;
            }

            Player tokenPlayer = playerDAO.findBySessionToken(token.trim());
            if (tokenPlayer == null) {
                JsonUtil.sendError(response, 404, "Jugador no encontrado");
                return;
            }
            if (tokenPlayer.getRoomId() != room.getId()) {
                JsonUtil.sendError(response, 403, "El jugador no pertenece a esta sala");
                return;
            }

            HttpSession session = request.getSession(false);
            Object userIdValue = session == null ? null : session.getAttribute("userId");
            boolean isHost = userIdValue instanceof Number
                    && ((Number) userIdValue).intValue() == room.getHostId();

            List<Player> players = playerDAO.findByRoomId(room.getId());
            JSONArray playersJson = new JSONArray();
            for (Player player : players) {
                JSONObject playerJson = new JSONObject();
                playerJson.put("nickname", player.getNickname());
                playerJson.put("ready", player.isReady());
                playerJson.put("isHost", Integer.valueOf(room.getHostId()).equals(player.getUserId()));
                playersJson.put(playerJson);
            }

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("status", room.getStatus());
            result.put("numImpostors", room.getNumImpostors());
            result.put("challengeType", room.getChallengeType());
            result.put("durationHours", room.getDurationHours());
            result.put("isHost", isHost);
            result.put("myReady", tokenPlayer.isReady());
            result.put("players", playersJson);
            JsonUtil.sendJson(response, 200, result);
        } catch (SQLException e) {
            JsonUtil.sendError(response, 500, "Error de base de datos");
        }
    }
}
