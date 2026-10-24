/**
 * PricingPolicy.java
 * Stage: 1
 * Responsibility: Define the pricing policy applied when an order is taken.
 */


import java.util.Objects;

/**
 * Calculates an order's final price in integer cents.
 *
 * Customer discount is applied first, then the active policy discount.
 * This class does not modify the customer, menu item, or any order.
 */
public final class PricingPolicy {

    /**
     * Supplies the current additional discount, such as Happy Hour.
     */
    @FunctionalInterface
    public interface ActiveDiscount {
        double getDiscountRate();
    }

    private final ActiveDiscount activeDiscount;

    /**
     * Creates a pricing policy.
     *
     * @param activeDiscount source of the active policy discount;
     *                       use () -> 0.0 when no policy is active
     */
    public PricingPolicy(ActiveDiscount activeDiscount) {
        this.activeDiscount =
                Objects.requireNonNull(activeDiscount, "activeDiscount");
    }

    /**
     * Calculates the final bill in cents.
     *
     * @throws NullPointerException if customer or item is null
     * @throws IllegalArgumentException if a discount rate is invalid
     */
    public int calculateBillCents(Customer customer, MenuItem item) {
        Objects.requireNonNull(customer, "customer");
        Objects.requireNonNull(item, "item");

        double customerRate = customer.getDiscountRate();
        double policyRate = activeDiscount.getDiscountRate();

        validateDiscountRate(customerRate, "customer discount");
        validateDiscountRate(policyRate, "active policy discount");

        int basePrice = item.getPriceCents();

        if (basePrice < 0) {
            throw new IllegalArgumentException(
                    "Menu item price cannot be negative");
        }

        // Apply customer discount first, then active policy discount.
        long afterCustomerDiscount =
                Math.round(basePrice * (1.0 - customerRate));

        long finalBill =
                Math.round(afterCustomerDiscount * (1.0 - policyRate));

        if (finalBill < 0 || finalBill > Integer.MAX_VALUE) {
            throw new ArithmeticException("Bill is outside the valid range");
        }

        return (int) finalBill;
    }

    private static void validateDiscountRate(double rate, String name) {
        if (!Double.isFinite(rate) || rate < 0.0 || rate > 1.0) {
            throw new IllegalArgumentException(
                    name + " must be between 0.0 and 1.0");
        }
    }
}
