# SC2005 SCS3 Group 5 Group Assignment

## Required Customer methods

Please provide these public methods in `restaurantrush.model.customer.Customer`. `Table` refers to `restaurantrush.model.Table`.

| Method | Expected return value | Required behaviour |
| --- | --- | --- |
| `public boolean isWaitingForTable()` | `true` when waiting for seating with no assigned table; otherwise `false`. | Check only; do not change state. |
| `public void seatAt(Table table)` | No return value (`void`). | Assign the table and change the customer state to seated. Reject null with `IllegalArgumentException`; reject an ineligible or already-seated customer with `IllegalStateException`. Validate before making changes. Do not call `Table.seatCustomer()` back. |
| `public void clearTable()` | No return value (`void`). | Clear the assigned table reference only, preserving the customer state. Safe when already unassigned. Do not call `Table.releaseTable()` back. |

For upcoming staff/payment integration, the proposed accessor is:

| Method | Expected return value | Required behaviour |
| --- | --- | --- |
| `public Table getTable()` | The assigned table, or `null` if none. | Read-only access; do not change state. |
