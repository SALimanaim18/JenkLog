package com.example.LogCollector.dto;

import java.time.LocalDateTime;

public class JenkinsLogDTO {

    private Long id;
    private Integer logNumber;
    private String jobName;
    private Integer buildNumber;
    private String extractedErrors;
    private String buildStatus;
    private LocalDateTime createdAt;

    // Constructor
    public JenkinsLogDTO() {}

    public JenkinsLogDTO(Long id, Integer logNumber, String jobName, Integer buildNumber,
                         String extractedErrors, String buildStatus, LocalDateTime createdAt) {
        this.id = id;
        this.logNumber = logNumber;
        this.jobName = jobName;
        this.buildNumber = buildNumber;
        this.extractedErrors = extractedErrors;
        this.buildStatus = buildStatus;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getLogNumber() { return logNumber; }
    public void setLogNumber(Integer logNumber) { this.logNumber = logNumber; }

    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }

    public Integer getBuildNumber() { return buildNumber; }
    public void setBuildNumber(Integer buildNumber) { this.buildNumber = buildNumber; }

    public String getExtractedErrors() { return extractedErrors; }
    public void setExtractedErrors(String extractedErrors) { this.extractedErrors = extractedErrors; }

    public String getBuildStatus() { return buildStatus; }
    public void setBuildStatus(String buildStatus) { this.buildStatus = buildStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}