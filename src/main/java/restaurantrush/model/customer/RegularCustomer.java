package restaurantrush.model.customer;

import restaurantrush.model.menu.Menu;

/**
 * RegularCustomer.java
 * Stage: 1
 * Responsibility: Represent regular customer satisfaction, pricing and food choice.
 */
public class RegularCustomer extends Customer {
    @Override
    protected int getSatisfactionLossRate() {
        return 8; 
    }

    @Override
    public void placeOrder(Menu menu) {
        this.currentOrder = menu.getRandomItem(); 
    }

    @Override
    public double calculateBill() {
        return currentOrder.getPrice(); 
    }
}
