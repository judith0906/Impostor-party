// FILE: src/main/java/com/impostorparty/servlet/CreateRoomServlet.java
package com.impostorparty.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONObject;

import com.impostorparty.dao.RoomDAO;
import com.impostorparty.model.Room;
import com.impostorparty.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/rooms")
public class CreateRoomServlet extends HttpServlet {

    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            JsonUtil.sendError(response, 401, "Debes iniciar sesion para crear una sala");
            return;
        }
        int hostId = (int) session.getAttribute("userId");

        JSONObject body = readJsonBody(request);
        int numImpostors = body.optInt("numImpostors", 1);
        String challengeType = body.optString("challengeType", "normales");
        int durationHours = body.optInt("durationHours", 3);

        if (numImpostors < 1 || durationHours < 1 || durationHours > 12) {
            JsonUtil.sendError(response, 400, "Configuracion de sala invalida");
            return;
        }

        try {
            Room room = roomDAO.createRoom(hostId, numImpostors, challengeType, durationHours);
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("code", room.getCode());
            JsonUtil.sendJson(response, 201, result);
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