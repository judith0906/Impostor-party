package com.impostorparty.util;

import java.io.IOException;

import org.json.JSONObject;

import jakarta.servlet.http.HttpServletResponse;

public class JsonUtil {

    // Envia una respuesta JSON generica con el status HTTP indicado
    public static void sendJson(HttpServletResponse response, int statusCode, JSONObject body) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(body.toString());
    }

    // Atajo para respuestas de error con un mensaje
    public static void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        JSONObject error = new JSONObject();
        error.put("success", false);
        error.put("message", message);
        sendJson(response, statusCode, error);
    }
}