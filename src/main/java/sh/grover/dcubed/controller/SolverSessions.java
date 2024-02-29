package sh.grover.dcubed.controller;

import sh.grover.dcubed.controller.solve.*;
import sh.grover.dcubed.controller.vision.ScannedSide;
import sh.grover.dcubed.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SolverSessions {

    private final ConcurrentHashMap<UUID, SolveSession> sessions = new ConcurrentHashMap<>();

    public ScanResult newSession(ScannedSide[] sides) {
        var session = new SolveSession();
        for (var side : sides) {
            session.addSide(side);
        }

        var sessionId = UUID.randomUUID();
        this.sessions.put(sessionId, session);
        return new ScanResult(sessionId, session.sides());
    }

    public ScanResult addPhoto(UUID sessionId, ScannedSide[] sides) throws IllegalArgumentException {
        var session = this.sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("session does not exist");
        }

        for (var side : sides) {
            session.addSide(side);
        }

        return new ScanResult(sessionId, session.sides());
    }

    public List<Move> solve(UUID sessionId) {
        var session = this.sessions.get(sessionId);
        var sides = session.sides();
        var cube = new Cube(
                new Side(sides[FaceColor.WHITE]),
                new Side(sides[FaceColor.RED]),
                new Side(sides[FaceColor.ORANGE]),
                new Side(sides[FaceColor.YELLOW]),
                new Side(sides[FaceColor.GREEN]),
                new Side(sides[FaceColor.BLUE])
        );

        var moves = new ArrayList<Move>(64);
        new WhiteCrossStep(cube, moves).solve();
        new WhiteCornersStep(cube, moves).solve();
        new SecondLayerStep(cube, moves).solve();
        new YellowCrossStep(cube, moves).solve();
        new YellowEdgesStep(cube, moves).solve();
        new PositionYellowCornersStep(cube, moves).solve();
        new OrientYellowCornersStep(cube, moves).solve();
        return moves;
    }
}