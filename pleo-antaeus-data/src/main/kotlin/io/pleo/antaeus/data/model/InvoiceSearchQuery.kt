package io.pleo.antaeus.data.model

import io.pleo.antaeus.models.InvoiceStatus
/*
 A data object that represents the fields that will be used to search and query the invoices.
 */
data class InvoiceSearchQuery(val status: InvoiceStatus)
