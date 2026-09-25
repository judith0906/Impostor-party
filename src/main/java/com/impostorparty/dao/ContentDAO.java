// FILE: src/main/java/com/impostorparty/dao/ContentDAO.java
package com.impostorparty.dao;

import com.impostorparty.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContentDAO {

    // Devuelve N palabras distintas al azar para una categoria (para repartir una a normales y otra a impostores)
    public List<String> getRandomWords(String challengeType, int count) throws SQLException {
        List<String> words = new ArrayList<>();
        String sql = "SELECT word FROM words WHERE challenge_type = ? ORDER BY RAND() LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, challengeType);
            stmt.setInt(2, count);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    words.add(rs.getString("word"));
                }
            }
        }
        return words;
    }

    // Devuelve N tareas al azar para una categoria, filtrando si son de impostor o no
    public List<String> getRandomTasks(String challengeType, boolean forImpostor, int count) throws SQLException {
        List<String> tasks = new ArrayList<>();
        String sql = "SELECT description FROM tasks WHERE challenge_type = ? AND is_for_impostor = ? ORDER BY RAND() LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, challengeType);
            stmt.setBoolean(2, forImpostor);
            stmt.setInt(3, count);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(rs.getString("description"));
                }
            }
        }
        return tasks;
    }
}