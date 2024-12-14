package cielo.sample.uriapp

import android.content.Intent
import android.util.Base64
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.Gson

fun ViewGroup.addText(text: String) {
    TextView(this.context).let {
        it.text = text
        addView(it)
    }
}

fun String.encodeBase64(): String = Base64.encodeToString(toByteArray(), Base64.DEFAULT)

fun Any.serializeJsonBase64() = Gson().toJson(this).encodeBase64()

fun Intent.queryParameter(name: String) = data?.getQueryParameter(name)


fun String.decodeBase64() = Base64.decode(this, Base64.DEFAULT).let(::String)

fun <T> String.deserializeJson(type: Class<T>): T = Gson().fromJson(this, type)

fun <T> Intent.deserializeQueryParameter(name: String, type: Class<T>, block: (String, T) -> Unit) =
    queryParameter(name)
        ?.decodeBase64()
        ?.let { block(it, it.deserializeJson(type)) }