/**
 * CriticCustomer.java
 * Stage: 2
 * Responsibility: Represent critic satisfaction loss and distinct order preference or service reaction.
 */
public class CriticCustomer extends Customer {
    @Override
    protected int getSatisfactionLossRate() {
        return 12; // Impatient, loses 12 points[cite: 1]
    }

    @Override
    public void placeOrder(Menu menu) {
        // Distinct behavior: Always orders the most expensive item[cite: 1]
        this.currentOrder = menu.getMostExpensiveItem(); 
    }

    @Override
    public double calculateBill() {
        return currentOrder.getPrice(); // Full price[cite: 1]
    }
}
