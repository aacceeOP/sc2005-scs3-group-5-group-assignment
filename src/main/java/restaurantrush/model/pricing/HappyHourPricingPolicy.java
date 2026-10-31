// Stage 3 deferred: preserved for later integration.
// /**
//  * HappyHourPricingPolicy.java
//  * Stage: 3
//  * Responsibility: Apply the Happy Hour bill discount after the customer discount; lock the resulting price when the order is taken.
//  */
//
//
// package restaurantrush.model.pricing;
//
// /**
//  * Applies a 20% discount while Happy Hour is active.
//  * Activation is allowed only once per game.
//  */
// public class HappyHourPricingPolicy implements PricingPolicy {
//     private boolean activatedBefore;
//     private int remainingTurns;
//
//     public HappyHourPricingPolicy() {
//         this.activatedBefore = false;
//         this.remainingTurns = 0;
//     }
//
//     public void activate() {
//         if (activatedBefore) {
//             throw new IllegalStateException(
//                 "Happy Hour can only be activated once per game."
//             );
//         }
//
//         activatedBefore = true;
//         remainingTurns = 2;
//     }
//
//     public boolean isActive() {
//         return remainingTurns > 0;
//     }
//
//     public int getRemainingTurns() {
//         return remainingTurns;
//     }
//
//     /**
//      * Call once at the end of each turn.
//      * Activating at turn start gives discounts on that turn
//      * and the following turn.
//      */
//     public void advanceTurn() {
//         if (remainingTurns > 0) {
//             remainingTurns--;
//         }
//     }
//
//     @Override
//     public double getPolicyDiscountRate() {
//         return isActive() ? 0.20 : 0.0;
//     }
// }
