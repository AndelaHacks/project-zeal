package com.deedcorps.edward.project_zeal.mlservice

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.deedcorps.edward.project_zeal.R
import com.deedcorps.edward.project_zeal.api.Injection
import com.deedcorps.edward.project_zeal.api.model.Article
import com.deedcorps.edward.project_zeal.api.model.ZealResponse
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class TextDetectorActivity : AppCompatActivity(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_detector)

        val drawable = this.resources.getDrawable(R.drawable.test_pick, null)
        val bitmap = (drawable as BitmapDrawable).bitmap
        analyzeTextFromBitmap(bitmap)
    }

    private fun analyzeTextFromBitmap(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        textRecognizer.processImage(image)
            .addOnSuccessListener { result ->
                val resultText = result.text
                val textView = findViewById<TextView>(R.id.text)
                launch {
                    val zealResponse = getResponse(Article(content = resultText))
                    textView.text = zealResponse.content.score.toString()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
            }

    }

    private suspend fun getResponse(article: Article): ZealResponse {
        return withContext(Dispatchers.IO) {
            Injection.getZealResponse(article)
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

}
