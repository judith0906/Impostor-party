package com.impostorparty.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Ajusta estos datos a tu configuracion local de MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/impostor_party?useSSL=false&serverTimezone=UTC";
    private static final String USER = "judith";
    private static final String PASSWORD = "Jcm270906#";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontro el driver de MySQL", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}