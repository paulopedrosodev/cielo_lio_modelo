package cielo.sample.uriapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cielo.orders.domain.Order
import cielo.sample.uriapp.R

class ListOrderAdapter(private val orders: List<Order> = emptyList()) : RecyclerView.Adapter<ListOrderAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtReference: TextView
        val txtPrice: TextView

        init {
            txtReference = view.findViewById(R.id.txt_reference)
            txtPrice = view.findViewById(R.id.txt_price)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.order_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = orders.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val order = orders[position]
        viewHolder.txtReference.text = order.reference
        viewHolder.txtPrice.text = "R$ " + order.price.toString()
    }
}