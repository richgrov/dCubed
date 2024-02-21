package sh.grover.dcubed.router;

import io.javalin.Javalin;
import io.javalin.config.SizeUnit;
import io.javalin.http.Context;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import sh.grover.dcubed.controller.SolverSessions;
import sh.grover.dcubed.model.ScanResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class WebServer {

    private final SolverSessions solverSessions;

    public WebServer(SolverSessions solverSessions) {
        this.solverSessions = solverSessions;

        Javalin.create(config -> config.jetty.multipartConfig.maxTotalRequestSize(1, SizeUnit.MB))
                .post("/scan-photo", this::scanPhoto)
                .post("/solve", this::solve)
                .start();
    }

    private void scanPhoto(Context ctx) {
        // TODO: rate limit
        if (true) {
            ctx.header("Access-Control-Allow-Origin", "*");
        }

        var file = ctx.uploadedFile("photo");
        if (file == null) {
            ctx.status(400).json("file required");
            return;
        }

        Mat img;
        try (var stream = file.content()) {
            img = readImage(stream, Imgcodecs.IMREAD_GRAYSCALE);
        } catch (IOException e) {
            e.printStackTrace(); // TODO
            return;
        }

        ScanResult scanResult;

        var sessionStr = ctx.queryParam("session");
        if (sessionStr == null) {
            scanResult = this.solverSessions.newSession(img);
        } else {
            UUID session;
            try {
                session = UUID.fromString(sessionStr);
            } catch (IllegalArgumentException e) {
                ctx.status(400).json("invalid session");
                return;
            }

            scanResult = this.solverSessions.addPhoto(session, img);
        }

        ctx.json(scanResult);
    }

    private void solve(Context ctx) {
        if (true) {
            ctx.header("Access-Control-Allow-Origin", "*");
        }

        var sessionStr = ctx.queryParam("session");
        if (sessionStr == null) {
            ctx.status(400).json("invalid session");
            return;
        }

        UUID session;
        try {
            session = UUID.fromString(sessionStr);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json("invalid session");
            return;
        }

        var solves = this.solverSessions.solve(session);
        ctx.json(solves);
    }

    private static Mat readImage(InputStream stream, int flags) throws IOException {
        var bytes = stream.readAllBytes();
        var mat = new Mat(1, bytes.length, CvType.CV_8UC1);
        mat.put(0, 0, bytes);
        return Imgcodecs.imdecode(mat, flags);
    }
}