// FILE: src/main/java/com/impostorparty/model/Room.java
package com.impostorparty.model;

import java.sql.Timestamp;

public class Room {
    private int id;
    private String code;
    private int hostId;
    private int numImpostors;
    private String challengeType;
    private Timestamp endTime;
    private int durationHours;
    private String status; // LOBBY, IN_PROGRESS, FINISHED

    public Room() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getHostId() { return hostId; }
    public void setHostId(int hostId) { this.hostId = hostId; }

    public int getNumImpostors() { return numImpostors; }
    public void setNumImpostors(int numImpostors) { this.numImpostors = numImpostors; }

    public String getChallengeType() { return challengeType; }
    public void setChallengeType(String challengeType) { this.challengeType = challengeType; }

    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}