package sh.grover.dcubed.controller.vision.segment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import sh.grover.dcubed.model.vision.ColorScanException;
import sh.grover.dcubed.model.vision.segment.CubeSegmentation;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpCubeSegmenter implements ICubeSegmenter, AutoCloseable {

    private final URI endpoint;
    private final HttpClient httpClient;
    private final ObjectMapper jsonMapper;

    public HttpCubeSegmenter(URI rootEndpoint) {
        this.endpoint = rootEndpoint.resolve("segment");
        this.httpClient = HttpClient.newHttpClient();
        this.jsonMapper = new ObjectMapper();
    }

    @Override
    public CubeSegmentation segment(Mat image) throws Exception {
        var buf = new MatOfByte();
        Imgcodecs.imencode(".jpeg", image, buf);
        var bytes = buf.toArray();

        var request = HttpRequest.newBuilder(this.endpoint)
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();

        var response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        switch (response.statusCode()) {
            case 200 -> {
                return this.jsonMapper.readValue(response.body(), CubeSegmentation.class);
            }

            case 422 -> throw new ColorScanException("endpoint failed to identify: " + response.body());
            default -> throw new IOException("server returned status " + response.statusCode() + ": " + response.body());
        }
    }

    @Override
    public void close() {
        this.httpClient.close();
    }
}
