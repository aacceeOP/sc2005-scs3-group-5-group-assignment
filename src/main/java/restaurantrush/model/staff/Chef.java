/**
 * Chef.java
 * Stage: 1
 * Responsibility: Advance an order by one preparation unit per turn.
 */


package restaurantrush.model.staff;

import restaurantrush.model.Order;

/**
 * Stage 1 responsibility:
 * Advance one order by one preparation unit per successful turn.
 */
public class Chef extends Staff {

    public Chef() {
        super();
    }

    /**
     * Advances the given order by one preparation unit.
     * The Chef can prepare an order on the same turn it is placed.
     *
     * @param order the order to prepare
     * @param currentTurn the current game turn, starting at 1
     */
    public void prepareOrder(Order order, int currentTurn) {
        if (!isAvailable()) {
            throw new IllegalStateException(
                "The chef has already worked this turn."
            );
        }

        if (order == null) {
            throw new IllegalArgumentException(
                "Order must not be null."
            );
        }

        if (currentTurn < 1) {
            throw new IllegalArgumentException(
                "Turn number must be at least 1."
            );
        }

        if (order.getStatus()
                != restaurantrush.model.enums.OrderStatus.PENDING) {
            throw new IllegalStateException(
                "Only pending orders can be prepared."
            );
        }

        // Validate first; only consume the Chef's task after success.
        order.advancePreparation(currentTurn);
        completeTask();
    }
}
