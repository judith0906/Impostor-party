package com.impostorparty.service;

import com.impostorparty.dao.ContentDAO;
import com.impostorparty.dao.PlayerDAO;
import com.impostorparty.dao.RoomDAO;
import com.impostorparty.model.Player;
import com.impostorparty.model.Room;
import com.impostorparty.model.TaskContent;
import com.impostorparty.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class GameService {
    private static final Set<String> CHALLENGE_TYPES = Set.of(
            "normales", "amigos", "picantes", "salseo", "extremos"
    );

    private final RoomDAO roomDAO;
    private final PlayerDAO playerDAO;
    private final ContentDAO contentDAO;

    public GameService() {
        this(new RoomDAO(), new PlayerDAO(), new ContentDAO());
    }

    public GameService(RoomDAO roomDAO, PlayerDAO playerDAO, ContentDAO contentDAO) {
        this.roomDAO = roomDAO;
        this.playerDAO = playerDAO;
        this.contentDAO = contentDAO;
    }

    public void startGame(int hostUserId, String code) throws GameStartException, SQLException {
        if (code == null || code.trim().isEmpty()) {
            throw new GameStartException(400, "Falta el codigo de sala");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Room room = roomDAO.findByCodeForUpdate(conn, code.trim().toUpperCase(Locale.ROOT));
                validateRoomAccess(room, hostUserId);

                int numImpostors = room.getNumImpostors();
                int durationHours = room.getDurationHours();
                validateConfiguration(numImpostors, durationHours, room.getChallengeType());

                List<Player> players = playerDAO.findByRoomId(conn, room.getId());
                validatePlayers(players, numImpostors, hostUserId);

                GameAssignmentPlanner.AssignmentPlan plan;
                try {
                    plan = GameAssignmentPlanner.createPlan(
                            durationHours,
                            numImpostors,
                            contentDAO.getWords(conn, room.getChallengeType()),
                            contentDAO.getAllActiveTasks(conn, room.getChallengeType())
                    );
                } catch (GameAssignmentPlanner.InsufficientContentException e) {
                    throw new GameStartException(409, e.getMessage());
                }

                List<Player> randomizedPlayers = new ArrayList<>(players);
                Collections.shuffle(randomizedPlayers);
                Map<Integer, Integer> assignmentIndexes = new HashMap<>();
                for (int index = 0; index < numImpostors; index++) {
                    assignmentIndexes.put(randomizedPlayers.get(index).getId(), index + 1);
                }

                for (Player player : players) {
                    Integer assignmentIndex = assignmentIndexes.get(player.getId());
                    boolean isImpostor = assignmentIndex != null;
                    int taskListIndex = isImpostor ? assignmentIndex : 0;
                    String word = isImpostor
                            ? plan.getImpostorWords().get(taskListIndex - 1)
                            : plan.getNormalWord();
                    List<TaskContent> tasks = plan.getTaskLists().get(taskListIndex);

                    playerDAO.updateImpostorAndWord(conn, player.getId(), isImpostor, word);
                    playerDAO.assignTasks(conn, player.getId(), tasks);
                }

                long endMillis = System.currentTimeMillis() + durationHours * 3600L * 1000L;
                roomDAO.setEndTime(conn, room.getId(), new Timestamp(endMillis));
                roomDAO.updateStatus(conn, room.getId(), "IN_PROGRESS");
                conn.commit();
            } catch (GameStartException | SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        }
    }

    private void validateRoomAccess(Room room, int hostUserId) throws GameStartException {
        if (room == null) {
            throw new GameStartException(404, "Sala no encontrada");
        }
        if (room.getHostId() != hostUserId) {
            throw new GameStartException(403, "Solo el host puede iniciar la partida");
        }
        if (!"LOBBY".equals(room.getStatus())) {
            throw new GameStartException(409, "La partida ya ha empezado o ha terminado");
        }
    }

    private void validateConfiguration(int numImpostors, int durationHours, String challengeType)
            throws GameStartException {
        if (numImpostors < 1 || numImpostors > 3) {
            throw new GameStartException(400, "El numero de impostores debe estar entre 1 y 3");
        }
        if (durationHours < 1 || durationHours > 12) {
            throw new GameStartException(400, "La duracion debe estar entre 1 y 12 horas");
        }
        if (!CHALLENGE_TYPES.contains(challengeType)) {
            throw new GameStartException(400, "Modalidad de reto invalida");
        }
    }

    private void validatePlayers(List<Player> players, int numImpostors, int hostUserId)
            throws GameStartException {
        if (players.size() - numImpostors < 1) {
            throw new GameStartException(400, "Debe haber al menos un jugador civil");
        }

        Player hostPlayer = null;
        int hostPlayerCount = 0;
        for (Player player : players) {
            if (Integer.valueOf(hostUserId).equals(player.getUserId())) {
                hostPlayer = player;
                hostPlayerCount++;
            }
        }
        if (hostPlayer == null || hostPlayerCount != 1) {
            throw new GameStartException(409, "El host debe estar incluido en los jugadores de la sala");
        }
        if (!hostPlayer.isReady()) {
            throw new GameStartException(409, "El host todavia no esta listo");
        }
        for (Player player : players) {
            if (!player.isReady()) {
                throw new GameStartException(409, "No todos los jugadores estan listos todavia");
            }
        }
    }

    private void rollback(Connection conn, Exception original) {
        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }
}
