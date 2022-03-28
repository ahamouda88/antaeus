package io.pleo.antaeus.core.messaging

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Envelope
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.messaging.model.InvoiceMessage
import org.apache.commons.lang3.SerializationUtils
import org.junit.jupiter.api.Test

class InvoiceMessageConsumerTest {

    private val invoiceId: Int = 12
    private val channel: Channel = mockk(relaxed = true)
    private val billingService: BillingService = mockk(relaxed = true)

    private val invoiceMessageConsumer: InvoiceMessageConsumer = InvoiceMessageConsumer(channel = channel, billingService = billingService)

    @Test
    fun `will consume invoice message`() {
        // Setup
        val invoiceMessage = InvoiceMessage(id = invoiceId)

        // Act
        invoiceMessageConsumer.handleDelivery(
                consumerTag = "test",
                body = SerializationUtils.serialize(invoiceMessage),
                envelope = Envelope(1, false, "", ""),
                properties = AMQP.BasicProperties.Builder().build()
        )

        // Verify
        verify { billingService.chargeCustomer(invoiceId = invoiceId) }
    }

    @Test
    fun `will ignore message when charge customer fails`() {
        // Setup
        val invoiceMessage = InvoiceMessage(id = invoiceId)
        every { billingService.chargeCustomer(invoiceId = invoiceId) } throws InvoiceNotFoundException(id = invoiceId)

        // Act & Verify
        invoiceMessageConsumer.handleDelivery(
                consumerTag = "test",
                body = SerializationUtils.serialize(invoiceMessage),
                envelope = Envelope(1, false, "", ""),
                properties = AMQP.BasicProperties.Builder().build()
        )
        verify { billingService.chargeCustomer(invoiceId = invoiceId) }
    }
}