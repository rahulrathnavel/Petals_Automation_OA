package com.example.cardetector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer
import java.util.Collections
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var tvStatus: TextView
    private lateinit var btnUpload: Button

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    private val targetSize = 640

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            processImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        tvStatus = findViewById(R.id.tvStatus)
        btnUpload = findViewById(R.id.btnUpload)

        initOnnx()

        btnUpload.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    private fun initOnnx() {
        try {
            ortEnv = OrtEnvironment.getEnvironment()
            val modelBytes = assets.open("yolo26n.onnx").readBytes()
            ortSession = ortEnv?.createSession(modelBytes, OrtSession.SessionOptions())
        } catch (e: Exception) {
            e.printStackTrace()
            tvStatus.text = "Failed to load model"
            tvStatus.visibility = View.VISIBLE
        }
    }

    private fun processImage(uri: Uri) {
        btnUpload.text = "Processing..."
        btnUpload.isEnabled = false
        tvStatus.visibility = View.GONE
        
        Thread {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                
                if (originalBitmap != null) {
                    val resultBitmap = runInference(originalBitmap)
                    runOnUiThread {
                        imageView.setImageBitmap(resultBitmap)
                        btnUpload.text = "Upload Another Image"
                        btnUpload.isEnabled = true
                    }
                } else {
                    runOnUiThread {
                        tvStatus.text = "Invalid Image"
                        tvStatus.visibility = View.VISIBLE
                        btnUpload.text = "Upload Another Image"
                        btnUpload.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    tvStatus.text = "Error processing image"
                    tvStatus.visibility = View.VISIBLE
                    btnUpload.text = "Upload Another Image"
                    btnUpload.isEnabled = true
                }
            }
        }.start()
    }

    private fun runInference(bitmap: Bitmap): Bitmap {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        
        // Letterbox resize
        val scale = min(targetSize / width, targetSize / height)
        val newW = (width * scale).toInt()
        val newH = (height * scale).toInt()
        
        val padX = (targetSize - newW) / 2
        val padY = (targetSize - newH) / 2

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        
        // Prepare float buffer
        val floatBuffer = FloatBuffer.allocate(1 * 3 * targetSize * targetSize)
        floatBuffer.rewind()
        
        // Fill buffer (RGB, 0-1)
        val pixels = IntArray(targetSize * targetSize)
        val paddedBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvasPad = Canvas(paddedBitmap)
        canvasPad.drawColor(Color.rgb(114, 114, 114)) // standard padding color
        canvasPad.drawBitmap(scaledBitmap, padX.toFloat(), padY.toFloat(), null)
        
        paddedBitmap.getPixels(pixels, 0, targetSize, 0, 0, targetSize, targetSize)
        
        // YOLO expects [1, 3, H, W] meaning CHW format
        for (c in 0 until 3) {
            for (i in 0 until targetSize * targetSize) {
                val color = pixels[i]
                val value = when (c) {
                    0 -> Color.red(color) / 255.0f
                    1 -> Color.green(color) / 255.0f
                    2 -> Color.blue(color) / 255.0f
                    else -> 0f
                }
                floatBuffer.put(value)
            }
        }
        floatBuffer.rewind()

        // Inference
        val inputName = ortSession?.inputNames?.iterator()?.next() ?: "images"
        val shape = longArrayOf(1, 3, targetSize.toLong(), targetSize.toLong())
        val tensor = OnnxTensor.createTensor(ortEnv, floatBuffer, shape)
        
        val result = ortSession?.run(Collections.singletonMap(inputName, tensor))
        val output = result?.get(0)?.value as Array<Array<FloatArray>> // shape [1, 300, 6]
        
        tensor.close()
        result.close()

        val detections = output[0] // [300, 6]
        
        val resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)
        val paintBox = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = max(2f, width / 200f)
        }
        val paintText = Paint().apply {
            color = Color.RED
            textSize = max(24f, width / 20f)
            style = Paint.Style.FILL
            isFakeBoldText = true
        }

        var carFound = false
        
        for (i in detections.indices) {
            val d = detections[i]
            val conf = d[4]
            val classId = d[5].toInt()
            
            // class 2 is "car" in COCO
            if (conf >= 0.25f && classId == 2) {
                carFound = true
                val x1 = (d[0] - padX) / scale
                val y1 = (d[1] - padY) / scale
                val x2 = (d[2] - padX) / scale
                val y2 = (d[3] - padY) / scale
                
                canvas.drawRect(x1, y1, x2, y2, paintBox)
                canvas.drawText("car %.2f".format(conf), x1, y1 - 10f, paintText)
            }
        }

        if (!carFound) {
            runOnUiThread {
                tvStatus.text = "No car detected"
                tvStatus.visibility = View.VISIBLE
            }
        }

        return resultBitmap
    }

    override fun onDestroy() {
        super.onDestroy()
        ortSession?.close()
        ortEnv?.close()
    }
}
