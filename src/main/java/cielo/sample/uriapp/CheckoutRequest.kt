package cielo.sample.uriapp

import cielo.orders.domain.SubAcquirer
import cielo.sample.uriapp.domain.Item

data class CheckoutRequest(
    val clientID: String,
    val accessToken: String,
    val value: Long,
    val paymentCode: String?,
    val installments: Int,
    val email: String,
    val merchantCode: String?,
    val reference: String,
    val items: MutableList<Item>,
    val subAcquirer: SubAcquirer?
)