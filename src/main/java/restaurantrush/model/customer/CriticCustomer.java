/**
 * CriticCustomer.java
 * Stage: 2
 * Responsibility: Represent critic satisfaction loss and distinct order preference or service reaction.
 */
public class CriticCustomer extends Customer {
    @Override
    protected int getSatisfactionLossRate() {
        return 12; 
    }

    @Override
    public void placeOrder(Menu menu) {
        this.currentOrder = menu.getMostExpensiveItem(); 
    }

    @Override
    public double calculateBill() {
        return currentOrder.getPrice(); 
    }
}
