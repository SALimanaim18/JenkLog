package com.example.LogCollector.dto;

import java.time.LocalDateTime;

public class BuildDTO {
    private Long id;
    private Integer buildNumber;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String triggeredBy;
    private Integer logCount;
    private Long pipelineId;
    private LocalDateTime createdAt;

    public BuildDTO() {}

    public BuildDTO(Long id, Integer buildNumber, String status, LocalDateTime startTime,
                    LocalDateTime endTime, Long duration, String triggeredBy, Integer logCount,
                    Long pipelineId, LocalDateTime createdAt) {
        this.id = id;
        this.buildNumber = buildNumber;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.triggeredBy = triggeredBy;
        this.logCount = logCount;
        this.pipelineId = pipelineId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getBuildNumber() { return buildNumber; }
    public void setBuildNumber(Integer buildNumber) { this.buildNumber = buildNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }

    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }

    public Integer getLogCount() { return logCount; }
    public void setLogCount(Integer logCount) { this.logCount = logCount; }

    public Long getPipelineId() { return pipelineId; }
    public void setPipelineId(Long pipelineId) { this.pipelineId = pipelineId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
