package restaurantrush.model.staff;

import restaurantrush.model.Table;
import restaurantrush.model.customer.Customer;

/**
 * Host.java
 * Stage: 3
 * Responsibility: Seat at most one customer per turn,
 * independently of the waiter's task.
 */
public class Host extends Staff {

    public Host() {
        super();
    }

    public void seatCustomer(Customer customer, Table table) {
        if (!isAvailable()) {
            throw new IllegalStateException(
                "This host has already acted this turn."
            );
        }

        if (table == null) {
            throw new IllegalArgumentException(
                "Table must not be null."
            );
        }

        table.seatCustomer(customer);
        completeTask();
    }
}
