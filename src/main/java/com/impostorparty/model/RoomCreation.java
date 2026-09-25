package com.impostorparty.model;

public class RoomCreation {
    private final Room room;
    private final Player hostPlayer;

    public RoomCreation(Room room, Player hostPlayer) {
        this.room = room;
        this.hostPlayer = hostPlayer;
    }

    public Room getRoom() {
        return room;
    }

    public Player getHostPlayer() {
        return hostPlayer;
    }
}
