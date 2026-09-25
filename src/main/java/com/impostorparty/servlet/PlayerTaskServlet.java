// FILE: src/main/java/com/impostorparty/servlet/PlayerTaskServlet.java
package com.impostorparty.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONObject;

import com.impostorparty.dao.PlayerDAO;
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

        JSONObject body = readJsonBody(request);
        int taskId = body.optInt("taskId", -1);
        boolean completed = body.optBoolean("completed", false);

        if (taskId < 0) {
            JsonUtil.sendError(response, 400, "Falta el id de la tarea");
            return;
        }

        try {
            playerDAO.markTaskCompleted(taskId, completed);
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