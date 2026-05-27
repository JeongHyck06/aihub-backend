package aihub.backend.compare;

import java.time.Instant;
import java.util.List;

public record CompareInsightResponse(
        String verdict,
        List<ScenarioInsight> byScenario,
        String model,
        Instant generatedAt,
        boolean cached
) {
    public record ScenarioInsight(
            String scenario,
            String winner,
            String reason
    ) {
    }
}
