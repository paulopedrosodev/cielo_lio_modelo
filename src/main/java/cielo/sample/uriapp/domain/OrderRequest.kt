package cielo.sample.uriapp.domain

data class CancelRequest(val id: String, val clientID: String, val accessToken: String, val cieloCode: String, val authCode: String, val merchantCode: String, val value: Long)

data class CheckoutRequest(val clientID: String, val accessToken: String, val value: Long,
                           val paymentCode: String?, val installments: Int, val email: String,
                           val merchantCode: String?, val reference: String, val items: MutableList<Item>)

data class ListRequest(val clientID: String, val accessToken: String, val pageSize: Int, val page: Int)

data class ProductsRequest(val clientID: String, val accessToken: String)

data class GetOrderRequest(
    val amount: Long = 0L,
    val authCode: String = "",
    val cieloCode: String = "",
    val orderId: String = "",
    val accessToken: String,
    val clientID: String
)