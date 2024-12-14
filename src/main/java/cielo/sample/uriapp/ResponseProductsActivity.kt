package cielo.sample.uriapp

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cielo.sample.uriapp.databinding.ActivityResponseProductsBinding

private const val TAG = "ResponseProducts"

class ResponseProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResponseProductsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityResponseProductsBinding.inflate(layoutInflater).let {
            binding = it
            setContentView(it.root)
        }

        val i = intent
        if (Intent.ACTION_VIEW == i.action) {
            val uri = i.data
            val response = uri?.getQueryParameter("response")
            val data = Base64.decode(response, Base64.DEFAULT)
            val json = String(data)
            Log.d(TAG, json)
            binding.jsonResponseProducts.text = json
        }
    }
}