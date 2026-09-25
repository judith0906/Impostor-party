package com.impostorparty.dao;

import com.impostorparty.model.TaskContent;
import com.impostorparty.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ContentDAO {

    public List<String> getRandomWords(String challengeType, int count) throws SQLException {
        List<String> words = getWords(challengeType);
        Collections.shuffle(words, new Random());
        if (words.size() <= count) {
            return words;
        }
        return new ArrayList<>(words.subList(0, count));
    }

    public List<String> getWords(String challengeType) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return getWords(conn, challengeType);
        }
    }

    public List<String> getWords(Connection conn, String challengeType) throws SQLException {
        List<String> words = new ArrayList<>();
        String sql = "SELECT word FROM words WHERE challenge_type = ? ORDER BY id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, challengeType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    words.add(rs.getString("word"));
                }
            }
        }
        return words;
    }

    public List<TaskContent> getAllActiveTasks(String challengeType) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return getAllActiveTasks(conn, challengeType);
        }
    }

    public List<TaskContent> getAllActiveTasks(Connection conn, String challengeType) throws SQLException {
        List<TaskContent> tasks = new ArrayList<>();
        String sql = "SELECT id, description FROM tasks WHERE challenge_type = ? AND active = TRUE ORDER BY id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, challengeType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(new TaskContent(rs.getInt("id"), rs.getString("description")));
                }
            }
        }
        return tasks;
    }
}
