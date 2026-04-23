package com.trackharbor.trackharbor.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class Position {
    private String id;
    private String name;
    private String link;
    private boolean applied;
    private LocalDate dateApplied;
    private String status;
    private boolean aiTipsGenerated;
    private List<String> aiTips;
    private Instant createdAt;
    private Instant updatedAt;

    public Position() {}

    public Position(String id, String name, String link, boolean applied, LocalDate dateApplied,
                    String status, boolean aiTipsGenerated, List<String> aiTips,
                    Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.link = link;
        this.applied = applied;
        this.dateApplied = dateApplied;
        this.status = status;
        this.aiTipsGenerated = aiTipsGenerated;
        this.aiTips = aiTips;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public boolean isApplied() { return applied; }
    public void setApplied(boolean applied) { this.applied = applied; }

    public LocalDate getDateApplied() { return dateApplied; }
    public void setDateApplied(LocalDate dateApplied) { this.dateApplied = dateApplied; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isAiTipsGenerated() { return aiTipsGenerated; }
    public void setAiTipsGenerated(boolean aiTipsGenerated) { this.aiTipsGenerated = aiTipsGenerated; }

    public List<String> getAiTips() { return aiTips; }
    public void setAiTips(List<String> aiTips) { this.aiTips = aiTips; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}