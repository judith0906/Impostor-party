// FILE: src/main/java/com/impostorparty/servlet/PlayerScreenServlet.java
package com.impostorparty.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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

@WebServlet("/api/player/screen")
public class PlayerScreenServlet extends HttpServlet {

    private final PlayerDAO playerDAO = new PlayerDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("sessionToken");
        if (token == null || token.trim().isEmpty()) {
            JsonUtil.sendError(response, 400, "Falta el token de sesion");
            return;
        }

        try {
            Player player = playerDAO.findBySessionToken(token);
            if (player == null) {
                JsonUtil.sendError(response, 404, "Jugador no encontrado");
                return;
            }

            Room room = roomDAO.findByCode(getRoomCode(player.getRoomId()));
            

            List<Map<String, Object>> tasks = playerDAO.getPlayerTasks(player.getId());
            JSONArray tasksJson = new JSONArray();
            for (Map<String, Object> t : tasks) {
                JSONObject tj = new JSONObject();
                tj.put("id", t.get("id"));
                tj.put("description", t.get("description"));
                tj.put("completed", t.get("completed"));
                tasksJson.put(tj);
            }

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("nickname", player.getNickname());
            result.put("word", player.getWordAssigned());
            result.put("tasks", tasksJson);
            result.put("endTime", room != null && room.getEndTime() != null ? room.getEndTime().getTime() : 0);
            result.put("roomCode", room != null ? room.getCode() : "");
            JsonUtil.sendJson(response, 200, result);

        } catch (SQLException e) {
            JsonUtil.sendError(response, 500, "Error de base de datos: " + e.getMessage());
        }
    }

    // Pequena ayuda: como Room no se busca por id directamente en RoomDAO, resolvemos via una consulta simple
    private String getRoomCode(int roomId) throws SQLException {
        String sql = "SELECT code FROM rooms WHERE id = ?";
        try (java.sql.Connection conn = com.impostorparty.util.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("code");
            }
        }
        return null;
    }
}