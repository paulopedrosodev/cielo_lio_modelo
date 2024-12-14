package cielo.sample.uriapp

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import cielo.orders.domain.ResultOrders
import cielo.sample.uriapp.adapter.ListOrderAdapter
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResponseGetOrdersActivity : AppCompatActivity() {
    private lateinit var logView: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_response_list_orders)

        val i = intent
        if (Intent.ACTION_VIEW == i.action) {
            val uri = i.data
            val response = uri?.getQueryParameter("response")
            val data = Base64.decode(response, Base64.DEFAULT)
            val json = String(data)

            if (json.contains("Limite de ordens")) {
                showLog(json)
            } else {
                showListOrders(json)
            }
        }
    }

    private fun showLog(json: String) {
        logView = findViewById(R.id.logLayoutListOrders)
        val tv = TextView(this)
        tv.text = json
        logView.addView(tv)
    }

    private fun showListOrders(json: String) {

        val resultOrders = Gson().fromJson(json, ResultOrders::class.java)

        if ((resultOrders != null) && (resultOrders.results.isNotEmpty())) {
            val orders = resultOrders.results

            CoroutineScope(Dispatchers.IO).launch {
                for (order in orders) {
                    withContext(Dispatchers.Main) {
                        MainActivity.orderList.add(order)
                    }
                }
            }

            val listOrdersAdapter = ListOrderAdapter(orders)
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_orders)
            recyclerView.adapter = listOrdersAdapter
        }
    }

}


