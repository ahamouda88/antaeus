package io.pleo.antaeus.core.messaging

import com.rabbitmq.client.Channel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.messaging.model.InvoiceMessage
import io.pleo.antaeus.core.messaging.model.BILLING_EXCHANGE_NAME
import io.pleo.antaeus.core.messaging.model.BILLING_QUEUE_NAME
import org.apache.commons.lang3.SerializationUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException

class InvoiceMessageProducerTest {

    private val channel: Channel = mockk(relaxed = true)
    private val invoiceMessageProducer: InvoiceMessageProducer = InvoiceMessageProducer(channel = channel)

    @Test
    fun `will send invoice message`() {
        // Setup
        val invoiceMessage = InvoiceMessage(id = 12)
        every {
            channel.basicPublish(
                    BILLING_EXCHANGE_NAME,
                    BILLING_QUEUE_NAME,
                    null,
                    SerializationUtils.serialize(invoiceMessage)
            )
        } throws IOException() andThen Unit

        // Act
        invoiceMessageProducer.publishMessage(invoiceMessage)

        // Verify
        verify(exactly = 2) { channel.basicPublish(any(), any(), any(), any()) }
    }

    @Test
    fun `will throw exception when publish message fails`() {
        // Setup
        val invoiceMessage = InvoiceMessage(id = 12)
        every {
            channel.basicPublish(
                    BILLING_EXCHANGE_NAME,
                    BILLING_QUEUE_NAME,
                    null,
                    SerializationUtils.serialize(invoiceMessage)
            )
        } throws NullPointerException()

        // Act & Verify
        assertThrows<NullPointerException> {
            invoiceMessageProducer.publishMessage(invoiceMessage)
        }
        verify { channel.basicPublish(any(), any(), any(), any()) }
    }
}