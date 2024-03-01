package sh.grover.dcubed.view;

import io.javalin.Javalin;
import io.javalin.config.SizeUnit;
import io.javalin.http.Context;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import sh.grover.dcubed.controller.SolverSessions;
import sh.grover.dcubed.controller.vision.IColorIdentifier;
import sh.grover.dcubed.model.ScanResult;
import sh.grover.dcubed.model.Side;
import sh.grover.dcubed.model.vision.ColorScanException;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class WebServer {

    private final SolverSessions solverSessions;
    private final IColorIdentifier colorIdentifier;

    public WebServer(SolverSessions solverSessions, IColorIdentifier colorIdentifier) {
        this.solverSessions = solverSessions;
        this.colorIdentifier = colorIdentifier;

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

        Mat image;
        try {
            image = this.imageFromStream(file.content(), Imgcodecs.IMREAD_UNCHANGED);
        } catch (IOException e) {
            e.printStackTrace();
            ctx.status(400).json("failed to read image");
            return;
        }

        Side[] sides;
        try {
            sides = this.colorIdentifier.estimateColors(image);
        } catch (ColorScanException e) {
            e.printStackTrace();
            ctx.status(422).json("failed to scan");
            return;
        }

        ScanResult scanResult;

        var sessionStr = ctx.queryParam("session");
        if (sessionStr == null) {
            scanResult = this.solverSessions.newSession(sides);
        } else {
            UUID session;
            try {
                session = UUID.fromString(sessionStr);
            } catch (IllegalArgumentException e) {
                ctx.status(400).json("invalid session");
                return;
            }

            scanResult = this.solverSessions.addPhoto(session, sides);
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

    private Mat imageFromStream(InputStream stream, int flags) throws IOException {
        var bytes = stream.readAllBytes();
        var mat = new Mat(1, bytes.length, CvType.CV_8UC1);
        mat.put(0, 0, bytes);

        var result = Imgcodecs.imdecode(mat, flags);
        if (result.empty()) {
            throw new IOException("failed to scan image");
        }
        return result;
    }
}