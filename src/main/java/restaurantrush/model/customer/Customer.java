package restaurantrush.model.customer;

import java.util.List;
import restaurantrush.model.Order;
import restaurantrush.model.Table;
import restaurantrush.model.enums.CustomerStatus;
import restaurantrush.model.enums.OrderStatus;
import restaurantrush.model.menu.Menu;
import restaurantrush.model.menu.MenuItem;

/**
 * Owns customer state, satisfaction, menu selection and the assigned table.
 * The controller owns turn timing, payment recording and abandonment cleanup.
 */
public abstract class Customer {
    private final int id;
    private int satisfaction = 100;
    private CustomerStatus state = CustomerStatus.QUEUED;
    private Order currentOrder;
    private Table table;
    private int servedTurn = -1;

    protected abstract int getSatisfactionLossRate();
    public abstract double getDiscountRate();
    public abstract MenuItem chooseMenuItem(Menu menu);

    public Customer(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Customer ID must be positive.");
        }
        this.id = id;
    }

    public int getId() { return id; }
    public int getCustomerId() { return id; }
    public int getSatisfaction() { return satisfaction; }
    public CustomerStatus getState() { return state; }
    public Order getOrder() { return currentOrder; }
    public Table getTable() { return table; }
    public int getServedTurn() { return servedTurn; }
    public int getWaitingLoss() { return getSatisfactionLossRate(); }

    public boolean isWaitingForTable() {
        return state == CustomerStatus.QUEUED && table == null;
    }

    public boolean isEligibleForSeating() { return isWaitingForTable(); }
    public boolean isEligibleForOrdering() { return canOrder(); }
    public boolean isReadyToPay() { return state == CustomerStatus.READY_TO_PAY; }

    /** Called by Table.seatCustomer; do not call back into Table here. */
    public void seatAt(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("Table must not be null.");
        }
        if (!isWaitingForTable() || !table.isAvailable()) {
            throw new IllegalStateException("Customer or table is not eligible for seating.");
        }
        this.table = table;
        state = CustomerStatus.SEATED;
    }

    /** Table owns releasing occupancy. Preserve PAID or ABANDONED state. */
    public void clearTable() {
        table = null;
    }

    public boolean canOrder() {
        return state == CustomerStatus.SEATED && table != null
            && table.getCustomer() == this && currentOrder == null;
    }

    public void placeOrder(Order order) {
        if (order == null || order.getCustomer() != this) {
            throw new IllegalArgumentException("Order must belong to this customer.");
        }
        if (!canOrder() || order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Customer cannot place this order.");
        }
        currentOrder = order;
        state = CustomerStatus.WAITING_FOR_FOOD;
    }

    /** Valid both before and after the waiter marks the order SERVED. */
    public boolean canReceiveOrder(Order order) {
        return state == CustomerStatus.WAITING_FOR_FOOD
            && table != null && table.getCustomer() == this
            && order != null && order == currentOrder
            && order.getCustomer() == this;
    }

    public void receiveOrder(Order order, int currentTurn) {
        if (currentTurn < 1) {
            throw new IllegalArgumentException("Turn number must be at least 1.");
        }
        if (!canReceiveOrder(order) || order.getStatus() != OrderStatus.SERVED
                || order.getServedTurn() != currentTurn) {
            throw new IllegalStateException("Customer can only receive their just-served order.");
        }
        servedTurn = currentTurn;
        state = CustomerStatus.SERVED;
    }

    public void receiveFood() {
        if (currentOrder == null) {
            throw new IllegalStateException("Customer has no order to receive.");
        }
        receiveOrder(currentOrder, currentOrder.getServedTurn());
    }

    /** Controller calls this no earlier than the turn after serving. */
    public void markReadyToPay() {
        if (state != CustomerStatus.SERVED) {
            throw new IllegalStateException("Customer must have been served.");
        }
        state = CustomerStatus.READY_TO_PAY;
    }

    public void finishEating() { markReadyToPay(); }

    /** Controller marks the order paid first; retain the table until release. */
    public void markPaid() {
        if (!isReadyToPay() || currentOrder == null
                || currentOrder.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Customer must be ready with a paid order.");
        }
        state = CustomerStatus.PAID;
    }

    public void completePayment() { markPaid(); }

    public boolean isWaitingForService() {
        return state == CustomerStatus.QUEUED || state == CustomerStatus.SEATED
            || state == CustomerStatus.WAITING_FOR_FOOD;
    }

    public void loseSatisfaction(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Satisfaction loss must not be negative.");
        }
        if (!isWaitingForService()) {
            return;
        }
        satisfaction = Math.max(0, satisfaction - amount);
        if (satisfaction == 0) {
            state = CustomerStatus.ABANDONED;
        }
    }

    public void processWaitingTurn() {
        loseSatisfaction(getWaitingLoss());
    }

    /** Controller separately cancels orders, removes queue membership and releases tables. */
    public void markDeparted() {
        if (state == CustomerStatus.ABANDONED) {
            return;
        }
        if (!isWaitingForService() || satisfaction > 0) {
            throw new IllegalStateException("Only an unsatisfied waiting customer can abandon.");
        }
        state = CustomerStatus.ABANDONED;
    }

    public void applyHappyHourBuff() {
        if (isWaitingForService()) {
            satisfaction = Math.min(100, satisfaction + 15);
        }
    }

    protected List<MenuItem> getAvailableMenuItems(Menu menu) {
        if (menu == null) {
            throw new IllegalArgumentException("Menu must not be null.");
        }
        List<MenuItem> items = menu.getItems();
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Menu must contain at least one item.");
        }
        return items;
    }
}
