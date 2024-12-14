package cielo.sample.uriapp.domain

import java.util.Date

/**
 * Orders resource represents a customer's request to purchase one or more items
 *
 * @property id the internal id of this order
 * @property price the total price of the order
 * @property paidAmount the paid amount of the order
 * @property pendingAmount the pending amount of the order
 * @property reference the reference of this order
 * @property number the control number of the order, used in a partner system
 * @property notes free typing notes for the order
 * @property status the status of the order
 * @property items the items list of the order
 * @property payments the list of payments made for the order
 * @property createdAt the creation date of this order
 * @property updatedAt the date when the order was updated in the system
 * @property releaseDate the date when the order status changed from DRAFT
 *
 */
class Order(var id: String, var price: Long, var paidAmount: Long, var pendingAmount: Long,
            var reference: String, var number: String, var notes: String, var status: Status,
            var items: MutableList<Item>, var payments: MutableList<Payment>, var createdAt: Date,
            var updatedAt: Date, var releaseDate: Date, var type: OrderType){



}