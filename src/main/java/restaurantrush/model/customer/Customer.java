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
    private final int id;
    protected int satisfaction = 100;
    protected CustomerStatus state = CustomerStatus.QUEUED;
    protected Order currentOrder;
    
    protected abstract int getSatisfactionLossRate();
    public abstract double getDiscountRate();
    public abstract MenuItem chooseMenuItem(Menu menu); 

    public Customer(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public int getSatisfaction() { return satisfaction; }
    public CustomerStatus getState() { return state; }
    public Order getOrder() { return currentOrder; }

    public boolean isEligibleForSeating() { return state == CustomerStatus.QUEUED; }
    public boolean isEligibleForOrdering() { return state == CustomerStatus.SEATED; }
    public boolean isReadyToPay() { return state == CustomerStatus.READY_TO_PAY; }

    public void seatAtTable() {
        if (isEligibleForSeating()) this.state = CustomerStatus.SEATED;
    }

    public void placeOrder(Order order) {
        this.currentOrder = order;
        this.state = CustomerStatus.WAITING_FOR_FOOD;
    }

    public void receiveFood() {
        if (this.state == CustomerStatus.WAITING_FOR_FOOD) {
            this.state = CustomerStatus.SERVED;
        }
    }

    public void finishEating() {
        if (this.state == CustomerStatus.SERVED) {
            this.state = CustomerStatus.READY_TO_PAY;
        }
    }

    public void completePayment() {
        if (this.state == CustomerStatus.READY_TO_PAY) {
            this.state = CustomerStatus.PAID;
        }
    }

    public void processWaitingTurn() {
        if (state == CustomerStatus.QUEUED || state == CustomerStatus.SEATED || state == CustomerStatus.WAITING_FOR_FOOD) {
            satisfaction -= getSatisfactionLossRate();
            if (satisfaction <= 0) {
                satisfaction = 0; 
                state = CustomerStatus.ABANDONED; 
            }
        }
    }

    public void applyHappyHourBuff() {
        if (state == CustomerStatus.QUEUED || state == CustomerStatus.SEATED || state == CustomerStatus.WAITING_FOR_FOOD) {
            satisfaction = Math.min(100, satisfaction + 15);
        }
    }
}
