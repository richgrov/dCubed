package sh.grover.dcubed

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class CubeImageAnalyzer : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        Log.d("TAG", image.imageInfo.toString())
        image.close()
    }
}