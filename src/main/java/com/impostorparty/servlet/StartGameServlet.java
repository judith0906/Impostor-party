// FILE: src/main/java/com/impostorparty/servlet/StartGameServlet.java
package com.impostorparty.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import com.impostorparty.dao.ContentDAO;
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

@WebServlet("/api/rooms/start")
public class StartGameServlet extends HttpServlet {

    private final RoomDAO roomDAO = new RoomDAO();
    private final PlayerDAO playerDAO = new PlayerDAO();
    private final ContentDAO contentDAO = new ContentDAO();
    private static final int TASKS_PER_PLAYER = 5;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            JsonUtil.sendError(response, 401, "Debes iniciar sesion como host");
            return;
        }
        int hostId = (int) session.getAttribute("userId");

        JSONObject body = readJsonBody(request);
        String code = body.optString("code", "").trim().toUpperCase();

        try {
            Room room = roomDAO.findByCode(code);
            if (room == null) {
                JsonUtil.sendError(response, 404, "Sala no encontrada");
                return;
            }
            if (room.getHostId() != hostId) {
                JsonUtil.sendError(response, 403, "Solo el host puede iniciar la partida");
                return;
            }
            if (!"LOBBY".equals(room.getStatus())) {
                JsonUtil.sendError(response, 409, "La partida ya ha empezado o ha terminado");
                return;
            }

            List<Player> players = playerDAO.findByRoomId(room.getId());
            if (players.size() < 3) {
                JsonUtil.sendError(response, 400, "Hacen falta al menos 3 jugadores");
                return;
            }
            for (Player p : players) {
                if (!p.isReady()) {
                    JsonUtil.sendError(response, 409, "No todos los jugadores estan listos todavia");
                    return;
                }
            }
            if (room.getNumImpostors() >= players.size()) {
                JsonUtil.sendError(response, 400, "Demasiados impostores para el numero de jugadores");
                return;
            }

            // Elegimos aleatoriamente quienes son los impostores
            List<Player> shuffled = new ArrayList<>(players);
            Collections.shuffle(shuffled);
            Set<Integer> impostorIds = new HashSet<>();
            for (int i = 0; i < room.getNumImpostors(); i++) {
                impostorIds.add(shuffled.get(i).getId());
            }

            // Una palabra para los normales, otra distinta para los impostores
            List<String> words = contentDAO.getRandomWords(room.getChallengeType(), 2);
            if (words.size() < 2) {
                JsonUtil.sendError(response, 500, "No hay suficientes palabras cargadas para esta categoria");
                return;
            }
            String normalWord = words.get(0);
            String impostorWord = words.get(1);

            for (Player p : players) {
                boolean isImpostor = impostorIds.contains(p.getId());
                String assignedWord = isImpostor ? impostorWord : normalWord;

                playerDAO.updateImpostorAndWord(p.getId(), isImpostor, assignedWord);

                List<String> tasks = contentDAO.getRandomTasks(room.getChallengeType(), isImpostor, TASKS_PER_PLAYER);
                playerDAO.assignTasks(p.getId(), tasks);
            }

            long endMillis = System.currentTimeMillis() + (room.getDurationHours() * 3600L * 1000L);
            roomDAO.setEndTime(room.getId(), new Timestamp(endMillis));
            roomDAO.updateStatus(room.getId(), "IN_PROGRESS");

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