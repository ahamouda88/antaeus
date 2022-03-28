package io.pleo.antaeus.models

data class InvoiceListResult(
    val result: List<Invoice>,
    val hasMore: Boolean
)
