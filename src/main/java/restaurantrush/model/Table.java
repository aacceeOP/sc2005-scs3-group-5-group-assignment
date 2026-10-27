package restaurantrush.model;

import restaurantrush.model.customer.Customer;
import restaurantrush.model.enums.CustomerStatus;

/**
 * Table.java
 * Stage: 1
 * Responsibility: Track occupancy and release the table when its customer pays or abandons.
 */
public class Table {
    private final int tableId;
    private Customer customer;

    public Table(int tableId) {
        this.tableId = tableId;
        this.customer = null;
    }

    public int getTableId() {
        return tableId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public boolean isAvailable() {
        return customer == null;
    }

    /** Alias used by the controller when checking table occupancy. */
    public boolean isFree() {
        return isAvailable();
    }

    public void seatCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer must not be null.");
        }
        if (!isAvailable()) {
            throw new IllegalStateException("Table is already occupied.");
        }
        if (!customer.isWaitingForTable()) {
            throw new IllegalStateException(
                "Customer is not waiting for a table."
            );
        }

        customer.seatAt(this);
        this.customer = customer;
    }

    public void releaseTable() {
        if (customer == null) {
            return;
        }

        CustomerStatus state = customer.getState();
        if (state != CustomerStatus.PAID
                && state != CustomerStatus.ABANDONED) {
            throw new IllegalStateException(
                "Table can only be released after payment or abandonment."
            );
        }

        customer.clearTable();
        this.customer = null;
    }
}
