/**
 * RegularCustomer.java
 * Stage: 1
 * Responsibility: Represent regular customer satisfaction, pricing and food choice.
 */
public class RegularCustomer extends Customer {
    @Override
    protected int getSatisfactionLossRate() {
        return 8; // Loses 8 points per turn[cite: 1]
    }

    @Override
    public void placeOrder(Menu menu) {
        // Deterministic rule: Pick a random item using a seeded randomizer[cite: 1]
        this.currentOrder = menu.getRandomItem(); 
    }

    @Override
    public double calculateBill() {
        return currentOrder.getPrice(); // Full price[cite: 1]
    }
}
