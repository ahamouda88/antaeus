/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.data.model.InvoiceSearchQuery
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceListResult

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
        return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun updateInvoice(updatedInvoice: Invoice): Invoice {
        return dal.updateInvoice(updatedInvoice)
    }

    fun searchInvoicesPaginated(invoiceSearchQuery: InvoiceSearchQuery): InvoiceListResult {
        return dal.searchInvoicesPaginated(invoiceSearchQuery)
    }
}
