package io.pleo.antaeus.core.messaging

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.messaging.model.InvoiceMessage
import mu.KotlinLogging
import org.apache.commons.lang3.SerializationUtils

class InvoiceMessageConsumer(channel: Channel, private val billingService: BillingService) : DefaultConsumer(channel) {

    private val logger = KotlinLogging.logger {}

    override fun handleDelivery(consumerTag: String?,
                                envelope: Envelope?,
                                properties: AMQP.BasicProperties?,
                                body: ByteArray?) {
        val message: InvoiceMessage = SerializationUtils.deserialize(body)
        logger.info { "[$consumerTag] Received message: '$message'." }

        try {
            billingService.chargeCustomer(message.id)
        } catch (ex: Exception) {
            logger.warn(ex) { "[$consumerTag] Failed to consume message: '$message' due to: '${ex.message}'" }
        }
    }
}