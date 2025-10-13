
package com.example.LogCollector.service;

import com.example.LogCollector.entity.JenkinsLog;
import com.example.LogCollector.dto.JenkinsLogDTO;
import com.example.LogCollector.repository.JenkinsLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JenkinsLogService {

    @Autowired
    private JenkinsLogRepository logRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${jenkins.url}")
    private String jenkinsUrl;

    @Value("${jenkins.username}")
    private String jenkinsUsername;

    @Value("${jenkins.api-key}")
    private String jenkinsApiKey;

    /**
     * Collect job logs from Jenkins and save to database
     * Called by POST /collect/{jobName} or /webhook-simple?jobName=xxx
     */
    public JenkinsLogDTO collectJobLog(String jobName) {
        try {
            System.out.println("🔄 Starting log collection for job: " + jobName);

            // Get job info (build number, status)
            String apiUrl = jenkinsUrl + "/job/" + jobName + "/lastBuild/api/json";
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
            String jobData = response.getBody();

            Integer buildNumber = extractBuildNumber(jobData);
            System.out.println("✓ Build Number: " + buildNumber);

            // Get console logs
            String consoleUrl = jenkinsUrl + "/job/" + jobName + "/lastBuild/consoleText";
            ResponseEntity<String> consoleResponse = restTemplate.exchange(consoleUrl, HttpMethod.GET, entity, String.class);
            String consoleLogs = consoleResponse.getBody();

            System.out.println("✓ Console logs retrieved, size: " + consoleLogs.length());

            // Extract errors and build status
            String extractedErrors = extractErrors(consoleLogs);
            String buildStatus = extractBuildStatus(consoleLogs);

            System.out.println("✓ Build Status: " + buildStatus);
            System.out.println("✓ Errors extracted: " + (extractedErrors.equals("No errors found") ? "None" : "Found"));

            // Create Jenkins Log entity
            JenkinsLog jenkinsLog = new JenkinsLog();
            jenkinsLog.setJobName(jobName);
            jenkinsLog.setBuildNumber(buildNumber);
            jenkinsLog.setFullLog(consoleLogs);
            jenkinsLog.setExtractedErrors(extractedErrors);
            jenkinsLog.setBuildStatus(buildStatus);
            jenkinsLog.setCreatedAt(LocalDateTime.now());

            // Auto-increment log number
            Integer maxLogNumber = logRepository.getMaxLogNumber();
            jenkinsLog.setLogNumber(maxLogNumber + 1);

            System.out.println("✓ Log Number: " + jenkinsLog.getLogNumber());

            // Save to database
            JenkinsLog savedLog = logRepository.save(jenkinsLog);

            System.out.println("✅ Log saved successfully with ID: " + savedLog.getId());

            return convertToDTO(savedLog);

        } catch (Exception e) {
            System.err.println("❌ Error collecting logs: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to collect Jenkins log: " + e.getMessage());
        }
    }

    /**
     * NEW METHOD: Collect logs with buildNumber and buildStatus already provided
     * More efficient - no need to query Jenkins API for build info
     */
    public JenkinsLogDTO collectAndSaveLogs(String jobName, Integer buildNumber, String buildStatus) {
        try {
            System.out.println("✓ Starting log collection for job: " + jobName);
            System.out.println("✓ Build Number: " + buildNumber);

            // Get console logs directly
            String consoleUrl = jenkinsUrl + "/job/" + jobName + "/" + buildNumber + "/consoleText";
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> consoleResponse = restTemplate.exchange(
                    consoleUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            String consoleLogs = consoleResponse.getBody();

            System.out.println("✓ Console logs retrieved, size: " + consoleLogs.length());

            // Extract build status from logs if not provided or null
            String finalBuildStatus = (buildStatus == null || buildStatus.equals("null"))
                    ? extractBuildStatus(consoleLogs)
                    : buildStatus;

            System.out.println("✓ Build Status: " + finalBuildStatus);

            // Extract errors
            String extractedErrors = extractErrors(consoleLogs);
            System.out.println("✓ Errors extracted: " + (extractedErrors.equals("No errors found") ? "None" : "Found"));

            // Create Jenkins Log entity
            JenkinsLog jenkinsLog = new JenkinsLog();
            jenkinsLog.setJobName(jobName);
            jenkinsLog.setBuildNumber(buildNumber);
            jenkinsLog.setFullLog(consoleLogs);
            jenkinsLog.setExtractedErrors(extractedErrors);
            jenkinsLog.setBuildStatus(finalBuildStatus);
            jenkinsLog.setCreatedAt(LocalDateTime.now());

            // Auto-increment log number
            Integer maxLogNumber = logRepository.getMaxLogNumber();
            jenkinsLog.setLogNumber(maxLogNumber + 1);

            System.out.println("✓ Log Number: " + jenkinsLog.getLogNumber());

            // Save to database
            JenkinsLog savedLog = logRepository.save(jenkinsLog);

            System.out.println("✓ Log saved successfully with ID: " + savedLog.getId());

            return convertToDTO(savedLog);

        } catch (Exception e) {
            System.err.println("❌ Error collecting logs: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to collect Jenkins log: " + e.getMessage());
        }
    }

    /**
     * Create HTTP headers with Jenkins authentication
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = jenkinsUsername + ":" + jenkinsApiKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }

    /**
     * Extract build number from Jenkins JSON response
     */
    private Integer extractBuildNumber(String jsonData) {
        Pattern pattern = Pattern.compile("\"number\":(\\d+)");
        Matcher matcher = pattern.matcher(jsonData);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    /**
     * Extract errors, warnings, and failures from console logs
     */
    private String extractErrors(String consoleLogs) {
        StringBuilder errors = new StringBuilder();
        String[] lines = consoleLogs.split("\n");

        for (String line : lines) {
            if (line.contains("[ERROR]") || line.contains("ERROR") ||
                    line.contains("FAILURE") || line.contains("Failed") ||
                    line.contains("[WARN]") || line.contains("Exception")) {
                errors.append(line).append("\n");
            }
        }

        return errors.length() > 0 ? errors.toString() : "No errors found";
    }

    /**
     * Extract build status from console logs
     */
    private String extractBuildStatus(String consoleLogs) {
        if (consoleLogs.contains("Finished: SUCCESS")) {
            return "SUCCESS";
        } else if (consoleLogs.contains("Finished: FAILURE")) {
            return "FAILURE";
        } else if (consoleLogs.contains("Finished: UNSTABLE")) {
            return "UNSTABLE";
        }
        return "UNKNOWN";
    }

    /**
     * Get log by ID
     */
    public JenkinsLogDTO getLogById(Long id) {
        return logRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Get all logs for a specific job (sorted by newest first)
     */
    public List<JenkinsLogDTO> getLogsByJobName(String jobName) {
        return logRepository.findByJobNameOrderByCreatedAtDesc(jobName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all logs from all jobs
     */
    public List<JenkinsLogDTO> getAllLogs() {
        return logRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get only the console log text by ID
     */
    public String getConsoleLogById(Long id) {
        return logRepository.findById(id)
                .map(JenkinsLog::getFullLog)
                .orElse(null);
    }

    /**
     * Convert JenkinsLog entity to DTO (exclude fullLog from response)
     */
    private JenkinsLogDTO convertToDTO(JenkinsLog log) {
        return new JenkinsLogDTO(
                log.getId(),
                log.getLogNumber(),
                log.getJobName(),
                log.getBuildNumber(),
                log.getExtractedErrors(),
                log.getBuildStatus(),
                log.getCreatedAt()
        );
    }


    public String getAllConsoleLogs() {
        List<JenkinsLog> allLogs = logRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // ترتيب من الأحدث للأقدم
                .collect(Collectors.toList());

        StringBuilder builder = new StringBuilder();

        for (JenkinsLog log : allLogs) {
            builder.append("==============================================\n");
            builder.append("🧩 Job Name: ").append(log.getJobName()).append("\n");
            builder.append("🆔 Log ID: ").append(log.getId()).append("\n");
            builder.append("📦 Build Number: ").append(log.getBuildNumber()).append("\n");
            builder.append("✅ Status: ").append(log.getBuildStatus()).append("\n");
            builder.append("📅 Created At: ").append(log.getCreatedAt()).append("\n");
            builder.append("==============================================\n\n");

            builder.append(log.getFullLog()).append("\n\n\n\n"); // 4 sauts de ligne
        }

        if (builder.length() == 0) {
            return "⚠️ No console logs found.";
        }

        return builder.toString();
    }




}
