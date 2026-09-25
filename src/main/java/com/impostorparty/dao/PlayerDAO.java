package com.impostorparty.dao;

import com.impostorparty.model.Player;
import com.impostorparty.model.TaskContent;
import com.impostorparty.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerDAO {

    public Player addPlayer(int roomId, String nickname) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return addPlayer(conn, roomId, null, nickname);
        }
    }

    public Player addPlayer(Connection conn, int roomId, Integer userId, String nickname) throws SQLException {
        String token = UUID.randomUUID().toString();
        String sql = "INSERT INTO players (room_id, user_id, nickname, session_token) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, roomId);
            if (userId == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, userId);
            }
            stmt.setString(3, nickname);
            stmt.setString(4, token);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se pudo obtener el id del jugador");
                }
                Player player = new Player();
                player.setId(keys.getInt(1));
                player.setRoomId(roomId);
                player.setUserId(userId);
                player.setNickname(nickname);
                player.setSessionToken(token);
                player.setReady(false);
                return player;
            }
        }
    }

    public List<Player> findByRoomId(int roomId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findByRoomId(conn, roomId);
        }
    }

    public List<Player> findByRoomId(Connection conn, int roomId) throws SQLException {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT * FROM players WHERE room_id = ? ORDER BY joined_at ASC, id ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = DBConnection.getConnection()) {
            return findBySessionToken(conn, token);
        }
    }

    public Player findBySessionToken(Connection conn, String token) throws SQLException {
        String sql = "SELECT * FROM players WHERE session_token = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean setReadyIfLobby(int roomId, int playerId, boolean ready) throws SQLException {
        boolean updated = false;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean lobby;
                String roomSql = "SELECT status FROM rooms WHERE id = ? FOR UPDATE";
                try (PreparedStatement roomStmt = conn.prepareStatement(roomSql)) {
                    roomStmt.setInt(1, roomId);
                    try (ResultSet rs = roomStmt.executeQuery()) {
                        lobby = rs.next() && "LOBBY".equals(rs.getString("status"));
                    }
                }

                if (lobby) {
                    String playerSql = "UPDATE players SET ready = ? WHERE id = ?";
                    try (PreparedStatement playerStmt = conn.prepareStatement(playerSql)) {
                        playerStmt.setBoolean(1, ready);
                        playerStmt.setInt(2, playerId);
                        updated = playerStmt.executeUpdate() > 0;
                    }
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        }
        return updated;
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
        try (Connection conn = DBConnection.getConnection()) {
            updateImpostorAndWord(conn, playerId, isImpostor, word);
        }
    }

    public void updateImpostorAndWord(Connection conn, int playerId, boolean isImpostor, String word)
            throws SQLException {
        String sql = "UPDATE players SET is_impostor = ?, word_assigned = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isImpostor);
            stmt.setString(2, word);
            stmt.setInt(3, playerId);
            stmt.executeUpdate();
        }
    }

    public void assignTasks(int playerId, List<TaskContent> tasks) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            assignTasks(conn, playerId, tasks);
        }
    }

    public void assignTasks(Connection conn, int playerId, List<TaskContent> tasks) throws SQLException {
        String sql = "INSERT INTO player_tasks (player_id, task_id, position, completed) VALUES (?, ?, ?, FALSE)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int position = 0; position < tasks.size(); position++) {
                stmt.setInt(1, playerId);
                stmt.setInt(2, tasks.get(position).getId());
                stmt.setInt(3, position);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public List<Map<String, Object>> getPlayerTasks(int playerId) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT pt.id, pt.task_id, t.description, pt.position, pt.completed " +
                "FROM player_tasks pt JOIN tasks t ON pt.task_id = t.id " +
                "WHERE pt.player_id = ? ORDER BY pt.position ASC, pt.id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("taskId", rs.getInt("task_id"));
                    row.put("description", rs.getString("description"));
                    row.put("position", rs.getInt("position"));
                    row.put("completed", rs.getBoolean("completed"));
                    result.add(row);
                }
            }
        }
        return result;
    }

    public TaskUpdateResult markTaskCompleted(int playerId, int playerTaskId, boolean completed)
            throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String roomSql = "SELECT r.status FROM players p JOIN rooms r ON r.id = p.room_id " +
                        "WHERE p.id = ? FOR UPDATE";
                String roomStatus;
                try (PreparedStatement roomStmt = conn.prepareStatement(roomSql)) {
                    roomStmt.setInt(1, playerId);
                    try (ResultSet rs = roomStmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.commit();
                            return TaskUpdateResult.NOT_FOUND;
                        }
                        roomStatus = rs.getString("status");
                    }
                }

                if (!"IN_PROGRESS".equals(roomStatus)) {
                    conn.commit();
                    return TaskUpdateResult.NOT_IN_PROGRESS;
                }

                String taskSql = "SELECT 1 FROM player_tasks WHERE id = ? AND player_id = ?";
                boolean belongsToPlayer;
                try (PreparedStatement taskStmt = conn.prepareStatement(taskSql)) {
                    taskStmt.setInt(1, playerTaskId);
                    taskStmt.setInt(2, playerId);
                    try (ResultSet rs = taskStmt.executeQuery()) {
                        belongsToPlayer = rs.next();
                    }
                }
                if (!belongsToPlayer) {
                    conn.commit();
                    return TaskUpdateResult.NOT_FOUND;
                }

                String updateSql = "UPDATE player_tasks SET completed = ? WHERE id = ? AND player_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setBoolean(1, completed);
                    updateStmt.setInt(2, playerTaskId);
                    updateStmt.setInt(3, playerId);
                    updateStmt.executeUpdate();
                }
                conn.commit();
                return TaskUpdateResult.UPDATED;
            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        }
    }

    public enum TaskUpdateResult {
        UPDATED,
        NOT_FOUND,
        NOT_IN_PROGRESS
    }

    private Player mapRow(ResultSet rs) throws SQLException {
        Player player = new Player();
        player.setId(rs.getInt("id"));
        player.setRoomId(rs.getInt("room_id"));
        Object userId = rs.getObject("user_id");
        player.setUserId(userId == null ? null : rs.getInt("user_id"));
        player.setNickname(rs.getString("nickname"));
        player.setSessionToken(rs.getString("session_token"));
        Object impostorValue = rs.getObject("is_impostor");
        player.setIsImpostor(impostorValue == null ? null : rs.getBoolean("is_impostor"));
        player.setReady(rs.getBoolean("ready"));
        player.setWordAssigned(rs.getString("word_assigned"));
        return player;
    }

    private void rollback(Connection conn, Exception original) {
        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }
}
