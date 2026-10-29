package edtech;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public final class AgentService {
    private final InfraiErrorClient errors;

    public AgentService(InfraiErrorClient errors) { this.errors = errors; }

    public DeliveryReport deliver(Course course, List<Learner> learners, LocalDate today) {
        int late = 0;
        for (Learner learner : learners) {
            if (learner.deadline().isBefore(today) && !learner.completed()) {
                late++;
                errors.captureException("deadline_missed", learner.id() + " missed " + course.id());
            }
        }
        return new DeliveryReport(course.id(), learners.size(), late, late == 0 ? "on_track" : "follow_up");
    }

    public record Course(String id, String title) {}
    public record Learner(String id, LocalDate deadline, boolean completed) {}
    public record DeliveryReport(String courseId, int learners, int overdue, String status) {}

    public static class InfraiErrorClient {
        private final HttpClient http = HttpClient.newHttpClient();
        private final String baseUrl;
        private final String key;

        public InfraiErrorClient(String baseUrl, String key) {
            this.baseUrl = baseUrl;
            this.key = key;
        }

        public void captureException(String type, String message) {
            String payload = "{\"exception\":{\"type\":\"" + json(type) + "\",\"message\":\"" + json(message) + "\"}}";
            // Infrai error capture: POST /v1/errors/capture.
            request("POST", "/v1/errors/capture", payload);
        }

        private String request(String method, String path, String payload) {
            for (int attempt = 0; attempt < 3; attempt++) {
                try {
                    HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                            .timeout(Duration.ofSeconds(10)).header("Authorization", "Bearer " + key)
                            .header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(payload)).build();
                    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                    String body = response.body();
                    boolean ok = body.contains("\"ok\":true");
                    if (response.statusCode() == 429) {
                        try { Thread.sleep(250L * (1L << attempt)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                        if (!ok) continue;
                    }
                    if (!ok) {
                        throw new IllegalStateException("Infrai error: " + body);
                    }
                    return body;
                } catch (java.io.IOException | InterruptedException e) {
                    if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                    throw new IllegalStateException("Infrai transport error", e);
                }
            }
            throw new IllegalStateException("request attempts exhausted");
        }

        private static String json(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    }
}
