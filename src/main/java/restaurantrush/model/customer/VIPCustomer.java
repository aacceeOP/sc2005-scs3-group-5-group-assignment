/**
 * VIPCustomer.java
 * Stage: 2
 * Responsibility: Represent VIP satisfaction loss and bill discount.
 */
public class VIPCustomer extends Customer {
    @Override
    protected int getSatisfactionLossRate() {
        return 5; // More patient, loses 5 points[cite: 1]
    }

    @Override
    public void placeOrder(Menu menu) {
        this.currentOrder = menu.getRandomItem(); 
    }

    @Override
    public double calculateBill() {
        // VIPs get 10% off[cite: 1]
        return currentOrder.getPrice() * 0.90; 
    }
}
