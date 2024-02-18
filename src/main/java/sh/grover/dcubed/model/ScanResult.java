package sh.grover.dcubed.model;

import java.util.List;
import java.util.UUID;

public record ScanResult(
        UUID sessionId,
        List<FaceColor> unscannedFaces
) {
}
