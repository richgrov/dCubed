package sh.grover.dcubed

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import org.opencv.android.OpenCVLoader
import sh.grover.dcubed.ui.theme.DCubedTheme
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private lateinit var ortSession: OrtSession;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!OpenCVLoader.initLocal()) {
            throw UnsatisfiedLinkError();
        }

        val ortEnv = OrtEnvironment.getEnvironment()
        ortSession = ortEnv.createSession(resources.openRawResource(R.raw.detector).readBytes())

        setContent {
            DCubedTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CameraPermission()
                }
            }
        }
    }

    @Composable
    private fun CameraPermission() {
        var cameraPermission by remember { mutableStateOf(hasPermission(Manifest.permission.CAMERA)) }
        val permissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                    isGranted -> cameraPermission = isGranted
            }

        if (!cameraPermission) {
            Column {
                Text("Camera permission not granted.")
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant permission")
                }
            }
            return
        }

        Camera(ortSession)
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun Camera(session: OrtSession) {
    val lifecycle = LocalLifecycleOwner.current
    val context = LocalContext.current

    var latestImage by remember {
        mutableStateOf(Bitmap.createBitmap(640, 640, Bitmap.Config.ARGB_8888))
    }

    LaunchedEffect(Unit) {
        val cameraFuture = ProcessCameraProvider.getInstance(context)
        cameraFuture.addListener({
            val camera = cameraFuture.get()
            val analyzer = CubeImageAnalyzer(session)

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.clearAnalyzer()
                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
                        val annotatedImage = analyzer.findBoxes(image.toBitmap())
                        latestImage = annotatedImage
                        image.close()
                    }
                }

            camera.unbind()
            camera.bindToLifecycle(lifecycle, CameraSelector.DEFAULT_BACK_CAMERA, analysis)
        }, ContextCompat.getMainExecutor(context))
    }

    Image(bitmap = latestImage.asImageBitmap(), contentDescription = "Camera View")
}