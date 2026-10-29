package edtech;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class AgentServiceTest {
    public static void main(String[] args) {
        List<String> captured = new ArrayList<>();
        var fake = new AgentService.InfraiErrorClient("http://127.0.0.1", "test") {
            @Override public void captureException(String type, String message) { captured.add(type + ":" + message); }
        };
        var report = new AgentService(fake).deliver(new AgentService.Course("c", "Course"),
                List.of(new AgentService.Learner("l", LocalDate.of(2026, 9, 12), false)), LocalDate.of(2026, 9, 13));
        if (report.overdue() != 1 || !report.status().equals("follow_up") || captured.size() != 1) throw new AssertionError(report);
        System.out.println("deadline decision passed");
    }
}
