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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

@WebServlet("/api/rooms/result")
public class GameResultServlet extends HttpServlet {
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

            Player player = playerDAO.findBySessionToken(token.trim());
            if (player == null) {
                JsonUtil.sendError(response, 404, "Jugador no encontrado");
                return;
            }
            if (player.getRoomId() != room.getId()) {
                JsonUtil.sendError(response, 403, "El jugador no pertenece a esta sala");
                return;
            }

            if ("LOBBY".equals(room.getStatus())) {
                JsonUtil.sendError(response, 409, "La partida todavia no ha empezado");
                return;
            }
            if ("IN_PROGRESS".equals(room.getStatus())) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                if (room.getEndTime() == null || room.getEndTime().after(now)) {
                    JsonUtil.sendError(response, 409, "La partida todavia no ha terminado");
                    return;
                }
                if (roomDAO.finishIfExpired(room.getId(), now)) {
                    room.setStatus("FINISHED");
                } else {
                    room = roomDAO.findById(room.getId());
                    if (room == null || "IN_PROGRESS".equals(room.getStatus())) {
                        JsonUtil.sendError(response, 409, "La partida todavia no ha terminado");
                        return;
                    }
                }
            }
            if (!"FINISHED".equals(room.getStatus())) {
                JsonUtil.sendError(response, 409, "La sala no tiene un resultado disponible");
                return;
            }

            List<Player> players = playerDAO.findByRoomId(room.getId());
            boolean hasImpostor = false;
            boolean hasCivilWord = false;
            for (Player roomPlayer : players) {
                hasImpostor |= Boolean.TRUE.equals(roomPlayer.getIsImpostor());
                hasCivilWord |= !Boolean.TRUE.equals(roomPlayer.getIsImpostor())
                        && roomPlayer.getWordAssigned() != null;
            }
            if (!hasImpostor || !hasCivilWord) {
                JsonUtil.sendError(response, 409, "La sala no tiene datos de partida");
                return;
            }

            JSONArray impostors = new JSONArray();
            JSONArray impostorDetails = new JSONArray();
            String normalWord = null;
            for (Player roomPlayer : players) {
                if (Boolean.TRUE.equals(roomPlayer.getIsImpostor())) {
                    impostors.put(roomPlayer.getNickname());
                    JSONObject detail = new JSONObject();
                    detail.put("nickname", roomPlayer.getNickname());
                    detail.put("word", roomPlayer.getWordAssigned());
                    impostorDetails.put(detail);
                } else {
                    normalWord = roomPlayer.getWordAssigned();
                }
            }

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("impostors", impostors);
            result.put("normalWord", normalWord);
            result.put("impostorDetails", impostorDetails);
            response.setHeader("Cache-Control", "no-store");
            JsonUtil.sendJson(response, 200, result);
        } catch (SQLException e) {
            JsonUtil.sendError(response, 500, "Error de base de datos");
        }
    }
}
