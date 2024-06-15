package sh.grover.dcubed

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.LifecycleCameraController
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import sh.grover.dcubed.ui.theme.DCubedTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        Camera()
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun Camera() {
    val lifecycle = LocalLifecycleOwner.current
    val camera = LifecycleCameraController(LocalContext.current)
    AndroidView(factory = { context ->
        PreviewView(context).also {
            it.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            it.scaleType = PreviewView.ScaleType.FILL_START
            it.controller = camera
            camera.bindToLifecycle(lifecycle)
        }
    })
}