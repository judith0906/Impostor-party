// FILE: src/main/java/com/impostorparty/util/DBConnection.java
package com.impostorparty.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Lee la configuracion de variables de entorno; si no existen, usa estos valores por defecto (solo para desarrollo local)
    private static final String URL = "jdbc:mysql://localhost:3306/impostor_party?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "DB_USER";
    private static final String PASSWORD = "DB_PASSWORD";

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontro el driver de MySQL", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}