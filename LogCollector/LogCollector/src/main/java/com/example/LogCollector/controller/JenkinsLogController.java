package com.example.LogCollector.controller;

import com.example.LogCollector.dto.JenkinsLogDTO;
import com.example.LogCollector.service.JenkinsLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jenkins-logs")
@CrossOrigin(origins = "*")
public class JenkinsLogController {

    @Autowired
    private JenkinsLogService logService;

    @Value("${webhook.secret-token}")
    private String webhookSecretToken;

    /**
     * ENDPOINT 1: Manual Collection
     * POST http://localhost:8081/api/jenkins-logs/collect/project5
     */
    @PostMapping("/collect/{jobName}")
    public ResponseEntity<?> collectLog(@PathVariable String jobName) {
        try {
            System.out.println("📌 Manual collection requested for job: " + jobName);
            JenkinsLogDTO log = logService.collectJobLog(jobName);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Logs collected and saved to database");
            response.put("data", log);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * ENDPOINT 2: Automatic Collection from Jenkins Webhook
     * Jenkins sends: POST /api/jenkins-logs/webhook?jobName=project5&buildNumber=10&buildStatus=SUCCESS&token=xxx
     * Uses collectAndSaveLogs method for efficient log collection with provided build info
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> webhookCollectLog(
            @RequestParam String jobName,
            @RequestParam(required = false) Integer buildNumber,
            @RequestParam(required = false) String buildStatus,
            @RequestParam String token) {
        try {
            if (!token.equals(webhookSecretToken)) {
                System.err.println("❌ Invalid webhook token received");
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("error", "Invalid webhook token");
                return ResponseEntity.status(403).body(error);
            }

            System.out.println("✓ Webhook received from Jenkins");
            System.out.println("   Job: " + jobName);
            System.out.println("   Build Number: " + buildNumber);
            System.out.println("   Build Status: " + buildStatus);

            JenkinsLogDTO log;
            if (buildNumber != null && buildStatus != null) {
                log = logService.collectAndSaveLogs(jobName, buildNumber, buildStatus);
            } else {
                log = logService.collectJobLog(jobName);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Logs automatically collected from Jenkins webhook");
            response.put("jobName", jobName);
            response.put("buildNumber", log.getBuildNumber());
            response.put("buildStatus", log.getBuildStatus());
            response.put("logNumber", log.getLogNumber());
            response.put("data", log);

            System.out.println("✓ Webhook processed successfully - Log #" + log.getLogNumber());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Webhook error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("error", "Webhook processing failed: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * ENDPOINT 3: Get Single Log by ID
     * GET http://localhost:8081/api/jenkins-logs/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLog(@PathVariable Long id) {
        try {
            JenkinsLogDTO log = logService.getLogById(id);

            if (log == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", log);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * ENDPOINT 4: Get ALL Logs for a Specific Job
     * GET http://localhost:8081/api/jenkins-logs/job/project5
     */
    @GetMapping("/job/{jobName}")
    public ResponseEntity<?> getLogsByJob(@PathVariable String jobName) {
        try {
            System.out.println("📊 Fetching all logs for job: " + jobName);
            List<JenkinsLogDTO> logs = logService.getLogsByJobName(jobName);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("jobName", jobName);
            response.put("totalLogs", logs.size());
            response.put("data", logs);

            Map<String, Long> statusSummary = new HashMap<>();
            for (JenkinsLogDTO log : logs) {
                String status = log.getBuildStatus();
                statusSummary.put(status, statusSummary.getOrDefault(status, 0L) + 1);
            }
            response.put("statusSummary", statusSummary);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * ENDPOINT 5: Get ALL Logs from ALL Jobs
     * GET http://localhost:8081/api/jenkins-logs/all
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllLogs() {
        try {
            System.out.println("📊 Fetching all logs from all jobs");
            List<JenkinsLogDTO> logs = logService.getAllLogs();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("totalLogs", logs.size());
            response.put("data", logs);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * ENDPOINT 6: Health Check
     * GET http://localhost:8081/api/jenkins-logs/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        try {
            List<JenkinsLogDTO> allLogs = logService.getAllLogs();

            Map<String, String> jobStatuses = new HashMap<>();
            Map<String, Object> jobDetails = new HashMap<>();

            for (JenkinsLogDTO log : allLogs) {
                String jobName = log.getJobName();
                if (!jobStatuses.containsKey(jobName)) {
                    jobStatuses.put(jobName, log.getBuildStatus());

                    Map<String, Object> details = new HashMap<>();
                    details.put("buildStatus", log.getBuildStatus());
                    details.put("buildNumber", log.getBuildNumber());
                    details.put("lastUpdated", log.getCreatedAt());
                    details.put("logNumber", log.getLogNumber());
                    jobDetails.put(jobName, details);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("service", "Jenkins Log Collector");
            response.put("totalJobs", jobStatuses.size());
            response.put("totalLogs", allLogs.size());
            response.put("jobStatuses", jobStatuses);
            response.put("jobDetails", jobDetails);
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "DOWN");
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * ENDPOINT 7: API Info
     * GET http://localhost:8081/api/jenkins-logs/info
     */
    @GetMapping("/info")
    public ResponseEntity<?> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "Jenkins Log Collector Microservice");
        info.put("version", "1.0.0");
        info.put("endpoints", new HashMap<String, String>() {{
            put("Manual Collection", "POST /api/jenkins-logs/collect/{jobName}");
            put("Auto Webhook", "POST /api/jenkins-logs/webhook?jobName={jobName}&buildNumber={num}&buildStatus={status}&token={token}");
            put("Get Log by ID", "GET /api/jenkins-logs/{id}");
            put("Get Job Logs", "GET /api/jenkins-logs/job/{jobName}");
            put("Get All Logs", "GET /api/jenkins-logs/all");
            put("Health Check", "GET /api/jenkins-logs/health");
            put("API Info", "GET /api/jenkins-logs/info");
        }});

        return ResponseEntity.ok(info);
    }

    /**
     * ENDPOINT 8: Get Console Log Only
     * GET http://localhost:8081/api/jenkins-logs/{id}/console
     * Returns only the raw console log text
     */
    @GetMapping("/{id}/console")
    public ResponseEntity<String> getConsoleLog(@PathVariable Long id) {
        try {
            String consoleLog = logService.getConsoleLogById(id);

            if (consoleLog == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body(consoleLog);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error retrieving console log: " + e.getMessage());
        }
    }




    /**
     * ENDPOINT 9: Get All Console Logs (Merged)
     * GET http://localhost:8081/api/jenkins-logs/all/console
     */
    @GetMapping("/all/console")
    public ResponseEntity<String> getAllConsoleLogs() {
        try {
            String allConsoles = logService.getAllConsoleLogs();
            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body(allConsoles);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("❌ Error retrieving all console logs: " + e.getMessage());
        }
    }

}
