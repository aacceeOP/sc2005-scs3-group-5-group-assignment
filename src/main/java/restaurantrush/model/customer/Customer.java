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
    protected CustomerState state = CustomerState.QUEUED;
    protected Order currentOrder;
    
    protected abstract int getSatisfactionLossRate();
    public abstract double getDiscountRate();
    public abstract MenuItem chooseMenuItem(Menu menu); 

    public Customer(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public int getSatisfaction() { return satisfaction; }
    public CustomerState getState() { return state; }
    public Order getOrder() { return currentOrder; }

    public boolean isEligibleForSeating() { return state == CustomerState.QUEUED; }
    public boolean isEligibleForOrdering() { return state == CustomerState.SEATED; }
    public boolean isReadyToPay() { return state == CustomerState.READY_TO_PAY; }

    public void seatAtTable() {
        if (isEligibleForSeating()) this.state = CustomerState.SEATED;
    }

    public void placeOrder(Order order) {
        this.currentOrder = order;
        this.state = CustomerState.WAITING_FOR_FOOD;
    }

    public void receiveFood() {
        if (this.state == CustomerState.WAITING_FOR_FOOD) {
            this.state = CustomerState.SERVED;
        }
    }

    public void finishEating() {
        if (this.state == CustomerState.SERVED) {
            this.state = CustomerState.READY_TO_PAY;
        }
    }

    public void completePayment() {
        if (this.state == CustomerState.READY_TO_PAY) {
            this.state = CustomerState.PAID;
        }
    }

    public void processWaitingTurn() {
        if (state == CustomerState.QUEUED || state == CustomerState.SEATED || state == CustomerState.WAITING_FOR_FOOD) {
            satisfaction -= getSatisfactionLossRate();
            if (satisfaction <= 0) {
                satisfaction = 0; 
                state = CustomerState.ABANDONED; 
            }
        }
    }

    public void applyHappyHourBuff() {
        if (state == CustomerState.QUEUED || state == CustomerState.SEATED || state == CustomerState.WAITING_FOR_FOOD) {
            satisfaction = Math.min(100, satisfaction + 15);
        }
    }
}
