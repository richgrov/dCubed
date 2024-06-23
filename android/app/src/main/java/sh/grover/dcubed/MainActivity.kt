package sh.grover.dcubed

import ai.onnxruntime.OrtEnvironment
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.opencv.android.OpenCVLoader
import sh.grover.dcubed.ui.theme.DCubedTheme
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private lateinit var imageAnalysis: ImageAnalysis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!OpenCVLoader.initLocal()) {
            throw UnsatisfiedLinkError();
        }

        val ortEnv = OrtEnvironment.getEnvironment()
        val session = ortEnv.createSession(resources.openRawResource(R.raw.detector).readBytes())

        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalysis.clearAnalyzer()
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), CubeImageAnalyzer(session))

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

        Camera(imageAnalysis)
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun Camera(analysis: ImageAnalysis) {
    val lifecycle = LocalLifecycleOwner.current

    AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
        val previewView = PreviewView(context).also {
            it.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            it.scaleType = PreviewView.ScaleType.FILL_START
            it.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }

        val cameraFuture = ProcessCameraProvider.getInstance(context)
        cameraFuture.addListener({
            val camera = cameraFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            camera.unbind()
            camera.bindToLifecycle(lifecycle, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(context))

        previewView
    })
}