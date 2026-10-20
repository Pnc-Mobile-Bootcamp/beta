package com.pnc.jetpackcomposedemos.features.orders.data

import com.pnc.jetpackcomposedemos.features.orders.domain.LineItem
import com.pnc.jetpackcomposedemos.features.orders.domain.Order
import java.time.LocalDateTime

object MyOrderMapper {

    fun mapToDomain(response: MyOrdersResponseDto, customerName: String): List<Order> {
        val firstName = customerName.substringBefore(' ')
        val lastName = customerName.substringAfter(' ', missingDelimiterValue = "")

        return response.orders.map { dto ->
            val orderDate = LocalDateTime.parse(dto.orderDate)
            Order(
                id = dto.orderNumber,
                customerId = response.customerId,
                firstName = firstName,
                lastName = lastName,
                orderDate = orderDate,
                // KNOWN LIMITATION (see Homework): Order.shipDate isn't nullable yet, so an order that
                // hasn't shipped shows its order date. The sample database has no unshipped orders.
                shipDate = dto.shipDate?.let { LocalDateTime.parse(it) } ?: orderDate,
                items = dto.lines.map { line ->
                    LineItem(
                        id = line.lineId,
                        productName = line.productName ?: "(unknown product)",
                        orderQty = line.quantity,
                        unitPrice = line.unitPrice,
                        lineTotal = line.lineTotal
                    )
                }
            )
        }
    }

}



