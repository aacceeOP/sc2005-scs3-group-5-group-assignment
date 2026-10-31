# Integration contracts

Reference for the shared module interfaces. Later-stage APIs in this document are deferred while their implementations are commented out.


## Table and staff integration requirements

Use the exact public signatures and behaviour below. `void` means no return value. Keep existing packages: Customer in `restaurantrush.model.customer`, Order/Table in `restaurantrush.model`, Menu/MenuItem in `restaurantrush.model.menu`, OrderStatus in `restaurantrush.model.enums`, and PricingPolicy in `restaurantrush.model.pricing`.

## Customer - Person 1

| Public signature | Return value / required behaviour |
| --- | --- |
| `boolean isWaitingForTable()` | True only when waiting for seating with no assigned table. |
| `void seatAt(Table table)` | Validate eligibility, assign a non-null table and set the state to seated. Do not call Table.seatCustomer back. |
| `void clearTable()` | Clear only the table reference; preserve the customer state. Safe when already unassigned. Do not call Table.releaseTable back. |
| `Table getTable()` | Assigned table, or null if none. |
| `boolean canOrder()` | True only when seated at an assigned table with no order placed. |
| `MenuItem chooseMenuItem(Menu menu)` | A non-null menu item chosen by a documented deterministic rule or seeded randomness. Reject null/unusable menus. Do not attach an order or change service state. |
| `void placeOrder(Order order)` | Attach the customer's order and set the waiting-for-food state. Reject null, wrong ownership, duplicate orders and ineligible customers. |
| `boolean canReceiveOrder(Order order)` | True only when seated, waiting for food, and the non-null order is the customer's assigned order. Check customer eligibility and identity, not whether the order is still READY. |
| `void receiveOrder(Order order, int currentTurn)` | Record the served state and turn. Accept an order just marked SERVED. Called by the controller, not the Waiter. Do not update the order, release the table or collect payment. |
| `double getDiscountRate()` | Finite discount fraction from 0 to 1; Regular/Critic 0.0, VIP 0.10. Required by the merged PricingPolicy. |
| `int getSatisfaction()` | Current satisfaction from 0 to 100, retained for the payment snapshot. |
| `void markPaid()` | Set PAID, keeping the table reference until release. Accept the order already being PAID. Do not update the order, revenue or table. |

## Orders and pricing - Person 2

| Public signature | Return value / required behaviour |
| --- | --- |
| `int PricingPolicy.calculateBillCents(Customer customer, MenuItem item)` | Non-negative bill in cents; apply customer discount before the policy discount. No state changes. |
| `Order(Customer customer, MenuItem item, int billCents, int currentTurn)` | Create a PENDING order with zero preparation progress, locked bill and order turn. Do not attach or register the order. |
| `Customer Order.getCustomer()` | Non-null owning customer. |
| `OrderStatus Order.getStatus()` | Current order status; used to reject unserved, cancelled or already-paid bills. |
| `int Order.getBillCents()` | Locked, non-negative bill in cents; never recalculate at payment. |
| `boolean Order.canServe(int currentTurn)` | True only when READY since an earlier turn. |
| `void Order.markServed(int currentTurn)` | Validate eligibility, set SERVED and record the turn; do not update the customer. |
| `void Order.markPaid(int currentTurn)` | Validate SERVED and payment timing, then set PAID. Called by the controller; do not update the customer, statistics or table. |

## Controller - Person 4

| Staff entry point | Return |
| --- | --- |
| `Waiter.seatCustomer(Customer customer, Table table)` | `void` |
| `Host.seatCustomer(Customer customer, Table table)` | `void` |
| `Waiter.takeOrder(Customer customer, Menu menu, PricingPolicy pricingPolicy, int currentTurn)` | New Order |
| `Waiter.serveOrder(Order order, int currentTurn)` | `void` |
| `Cashier.processPayment(Customer customer, Order order)` | Locked bill as integer cents |

- Reset each active staff member once at turn start. Waiter actions share one allowance; Host has a separate allowance and is enabled only in Stage 3.
- Validate restaurant and queue membership before seating. Remove queue membership only after success. `Table.isFree()` and `Table.isAvailable()` are equivalent occupancy checks.
- Register the order returned by takeOrder exactly once; do not attach it again or recalculate the bill. Make it available to the Chef in the same turn.
- Run the Waiter before the Chef. After successful serveOrder, call `customer.receiveOrder(order, currentTurn)` exactly once. This call must succeed after prior customer eligibility checks and the order transition to SERVED.
- Enforce eating/payment timing: served on T, READY_TO_PAY on T+1, eligible for payment no earlier than T+2. Preserve satisfaction after serving.
- Select the oldest eligible ready-to-pay customer automatically; use arrival order for ties. If none qualifies, do not call the Cashier. Its two-argument method cannot verify the current turn or choose from the queue.
- Before calling the Cashier, validate readiness timing, the restaurant order/table and payment recording. After it returns the locked amount, call `order.markPaid(currentTurn)`, `customer.markPaid()`, record revenue and satisfaction once in GameState, then release the table. Do not use a second GameStatistics ledger.
- On abandonment, mark departed, cancel unfinished orders, remove queue membership and release any occupied table. Apply waiting loss to both queued and seated unserved customers.
- Failed staff actions must leave the task allowance available. Display validation messages through the CLI and allow a retry; do not reset the allowance mid-turn.

## Common rules

- Accessors and eligibility checks are read-only. Validate before changing state.
- Use IllegalArgumentException for invalid arguments and IllegalStateException for disallowed states. Turn numbers start at 1.
- Sequential updates have no automatic rollback: once prechecks pass, customer/order/payment updates must not introduce conflicting checks that leave partial state.
- Keep money in integer cents, fields private and console interaction in the CLI. Use four-space indentation, UpperCamelCase classes and lowerCamelCase methods/fields.

Internal implementation choices remain with each module as long as these contracts are met.
