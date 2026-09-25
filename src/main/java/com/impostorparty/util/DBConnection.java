// FILE: src/main/java/com/impostorparty/util/DBConnection.java
package com.impostorparty.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    public static Connection getConnection() throws SQLException {
        String url = requireEnv("DB_URL");
        String user = requireEnv("DB_USER");
        String password = requireEnv("DB_PASSWORD");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontro el driver de MySQL", e);
        }
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        properties.setProperty("useUnicode", "true");
        properties.setProperty("characterEncoding", "UTF-8");
        return DriverManager.getConnection(url, properties);
    }

    private static String requireEnv(String name) throws SQLException {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new SQLException("Falta la variable de entorno " + name);
        }
        return value.trim();
    }
}