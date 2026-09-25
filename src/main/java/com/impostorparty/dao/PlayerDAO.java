// FILE: src/main/java/com/impostorparty/dao/PlayerDAO.java
package com.impostorparty.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.impostorparty.model.Player;
import com.impostorparty.util.DBConnection;

public class PlayerDAO {

    // Anade un jugador a la sala y le genera un token propio (no necesita login)
    public Player addPlayer(int roomId, String nickname) throws SQLException {
        String token = UUID.randomUUID().toString();
        String sql = "INSERT INTO players (room_id, nickname, session_token) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, roomId);
            stmt.setString(2, nickname);
            stmt.setString(3, token);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    Player player = new Player();
                    player.setId(keys.getInt(1));
                    player.setRoomId(roomId);
                    player.setNickname(nickname);
                    player.setSessionToken(token);
                    player.setReady(false);
                    return player;
                }
            }
        }
        return null;
    }

    public List<Player> findByRoomId(int roomId) throws SQLException {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT * FROM players WHERE room_id = ? ORDER BY joined_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    players.add(mapRow(rs));
                }
            }
        }
        return players;
    }

    public Player findBySessionToken(String token) throws SQLException {
        String sql = "SELECT * FROM players WHERE session_token = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public void setReady(int playerId, boolean ready) throws SQLException {
        String sql = "UPDATE players SET ready = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, ready);
            stmt.setInt(2, playerId);
            stmt.executeUpdate();
        }
    }

    public void updateImpostorAndWord(int playerId, boolean isImpostor, String word) throws SQLException {
        String sql = "UPDATE players SET is_impostor = ?, word_assigned = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isImpostor);
            stmt.setString(2, word);
            stmt.setInt(3, playerId);
            stmt.executeUpdate();
        }
    }

    private Player mapRow(ResultSet rs) throws SQLException {
        Player player = new Player();
        player.setId(rs.getInt("id"));
        player.setRoomId(rs.getInt("room_id"));
        player.setNickname(rs.getString("nickname"));
        player.setSessionToken(rs.getString("session_token"));
        Object impostorVal = rs.getObject("is_impostor");
        player.setIsImpostor(impostorVal == null ? null : rs.getBoolean("is_impostor"));
        player.setReady(rs.getBoolean("ready"));
        player.setWordAssigned(rs.getString("word_assigned"));
        return player;
    }

    // Guarda las tareas asignadas a un jugador como texto libre (no referenciamos tasks.id para simplificar,
    // ya que las tareas se generan al vuelo mezclando categorias)
    public void assignTasks(int playerId, java.util.List<String> taskDescriptions) throws SQLException {
        String sql = "INSERT INTO player_tasks (player_id, task_id, completed) " +
                     "SELECT ?, id, FALSE FROM tasks WHERE description = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String desc : taskDescriptions) {
                stmt.setInt(1, playerId);
                stmt.setString(2, desc);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // Recupera las tareas asignadas a un jugador con su estado
    public List<java.util.Map<String, Object>> getPlayerTasks(int playerId) throws SQLException {
        List<java.util.Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT pt.id, t.description, pt.completed FROM player_tasks pt " +
                     "JOIN tasks t ON pt.task_id = t.id WHERE pt.player_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> row = new java.util.HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("description", rs.getString("description"));
                    row.put("completed", rs.getBoolean("completed"));
                    result.add(row);
                }
            }
        }
        return result;
    }

    public void markTaskCompleted(int playerTaskId, boolean completed) throws SQLException {
        String sql = "UPDATE player_tasks SET completed = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, completed);
            stmt.setInt(2, playerTaskId);
            stmt.executeUpdate();
        }
    }
}