/**
 * CustomerStatus.java
 * Stage: 1
 * Responsibility: Enumerate customer lifecycle states.
 */
public enum CustomerStatus {
    QUEUED,
    SEATED,
    WAITING_FOR_FOOD, // Order taken, food not yet SERVED
    SERVED,           // Eating phase
    READY_TO_PAY,
    PAID,
    ABANDONED
}
