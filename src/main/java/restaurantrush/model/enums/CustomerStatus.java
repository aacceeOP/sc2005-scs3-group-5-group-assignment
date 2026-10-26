package restaurantrush.model.enums;

/**
 * CustomerStatus.java
 * Stage: 1
 * Responsibility: Enumerate customer lifecycle states.
 */
public enum CustomerStatus {
    QUEUED,
    SEATED,
    WAITING_FOR_FOOD, 
    SERVED,           
    READY_TO_PAY,
    PAID,
    ABANDONED
}
