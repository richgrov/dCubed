package sh.grover.dcubed.controller;

import org.opencv.core.Mat;
import sh.grover.dcubed.controller.vision.IColorIdentifier;
import sh.grover.dcubed.model.ScanResult;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SolverSessions {

    private final ConcurrentHashMap<UUID, SolveSession> sessions = new ConcurrentHashMap<>();
    private final IColorIdentifier colorIdentifier;

    public SolverSessions(IColorIdentifier colorIdentifier) {
        this.colorIdentifier = colorIdentifier;
    }

    public ScanResult newSession(Mat firstPhoto) {
        var sides = this.colorIdentifier.estimateColors(firstPhoto);
        var session = new SolveSession();
        for (var side : sides) {
            session.addSide(side);
        }

        var sessionId = UUID.randomUUID();
        this.sessions.put(sessionId, session);
        return new ScanResult(sessionId, session.sides());
    }

    public ScanResult addPhoto(UUID sessionId, Mat photo) throws IllegalArgumentException {
        var session = this.sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("session does not exist");
        }

        var sides = this.colorIdentifier.estimateColors(photo);
        for (var side : sides) {
            session.addSide(side);
        }

        return new ScanResult(sessionId, session.sides());
    }
}
