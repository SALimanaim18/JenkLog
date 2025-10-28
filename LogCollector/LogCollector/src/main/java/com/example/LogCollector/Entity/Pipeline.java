package com.example.LogCollector.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pipeline")
public class Pipeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "jenkins_url")
    private String jenkinsUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "pipeline", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Build> builds;

    public Pipeline() {}

    public Pipeline(String name, String jenkinsUrl) {
        this.name = name;
        this.jenkinsUrl = jenkinsUrl;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getJenkinsUrl() { return jenkinsUrl; }
    public void setJenkinsUrl(String jenkinsUrl) { this.jenkinsUrl = jenkinsUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Build> getBuilds() { return builds; }
    public void setBuilds(List<Build> builds) { this.builds = builds; }
}