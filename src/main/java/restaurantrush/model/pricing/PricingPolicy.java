/**
 * PricingPolicy.java
 * Stage: 1
 * Responsibility: Define the pricing policy applied when an order is taken.
 */


package restaurantrush.model.pricing;

import restaurantrush.model.customer.Customer;
import restaurantrush.model.menu.MenuItem;

/**
 * Calculates an order's final price in integer cents.
 */
public interface PricingPolicy {

    /**
     * Returns the discount rate supplied by this pricing policy.
     * For example, 0.20 means 20% off.
     */
    double getPolicyDiscountRate();

    /**
     * Applies the customer discount first, then the policy discount.
     */
    default int calculateBillCents(Customer customer, MenuItem item) {
        if (customer == null || item == null) {
            throw new IllegalArgumentException(
                "Customer and menu item must not be null."
            );
        }

        double customerRate = customer.getDiscountRate();
        double policyRate = getPolicyDiscountRate();

        validateRate(customerRate, "Customer discount");
        validateRate(policyRate, "Policy discount");

        long afterCustomerDiscount = Math.round(
            item.getPriceCents() * (1.0 - customerRate)
        );

        long finalBill = Math.round(
            afterCustomerDiscount * (1.0 - policyRate)
        );

        if (finalBill < 0 || finalBill > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                "Calculated bill is outside the supported range."
            );
        }

        return (int) finalBill;
    }

    private static void validateRate(double rate, String label) {
        if (!Double.isFinite(rate) || rate < 0.0 || rate > 1.0) {
            throw new IllegalArgumentException(
                label + " must be between 0 and 1."
            );
        }
    }
}
