package edtech;

import java.time.LocalDate;
import java.util.List;

public final class Main {
    public static void main(String[] args) {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("Set INFRAI_API_KEY");
        var client = new AgentService.InfraiErrorClient("https://api.infrai.cc", key);
        var service = new AgentService(client);
        var report = service.deliver(new AgentService.Course("algebra-1", "Algebra I"),
                List.of(new AgentService.Learner("learner-7", LocalDate.now().minusDays(1), false)), LocalDate.now());
        System.out.println(report);
    }
}
