package restaurantrush.model.staff;

import restaurantrush.model.Order;
import restaurantrush.model.Table;
import restaurantrush.model.customer.Customer;
import restaurantrush.model.enums.OrderStatus;

/**
 * Cashier.java
 * Stage: 1
 * Responsibility: Validate one payment per turn and return the locked bill.
 * The controller selects the oldest eligible customer, enforces payment timing,
 * records payment, updates customer/order states and releases the table.
 */
public class Cashier extends Staff {

    public Cashier() {
        super();
    }

    /**
     * The caller must verify READY_TO_PAY timing before calling this method.
     * Returns the amount in cents; does not update customer/order/payment records.
     */
    public int processPayment(Customer customer, Order order) {
        if (!isAvailable()) {
            throw new IllegalStateException(
                "This cashier has already acted this turn."
            );
        }

        if (customer == null || order == null) {
            throw new IllegalArgumentException(
                "Customer and order must not be null."
            );
        }

        if (order.getCustomer() != customer) {
            throw new IllegalArgumentException(
                "Order must belong to the selected customer."
            );
        }

        if (order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException(
                "Only a served, unpaid order can be charged."
            );
        }

        Table table = customer.getTable();
        if (table == null || table.getCustomer() != customer) {
            throw new IllegalStateException(
                "Customer must occupy their assigned table."
            );
        }

        int billCents = order.getBillCents();
        if (billCents < 0) {
            throw new IllegalStateException(
                "Order bill must not be negative."
            );
        }

        completeTask();
        return billCents;
    }
}
