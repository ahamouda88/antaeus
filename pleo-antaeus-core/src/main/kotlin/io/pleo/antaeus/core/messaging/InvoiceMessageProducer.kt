package io.pleo.antaeus.core.messaging

import com.rabbitmq.client.Channel
import io.pleo.antaeus.core.utils.RetryUtils
import io.pleo.antaeus.core.messaging.model.InvoiceMessage
import io.pleo.antaeus.core.messaging.model.BILLING_EXCHANGE_NAME
import io.pleo.antaeus.core.messaging.model.BILLING_QUEUE_NAME
import mu.KotlinLogging
import org.apache.commons.lang3.SerializationUtils

class InvoiceMessageProducer(private val channel: Channel) {

    private val logger = KotlinLogging.logger {}
    private val retryUtils: RetryUtils = RetryUtils(name = "message_producer")

    fun publishMessage(invoiceMessage: InvoiceMessage) {
        logger.info { "Publishing message: '$invoiceMessage'." }
        retryUtils.retry {
            channel.basicPublish(
                    BILLING_EXCHANGE_NAME,
                    BILLING_QUEUE_NAME,
                    null,
                    SerializationUtils.serialize(invoiceMessage)
            )
        }
    }
}