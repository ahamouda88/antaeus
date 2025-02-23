/*
    Implements the data access layer (DAL).
    The data access layer generates and executes requests to the database.

    See the `mappings` module for the conversions between database rows and Kotlin objects.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.data.model.InvoiceSearchQuery
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceListResult
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class AntaeusDal(private val db: Database) {
    fun fetchInvoice(id: Int): Invoice? {
        // transaction(db) runs the internal query as a new database transaction.
        return transaction(db) {
            // Returns the first invoice with matching id.
            InvoiceTable
                .select { InvoiceTable.id.eq(id) }
                .firstOrNull()
                ?.toInvoice()
        }
    }

    fun fetchInvoices(): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                .selectAll()
                .map { it.toInvoice() }
        }
    }

    fun searchInvoicesPaginated(invoiceSearchQuery: InvoiceSearchQuery): InvoiceListResult {
        with(invoiceSearchQuery) {
            val selectExpression: Op<Boolean> = InvoiceTable.status.eq(status.name)
            val invoices = transaction(db) {
                InvoiceTable.select { selectExpression }
                        .limit(n = limit, offset = offset)
                        .orderBy(InvoiceTable.id)
                        .map { it.toInvoice() }
            }
            val totalNumberOfRecords: Int = InvoiceTable.count(selectExpression = selectExpression)
            return InvoiceListResult(result = invoices, hasMore = totalNumberOfRecords > offset + invoices.size)
        }
    }

    fun updateInvoice(updatedInvoice: Invoice): Invoice {
        transaction(db) {
            // Update the invoice.
            InvoiceTable.update ( { InvoiceTable.id.eq(updatedInvoice.id) } ){
                it[this.value] = updatedInvoice.amount.value
                it[this.currency] = updatedInvoice.amount.currency.toString()
                it[this.status] = updatedInvoice.status.toString()
            }
        }
        return updatedInvoice
    }

    fun createInvoice(amount: Money, customer: Customer, status: InvoiceStatus = InvoiceStatus.PENDING): Invoice? {
        val id = transaction(db) {
            // Insert the invoice and returns its new id.
            InvoiceTable
                .insert {
                    it[this.value] = amount.value
                    it[this.currency] = amount.currency.toString()
                    it[this.status] = status.toString()
                    it[this.customerId] = customer.id
                } get InvoiceTable.id
        }
        return fetchInvoice(id)
    }

    fun fetchCustomer(id: Int): Customer? {
        return transaction(db) {
            CustomerTable
                .select { CustomerTable.id.eq(id) }
                .firstOrNull()
                ?.toCustomer()
        }
    }

    fun fetchCustomers(): List<Customer> {
        return transaction(db) {
            CustomerTable
                .selectAll()
                .map { it.toCustomer() }
        }
    }

    fun createCustomer(currency: Currency): Customer? {
        val id = transaction(db) {
            // Insert the customer and return its new id.
            CustomerTable.insert {
                it[this.currency] = currency.toString()
            } get CustomerTable.id
        }

        return fetchCustomer(id)
    }

    private fun <T: Table> T.count(selectExpression: Op<Boolean>): Int {
        return transaction(db) {
            this@count.select { selectExpression }.count()
        }
    }
}
