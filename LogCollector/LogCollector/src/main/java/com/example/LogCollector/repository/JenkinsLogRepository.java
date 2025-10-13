package com.example.LogCollector.repository;

import com.example.LogCollector.entity.JenkinsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JenkinsLogRepository extends JpaRepository<JenkinsLog, Long> {

    @Query("SELECT COALESCE(MAX(j.logNumber), 0) FROM JenkinsLog j")
    Integer getMaxLogNumber();

    JenkinsLog findByJobNameAndBuildNumber(String jobName, Integer buildNumber);

    List<JenkinsLog> findByJobNameOrderByCreatedAtDesc(String jobName);
}
