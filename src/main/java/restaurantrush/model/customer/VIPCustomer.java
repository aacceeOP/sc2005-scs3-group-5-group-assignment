/**
 * VIPCustomer.java
 * Stage: 2
 * Responsibility: Represent VIP satisfaction loss and bill discount.
 */
public class VIPCustomer extends Customer {
    @Override
    protected int getSatisfactionLossRate() {
        return 5; 
    }

    @Override
    public void placeOrder(Menu menu) {
        this.currentOrder = menu.getRandomItem(); 
    }

    @Override
    public double calculateBill() {
        return currentOrder.getPrice() * 0.90; 
    }
}
