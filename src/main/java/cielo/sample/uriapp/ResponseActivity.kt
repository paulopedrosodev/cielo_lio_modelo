package cielo.sample.uriapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cielo.sample.uriapp.databinding.ActivityResponseBinding
import cielo.sample.uriapp.domain.Order

private const val TAG = "URIAPP_TEST"

class ResponseActivity : AppCompatActivity() {

    private val scheme by lazy { getString(R.string.intent_scheme) }
    private val responseHost by lazy { getString(R.string.intent_host) }
    private val callbackUrl by lazy { "$scheme://$responseHost" }


    private lateinit var binding: ActivityResponseBinding

    private val logView get() = binding.logCancelLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action != Intent.ACTION_VIEW) return finish()

        ActivityResponseBinding.inflate(layoutInflater).let {
            binding = it
            setContentView(it.root)
        }

        intent.deserializeQueryParameter("response", Order::class.java) { json, order ->
            Log.d(TAG, "Response: $json")
            logView.addText("JSON=$json")

            binding.cancelButton.setOnClickListener { cancelClick(order) }
            binding.cancelFailureBtn.setOnClickListener { cancelFailureClick(order) }
        }

    }

    private fun cancelFailureClick(order: Order) {
        try {
            if (order.payments.isNotEmpty()) {
                val cancelRequest = CancelRequest(
                    order.id,
                    "rSAqNPGvFPJI",
                    "XZevoUYKmkVr",
                    order.payments[0].cieloCode,
                    order.payments[0].authCode,
                    order.payments[0].merchantCode,
                    order.payments[0].amount
                )

                val request = cancelRequest.serializeJsonBase64()
                val cancelUri = "lio://payment-reversal?request=$request&urlCallback=order://responseXXXX"
                Intent(Intent.ACTION_VIEW, Uri.parse(cancelUri)).let {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(it)
                }

            } else {
                Log.e(TAG, "ResponseActivity ERROR= it.payments=NULL")
                logView.addText("Não ha ordens para cancelar." + "\n" + "ResponseActivity ERROR= it.payments=NULL")
            }
        } catch (e: Exception) {
            Log.e(TAG, "ResponseActivity ERROR", e)
            logView.addText("ResponseActivity ERROR=" + e.toString() + " " + e.message)
        }

    }

    private fun cancelClick(order: Order) {
        try {
            if (order.payments.isNotEmpty()) {
                val cancelRequest = CancelRequest(
                    order.id, "rSAqNPGvFPJI",
                    "XZevoUYKmkVr", order.payments[0].cieloCode,
                    order.payments[0].authCode,
                    order.payments[0].merchantCode,
                    order.payments[0].amount
                )

                val request = cancelRequest.serializeJsonBase64()
                val cancelUri = "lio://payment-reversal?request=$request&urlCallback=$callbackUrl"
                Intent(Intent.ACTION_VIEW, Uri.parse(cancelUri)).let {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(it)
                }
            } else {
                Log.e(TAG, "ResponseActivity ERROR= it.payments=NULL")
                logView.addText("Não ha ordens para cancelar." + "\n" + "ResponseActivity ERROR= it.payments=NULL")
            }
        } catch (e: Exception) {
            Log.e(TAG, "ResponseActivity ERROR=" + e.toString() + " " + e.message)
            logView.addText("ResponseActivity ERROR=" + e.toString() + " " + e.message)
        }
    }
}
