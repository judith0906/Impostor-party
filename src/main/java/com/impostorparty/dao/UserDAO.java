// FILE: src/main/java/com/impostorparty/dao/UserDAO.java
package com.impostorparty.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.impostorparty.model.User;
import com.impostorparty.util.DBConnection;

public class UserDAO {

    // Busca un usuario por su nombre de usuario, o null si no existe
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, salt FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("salt")
                    );
                }
            }
        }
        return null;
    }

    // Inserta un nuevo usuario y devuelve su id generado
    public int create(String username, String passwordHash, String salt) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, salt);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }
}