package restaurantrush.model.staff;

import restaurantrush.model.Order;
import restaurantrush.model.menu.Menu;
import restaurantrush.model.menu.MenuItem;
import restaurantrush.model.pricing.PricingPolicy;
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

    public Order takeOrder(
            Customer customer,
            Menu menu,
            PricingPolicy pricingPolicy,
            int currentTurn) {

        if (!isAvailable()) {
            throw new IllegalStateException(
                "This waiter has already acted this turn."
            );
        }

        if (customer == null || menu == null || pricingPolicy == null) {
            throw new IllegalArgumentException(
                "Customer, menu and pricing policy must not be null."
            );
        }

        if (currentTurn < 1) {
            throw new IllegalArgumentException(
                "Turn number must be at least 1."
            );
        }

        if (!customer.canOrder()) {
            throw new IllegalStateException(
                "Customer must be seated and must not already have an order."
            );
        }

        MenuItem item = customer.chooseMenuItem(menu);

        if (item == null) {
            throw new IllegalStateException(
                "Customer did not select a menu item."
            );
        }

        int billCents = pricingPolicy.calculateBillCents(customer, item);

        if (billCents < 0) {
            throw new IllegalStateException(
                "Calculated bill must not be negative."
            );
        }

        Order order = new Order(customer, item, billCents, currentTurn);
        customer.placeOrder(order);
        completeTask();

        return order;
    }
}
