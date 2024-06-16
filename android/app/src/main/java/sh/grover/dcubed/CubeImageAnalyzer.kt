package sh.grover.dcubed

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.FloatBuffer
import java.util.Collections

class CubeImageAnalyzer(private val session: OrtSession) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        val bitmap = image.toBitmap()
        val modelInput = toModelInput(bitmap)
        OrtEnvironment.getEnvironment().use {
            val inputTensor = OnnxTensor.createTensor(it, modelInput, longArrayOf(1, 3, DIMENSION.toLong(), DIMENSION.toLong()))
            inputTensor.use {
                val outputTensor = session.run(Collections.singletonMap("images", inputTensor))
                outputTensor.use {
                    @Suppress("UNCHECKED_CAST")
                    val output = outputTensor.get(0).value as Array<Array<FloatArray>>
                    val values = output[0]
                    for (subArray in values) {
                            Log.d("MODEL OUTPUT", subArray.contentToString())
                    }
                }
            }
        }
        image.close()
    }

    companion object {
        private val DIMENSION = 640

        fun toModelInput(bitmap: Bitmap): FloatBuffer {
            val modelInput = FloatBuffer.allocate(3 * DIMENSION * DIMENSION)
            val stride = bitmap.width * bitmap.height
            for (x in 0 until bitmap.width) {
                for (y in 0 until 640) {
                    val pixel = if (y < bitmap.height) bitmap.getPixel(x, y) else 0 // TODO
                    val index = y * bitmap.width + x
                    val r = pixel shr 24
                    val g = (pixel shr 16) and 0xFF
                    val b = (pixel shr 8) and 0xFF
                    modelInput.put(index, r.div(255f))
                    modelInput.put(index + stride, g.div(255f))
                    modelInput.put(index + stride * 2, b.div(255f))
                }
            }
            return modelInput
        }
    }
}