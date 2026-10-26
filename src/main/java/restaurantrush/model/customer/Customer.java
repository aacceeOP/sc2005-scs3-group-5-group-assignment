package restaurantrush.model.customer;

import restaurantrush.model.Order;
import restaurantrush.model.enums.CustomerStatus;
import restaurantrush.model.menu.Menu;

/**
 * Customer.java
 * Stage: 1
 * Responsibility: Define customer states, satisfaction, abandonment eligibility, food selection and extensible customer behaviour.
 */
public abstract class Customer {
    protected int satisfaction = 100;
    protected CustomerStatus state = CustomerStatus.QUEUED;
    protected Order currentOrder;

    protected abstract int getSatisfactionLossRate();
    public abstract void placeOrder(Menu menu);
    public abstract double calculateBill();

    public void processWaitingTurn() {
        if (state == CustomerStatus.QUEUED || state == CustomerStatus.SEATED || state == CustomerStatus.WAITING_FOR_FOOD) {
            satisfaction -= getSatisfactionLossRate();
            if (satisfaction <= 0) {
                satisfaction = 0; // Minimum 0
                state = CustomerStatus.ABANDONED; 
            }
        }
    }

    public void applyHappyHourBuff() {
        if (state == CustomerStatus.QUEUED || state == CustomerStatus.SEATED || state == CustomerStatus.WAITING_FOR_FOOD) {
            satisfaction = Math.min(100, satisfaction + 15);
        }
    }

    // Standard Getters and Setters
    public int getSatisfaction() { return satisfaction; }
    public CustomerStatus getState() { return state; }
    public void setState(CustomerStatus state) { this.state = state; }
    public Order getOrder() { return currentOrder; }
}
