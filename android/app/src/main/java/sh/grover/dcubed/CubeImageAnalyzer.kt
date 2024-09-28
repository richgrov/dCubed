package sh.grover.dcubed

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfRect2d
import org.opencv.core.Rect
import org.opencv.core.Rect2d
import org.opencv.core.Scalar
import org.opencv.dnn.Dnn
import org.opencv.imgproc.Imgproc
import java.nio.FloatBuffer
import java.util.Collections

class CubeImageAnalyzer(private val session: OrtSession) {

    fun findBoxes(image: Bitmap): Bitmap {
        //val amat = toModelInput2(image)
        var result = image// = Bitmap.createBitmap(amat.width(), amat.height(), Bitmap.Config.ARGB_8888)
        //amat.convertTo(amat, CvType.CV_8UC3)
        //Utils.matToBitmap(amat, result)

        val modelInput = toModelInput(image)
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

                    val confidenceScores = values[4]
                    var highest = Float.MIN_VALUE
                    var highestIndex = -1
                    for (i in confidenceScores.indices) {
                        if (confidenceScores[i] > highest) {
                            highest = confidenceScores[i]
                            highestIndex = i
                        }
                    }
                    Log.d("BEST", "$highest - ${values[0][highestIndex]} ${values[1][highestIndex]} ${values[2][highestIndex]} ${values[3][highestIndex]}")

                    for (subArray in values) {
                        Log.d("MODEL OUTPUT", subArray.contentToString())
                    }

                    val boxes = Array(values[0].size) { Rect2d() }
                    for (i in 0 until values[0].size) {
                        boxes[i] = Rect2d(values[0][i].toDouble(),
                            values[1][i].toDouble(), values[2][i].toDouble(), values[3][i].toDouble()
                        )
                    }
                    val scores = MatOfFloat(*confidenceScores)
                    val indices = MatOfInt()
                    Dnn.NMSBoxes(MatOfRect2d(*boxes), scores, 0.5f, 0.5f, indices)

                    if (indices.size(0) > 0) {
                        val index = intArrayOf(0)
                        indices.get(0, 0, index)
                        val box = boxes[index[0]]
                        Log.d("BOXES", box.toString())

                        val img = Mat()
                        Utils.bitmapToMat(image, img)
                        Imgproc.rectangle(img, Rect(box.x.toInt(), box.y.toInt(), box.width.toInt(), box.height.toInt()), Scalar(0.0, 255.0, 0.0), 2)
                        result = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888)
                        Utils.matToBitmap(img, result)
                        /*val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        Imgcodecs.imwrite(File(path, "img.jpg").absolutePath, img)*/
                    }
                }
            }
        }

        return result
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
                    val r = (pixel shr 24) and 0xFF
                    val g = (pixel shr 16) and 0xFF
                    val b = (pixel shr 8) and 0xFF
                    modelInput.put(index, r.div(255f))
                    modelInput.put(index + stride, g.div(255f))
                    modelInput.put(index + stride * 2, b.div(255f))
                }
            }
            return modelInput
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun toModelInput2(bitmap: Bitmap): Mat {
            Log.d("FORMAT", bitmap.colorSpace.toString())
            val pixels = FloatArray(3 * DIMENSION * DIMENSION)

            for (x in 0 until bitmap.width) {
                for (y in 0 until 640) {
                    val pixel = if (y < bitmap.height) bitmap.getPixel(x, y) else 0 // TODO
                    val index = (y * bitmap.width + x) * 3
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = (pixel) and 0xFF
                    pixels[index] = r.toFloat()
                    pixels[index + 1] = g.toFloat()
                    pixels[index + 2] = b.toFloat()
                }
            }

            val mat = Mat(DIMENSION, DIMENSION, CvType.CV_32FC3)
            mat.put(0, 0, pixels)
            return mat
        }
    }
}