package com.impostorparty.servlet;

import com.impostorparty.service.GameService;
import com.impostorparty.service.GameStartException;
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

@WebServlet("/api/rooms/start")
public class StartGameServlet extends HttpServlet {
    private final GameService gameService = new GameService();

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
        try {
            gameService.startGame(((Number) userIdValue).intValue(), code);
            JSONObject result = new JSONObject();
            result.put("success", true);
            JsonUtil.sendJson(response, 200, result);
        } catch (GameStartException e) {
            JsonUtil.sendError(response, e.getStatusCode(), e.getMessage());
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
