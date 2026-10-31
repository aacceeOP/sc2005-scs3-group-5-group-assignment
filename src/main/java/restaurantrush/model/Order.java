/**
 * Order.java
 * Stage: 1
 * Responsibility: Track the chosen meal, locked bill, preparation units, order states, service timing and cancellation when a customer abandons.
 */


package restaurantrush.model;

import restaurantrush.model.customer.Customer;
import restaurantrush.model.enums.OrderStatus;
import restaurantrush.model.menu.MenuItem;

/**
 * Represents one customer's order.
 * The bill is locked when the order is created.
 */
public class Order {
    private static int nextOrderId = 1;
    private final int orderId;
    private final Customer customer;
    private final MenuItem item;
    private final int billCents;
    private final int orderTurn;

    private OrderStatus status;
    private int preparationProgress;
    private int readyTurn;
    private int servedTurn;

    public Order(
            Customer customer,
            MenuItem item,
            int billCents,
            int currentTurn) {

        if (customer == null || item == null) {
            throw new IllegalArgumentException(
                "Customer and menu item must not be null."
            );
        }
        if (billCents < 0) {
            throw new IllegalArgumentException(
                "Bill must not be negative."
            );
        }
        if (currentTurn < 1) {
            throw new IllegalArgumentException(
                "Turn number must be at least 1."
            );
        }

        this.orderId = nextOrderId++;
        this.customer = customer;
        this.item = item;
        this.billCents = billCents;
        this.orderTurn = currentTurn;

        this.status = OrderStatus.PENDING;
        this.preparationProgress = 0;
        this.readyTurn = -1;
        this.servedTurn = -1;
    }

    public int getOrderId() {
        return orderId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public MenuItem getItem() {
        return item;
    }

    public int getBillCents() {
        return billCents;
    }

    public int getOrderTurn() {
        return orderTurn;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getPreparationProgress() {
        return preparationProgress;
    }

    public int getReadyTurn() {
        return readyTurn;
    }

    public int getServedTurn() {
        return servedTurn;
    }

    /**
     * Advances preparation by one unit.
     * The Chef may call this once per successful task.
     */
    public void advancePreparation(int currentTurn) {
        validateTurn(currentTurn);

        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                "Only pending orders can be prepared."
            );
        }
        if (currentTurn < orderTurn) {
            throw new IllegalArgumentException(
                "Preparation cannot occur before the order is placed."
            );
        }

        preparationProgress++;

        if (preparationProgress >= item.getPreparationUnits()) {
            preparationProgress = item.getPreparationUnits();
            status = OrderStatus.READY;
            readyTurn = currentTurn;
        }
    }

    /**
     * An order can only be served on a turn after it became ready.
     */
    public boolean canServe(int currentTurn) {
        if (currentTurn < 1) {
            return false;
        }

        return status == OrderStatus.READY
            && readyTurn >= 1
            && currentTurn > readyTurn;
    }

    /**
     * Marks the order served. Customer state is updated separately
     * by the controller after the Waiter successfully serves the order.
     */
    public void markServed(int currentTurn) {
        validateTurn(currentTurn);

        if (!canServe(currentTurn)) {
            throw new IllegalStateException(
                "Order must have been ready before this turn."
            );
        }

        status = OrderStatus.SERVED;
        servedTurn = currentTurn;
    }

    /**
     * Marks payment complete after the customer has had a full
     * turn to eat. The Cashier must also check customer eligibility.
     */
    public void markPaid(int currentTurn) {
        validateTurn(currentTurn);

        if (status != OrderStatus.SERVED) {
            throw new IllegalStateException(
                "Only a served order can be paid."
            );
        }
        if (currentTurn < servedTurn + 2) {
            throw new IllegalStateException(
                "Payment is allowed no earlier than two turns "
                    + "after serving."
            );
        }

        status = OrderStatus.PAID;
    }

    /**
     * Cancels an order when the customer abandons the restaurant.
     */
    public void cancel() {
        if (status == OrderStatus.PAID) {
            throw new IllegalStateException(
                "A paid order cannot be cancelled."
            );
        }
        if (status == OrderStatus.CANCELLED) {
            return;
        }

        status = OrderStatus.CANCELLED;
    }

    private void validateTurn(int currentTurn) {
        if (currentTurn < 1) {
            throw new IllegalArgumentException(
                "Turn number must be at least 1."
            );
        }
    }
}
