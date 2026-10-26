/**
 * StandardPricingPolicy.java
 * Stage: 1
 * Responsibility: Apply normal menu pricing after the customer discount.
 */


package restaurantrush.model.pricing;

/**
 * Standard pricing applies the customer's discount only.
 */
public class StandardPricingPolicy implements PricingPolicy {

    @Override
    public double getPolicyDiscountRate() {
        return 0.0;
    }
}
