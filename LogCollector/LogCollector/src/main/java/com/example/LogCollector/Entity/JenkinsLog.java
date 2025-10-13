package com.example.LogCollector.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jenkins_log")
public class JenkinsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_number")
    private Integer logNumber;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "build_number")
    private Integer buildNumber;

    @Column(columnDefinition = "TEXT")
    private String fullLog;

    @Column(columnDefinition = "TEXT")
    private String extractedErrors;

    @Column(name = "build_status")
    private String buildStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public JenkinsLog() {}

    public JenkinsLog(String jobName, Integer buildNumber, String fullLog,
                      String extractedErrors, String buildStatus) {
        this.jobName = jobName;
        this.buildNumber = buildNumber;
        this.fullLog = fullLog;
        this.extractedErrors = extractedErrors;
        this.buildStatus = buildStatus;
        this.createdAt = LocalDateTime.now();
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

    public String getFullLog() { return fullLog; }
    public void setFullLog(String fullLog) { this.fullLog = fullLog; }

    public String getExtractedErrors() { return extractedErrors; }
    public void setExtractedErrors(String extractedErrors) { this.extractedErrors = extractedErrors; }

    public String getBuildStatus() { return buildStatus; }
    public void setBuildStatus(String buildStatus) { this.buildStatus = buildStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
