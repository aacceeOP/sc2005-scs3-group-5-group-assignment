package restaurantrush.model.staff;

import restaurantrush.model.Table;
import restaurantrush.model.customer.Customer;

/**
 * Waiter.java
 * Stage: 1
 * Responsibility: Handle seating, order taking and serving,
 * with at most one successful task per turn.
 */
public class Waiter extends Staff {

    public Waiter() {
        super();
    }

    public void seatCustomer(Customer customer, Table table) {
        if (!isAvailable()) {
            throw new IllegalStateException(
                "This waiter has already acted this turn."
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
