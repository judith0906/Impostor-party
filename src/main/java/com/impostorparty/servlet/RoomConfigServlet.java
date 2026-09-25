package com.impostorparty.servlet;

import com.impostorparty.dao.RoomDAO;
import com.impostorparty.model.Room;
import com.impostorparty.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;
import java.util.Locale;

@WebServlet("/api/rooms/config")
public class RoomConfigServlet extends HttpServlet {
    private static final Set<String> CHALLENGE_TYPES = Set.of(
            "normales", "amigos", "picantes", "salseo", "extremos"
    );

    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Object userIdValue = session == null ? null : session.getAttribute("userId");
        if (!(userIdValue instanceof Number)) {
            JsonUtil.sendError(response, 401, "Debes iniciar sesion como host");
            return;
        }

        JSONObject body;
        try {
            body = readJsonBody(request);
        } catch (JSONException e) {
            JsonUtil.sendError(response, 400, "Cuerpo JSON invalido");
            return;
        }

        String code = body.optString("code", "").trim();
        if (code.isEmpty()) {
            JsonUtil.sendError(response, 400, "Falta el codigo de sala");
            return;
        }

        int numImpostors = body.optInt("numImpostors", -1);
        int durationHours = body.optInt("durationHours", -1);
        String challengeType = body.optString("challengeType", "");
        if (numImpostors < 1 || numImpostors > 3
                || durationHours < 1 || durationHours > 12
                || !CHALLENGE_TYPES.contains(challengeType)) {
            JsonUtil.sendError(response, 400, "Configuracion de sala invalida");
            return;
        }

        try {
            Room room = roomDAO.findByCode(code.toUpperCase(Locale.ROOT));
            if (room == null) {
                JsonUtil.sendError(response, 404, "Sala no encontrada");
                return;
            }
            if (room.getHostId() != ((Number) userIdValue).intValue()) {
                JsonUtil.sendError(response, 403, "Solo el host puede configurar la sala");
                return;
            }
            if (!"LOBBY".equals(room.getStatus())) {
                JsonUtil.sendError(response, 409, "La sala solo puede configurarse en LOBBY");
                return;
            }
            if (!roomDAO.updateConfiguration(room.getId(), numImpostors, challengeType, durationHours)) {
                JsonUtil.sendError(response, 409, "La sala ya no esta en LOBBY");
                return;
            }

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("code", room.getCode());
            result.put("numImpostors", numImpostors);
            result.put("durationHours", durationHours);
            result.put("challengeType", challengeType);
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
