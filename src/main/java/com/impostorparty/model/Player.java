package com.impostorparty.model;

public class Player {
    private int id;
    private int roomId;
    private Integer userId;
    private String nickname;
    private String sessionToken;
    private Boolean isImpostor;
    private boolean ready;
    private String wordAssigned;

    public Player() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public Boolean getIsImpostor() { return isImpostor; }
    public void setIsImpostor(Boolean isImpostor) { this.isImpostor = isImpostor; }

    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }

    public String getWordAssigned() { return wordAssigned; }
    public void setWordAssigned(String wordAssigned) { this.wordAssigned = wordAssigned; }
}
