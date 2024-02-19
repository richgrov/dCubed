package sh.grover.dcubed.model;

import java.util.Map;
import java.util.UUID;

public record ScanResult(
        UUID sessionId,
        Map<FaceColor, FaceColor[]> sides
) {
}
