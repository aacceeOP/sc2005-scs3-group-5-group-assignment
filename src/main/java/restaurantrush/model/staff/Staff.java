package restaurantrush.model.staff;

/**
 * Staff.java
 * Stage: 1
 * Responsibility: Track shared staff availability and one successful task per turn.
 */
public abstract class Staff {
    private boolean taskCompleted;

    protected Staff() {
        this.taskCompleted = false;
    }

    public boolean isAvailable() {
        return !taskCompleted;
    }

    protected void completeTask() {
        if (!isAvailable()) {
            throw new IllegalStateException(
                "This staff member has already acted this turn."
            );
        }

        taskCompleted = true;
    }

    public void resetForNewTurn() {
        taskCompleted = false;
    }
}
