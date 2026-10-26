/**
 * Customer.java
 * Stage: 1
 * Responsibility: Define customer states, satisfaction, abandonment eligibility, food selection and extensible customer behaviour.
 */
public abstract class Customer {
    protected int satisfaction = 100;
    protected CustomerState state = CustomerState.QUEUED;
    protected Order currentOrder;

    // Abstract methods force subclasses to define their own rules
    protected abstract int getSatisfactionLossRate();
    public abstract void placeOrder(Menu menu);
    public abstract double calculateBill();

    // Controller calls this at the end of every turn
    public void processWaitingTurn() {
        // Customers only lose satisfaction if waiting or seated and not served
        if (state == CustomerState.QUEUED || state == CustomerState.SEATED || state == CustomerState.WAITING_FOR_FOOD) {
            satisfaction -= getSatisfactionLossRate();
            if (satisfaction <= 0) {
                satisfaction = 0; // Minimum 0
                state = CustomerState.ABANDONED; // Trigger abandonment
            }
        }
    }

    // Stage 3 Happy Hour logic[cite: 1]
    public void applyHappyHourBuff() {
        if (state == CustomerState.QUEUED || state == CustomerState.SEATED || state == CustomerState.WAITING_FOR_FOOD) {
            satisfaction = Math.min(100, satisfaction + 15);
        }
    }

    // Standard Getters and Setters
    public int getSatisfaction() { return satisfaction; }
    public CustomerState getState() { return state; }
    public void setState(CustomerState state) { this.state = state; }
    public Order getOrder() { return currentOrder; }
}
