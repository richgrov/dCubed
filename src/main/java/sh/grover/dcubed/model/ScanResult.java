package sh.grover.dcubed.model;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record ScanResult(
        UUID sessionId,
        Map<String, String[]> sides
) {
    public ScanResult(UUID sessionId, Side[] sides) {
        this(sessionId, toMap(sides));
    }

    private static Map<String, String[]> toMap(Side[] sides) {
        return IntStream.range(0, sides.length).boxed()
                .filter(index -> sides[index] != null)
                .collect(Collectors.toMap(
                        FaceColor::toString,
                        index -> IntStream.range(0, 8)
                                .mapToObj(faceIndex -> sides[index].face(faceIndex))
                                .map(FaceColor::toString)
                                .toArray(String[]::new)
                ));
    }
}