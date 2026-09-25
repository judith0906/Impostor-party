package com.impostorparty.dao;

import com.impostorparty.model.Player;
import com.impostorparty.model.Room;
import com.impostorparty.model.RoomCreation;
import com.impostorparty.util.DBConnection;
import com.impostorparty.util.RoomCodeGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;

public class RoomDAO {
    private static final int ROOM_CODE_ATTEMPTS = 5;

    public Room createRoom(int hostId, int numImpostors, String challengeType, int durationHours)
            throws SQLException {
        String sql = "INSERT INTO rooms (code, host_id, num_impostors, challenge_type, duration_hours, status) " +
                "VALUES (?, ?, ?, ?, ?, 'LOBBY')";
        for (int attempt = 0; attempt < ROOM_CODE_ATTEMPTS; attempt++) {
            try (Connection conn = DBConnection.getConnection()) {
                return insertRoom(conn, sql, RoomCodeGenerator.generate(), hostId,
                        numImpostors, challengeType, durationHours);
            } catch (SQLIntegrityConstraintViolationException duplicate) {
            }
        }
        throw new SQLException("No se pudo generar un codigo de sala unico, intenta de nuevo");
    }

    public RoomCreation createRoomWithHost(int hostId, String username, int numImpostors,
                                          String challengeType, int durationHours) throws SQLException {
        String sql = "INSERT INTO rooms (code, host_id, num_impostors, challenge_type, duration_hours, status) " +
                "VALUES (?, ?, ?, ?, ?, 'LOBBY')";
        for (int attempt = 0; attempt < ROOM_CODE_ATTEMPTS; attempt++) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    Room room;
                    try {
                        room = insertRoom(conn, sql, RoomCodeGenerator.generate(), hostId,
                                numImpostors, challengeType, durationHours);
                    } catch (SQLIntegrityConstraintViolationException duplicate) {
                        rollback(conn, duplicate);
                        continue;
                    }
                    Player hostPlayer = new PlayerDAO().addPlayer(conn, room.getId(), hostId, username);
                    conn.commit();
                    return new RoomCreation(room, hostPlayer);
                } catch (SQLException | RuntimeException e) {
                    rollback(conn, e);
                    throw e;
                }
            }
        }
        throw new SQLException("No se pudo generar un codigo de sala unico, intenta de nuevo");
    }

    public Room findByCode(String code) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findByCode(conn, code, false);
        }
    }

    public Room findByCodeForUpdate(Connection conn, String code) throws SQLException {
        return findByCode(conn, code, true);
    }

    public Room findById(int roomId) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean updateConfiguration(int roomId, int numImpostors, String challengeType, int durationHours)
            throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return updateConfiguration(conn, roomId, numImpostors, challengeType, durationHours);
        }
    }

    public boolean updateConfiguration(Connection conn, int roomId, int numImpostors,
                                       String challengeType, int durationHours) throws SQLException {
        String sql = "UPDATE rooms SET num_impostors = ?, challenge_type = ?, duration_hours = ? " +
                "WHERE id = ? AND status = 'LOBBY'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, numImpostors);
            stmt.setString(2, challengeType);
            stmt.setInt(3, durationHours);
            stmt.setInt(4, roomId);
            return stmt.executeUpdate() > 0;
        }
    }

    public void updateStatus(int roomId, String status) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            updateStatus(conn, roomId, status);
        }
    }

    public void updateStatus(Connection conn, int roomId, String status) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, roomId);
            stmt.executeUpdate();
        }
    }

    public boolean finishIfExpired(int roomId, Timestamp now) throws SQLException {
        String sql = "UPDATE rooms SET status = 'FINISHED' " +
                "WHERE id = ? AND status = 'IN_PROGRESS' " +
                "AND end_time IS NOT NULL AND end_time <= ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.setTimestamp(2, now);
            return stmt.executeUpdate() > 0;
        }
    }

    public void setEndTime(int roomId, Timestamp endTime) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            setEndTime(conn, roomId, endTime);
        }
    }

    public void setEndTime(Connection conn, int roomId, Timestamp endTime) throws SQLException {
        String sql = "UPDATE rooms SET end_time = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, endTime);
            stmt.setInt(2, roomId);
            stmt.executeUpdate();
        }
    }

    private Room findByCode(Connection conn, String code, boolean forUpdate) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE code = ?" + (forUpdate ? " FOR UPDATE" : "");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    private Room insertRoom(Connection conn, String sql, String code, int hostId, int numImpostors,
                            String challengeType, int durationHours) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, code);
            stmt.setInt(2, hostId);
            stmt.setInt(3, numImpostors);
            stmt.setString(4, challengeType);
            stmt.setInt(5, durationHours);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se pudo obtener el id de la sala");
                }
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

    private void rollback(Connection conn, Exception original) {
        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }
}
