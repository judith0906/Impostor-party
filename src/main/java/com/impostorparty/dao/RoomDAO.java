// FILE: src/main/java/com/impostorparty/dao/RoomDAO.java
package com.impostorparty.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;

import com.impostorparty.model.Room;
import com.impostorparty.util.DBConnection;
import com.impostorparty.util.RoomCodeGenerator;

public class RoomDAO {

    // Crea una sala con un codigo unico, reintentando si hay colision (muy improbable pero posible)
    public Room createRoom(int hostId, int numImpostors, String challengeType, int durationHours) throws SQLException {
        String sql = "INSERT INTO rooms (code, host_id, num_impostors, challenge_type, duration_hours, status) " +
                     "VALUES (?, ?, ?, ?, ?, 'LOBBY')";

        for (int attempt = 0; attempt < 5; attempt++) {
            String code = RoomCodeGenerator.generate();
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, code);
                stmt.setInt(2, hostId);
                stmt.setInt(3, numImpostors);
                stmt.setString(4, challengeType);
                stmt.setInt(5, durationHours);
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        Room room = new Room();
                        room.setId(keys.getInt(1));
                        room.setCode(code);
                        room.setHostId(hostId);
                        room.setNumImpostors(numImpostors);
                        room.setChallengeType(challengeType);
                        room.setDurationHours(durationHours);
                        room.setStatus("LOBBY");
                        return room;
                    }
                }
            } catch (SQLIntegrityConstraintViolationException dup) {
                // codigo duplicado, reintentamos con otro
            }
        }
        throw new SQLException("No se pudo generar un codigo de sala unico, intenta de nuevo");
    }

    public Room findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public void updateStatus(int roomId, String status) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, roomId);
            stmt.executeUpdate();
        }
    }

    public void setEndTime(int roomId, Timestamp endTime) throws SQLException {
        String sql = "UPDATE rooms SET end_time = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, endTime);
            stmt.setInt(2, roomId);
            stmt.executeUpdate();
        }
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getInt("id"));
        room.setCode(rs.getString("code"));
        room.setHostId(rs.getInt("host_id"));
        room.setNumImpostors(rs.getInt("num_impostors"));
        room.setChallengeType(rs.getString("challenge_type"));
        room.setEndTime(rs.getTimestamp("end_time"));
        room.setDurationHours(rs.getInt("duration_hours"));
        room.setStatus(rs.getString("status"));
        return room;
    }
}