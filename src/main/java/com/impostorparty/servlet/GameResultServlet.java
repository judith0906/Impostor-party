// FILE: src/main/java/com/impostorparty/servlet/GameResultServlet.java
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
import java.util.List;

@WebServlet("/api/rooms/result")
public class GameResultServlet extends HttpServlet {

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

            // Marcamos la sala como finalizada la primera vez que se consulta el resultado tras acabar el tiempo
            if ("IN_PROGRESS".equals(room.getStatus())) {
                roomDAO.updateStatus(room.getId(), "FINISHED");
            }

            List<Player> players = playerDAO.findByRoomId(room.getId());

            JSONArray impostorsJson = new JSONArray();
            String normalWord = null;
            String impostorWord = null;

            for (Player p : players) {
                if (Boolean.TRUE.equals(p.getIsImpostor())) {
                    impostorsJson.put(p.getNickname());
                    impostorWord = p.getWordAssigned();
                } else {
                    normalWord = p.getWordAssigned();
                }
            }

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("impostors", impostorsJson);
            result.put("normalWord", normalWord);
            result.put("impostorWord", impostorWord);
            JsonUtil.sendJson(response, 200, result);

        } catch (SQLException e) {
            JsonUtil.sendError(response, 500, "Error de base de datos: " + e.getMessage());
        }
    }
}