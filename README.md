# SC2005 SCS3 Group 5 Group Assignment

## Shared API requirements

The following interfaces are required by the table/staff module. Match method names, parameter order, types and behaviour; coordinate any changes with this module. All listed methods and constructors must be `public`. `void` means no return value.

Use the existing packages under `restaurantrush`: `model.customer.Customer`, `model.menu.Menu`, `model.menu.MenuItem`, `model.Order`, `model.Table`, and `model.pricing.PricingPolicy`.

## Customer - Person 1

| Signature | Return value / required behaviour |
| --- | --- |
| `boolean isWaitingForTable()` | True only when waiting for seating with no assigned table; otherwise false. |
| `void seatAt(Table table)` | Validate eligibility, assign a non-null table and set the state to seated. Reject an already-seated customer. Do not call `Table.seatCustomer()` back. |
| `void clearTable()` | Clear only the table reference; preserve customer state. Safe when already unassigned. Do not call `Table.releaseTable()` back. |
| `Table getTable()` | Assigned table, or null if none. Required for upcoming payment integration. |
| `boolean canOrder()` | True only when seated at an assigned table with no order placed; otherwise false. |
| `MenuItem chooseMenuItem(Menu menu)` | A non-null item from the supplied menu, chosen by the customer using a documented deterministic rule or seeded randomness. Reject null/unusable menus. Do not attach an order or change service state. |
| `void placeOrder(Order order)` | Attach the customer's order and set the waiting-for-food state. Reject null, another customer's order, duplicate orders or an ineligible customer. |
| `boolean canReceiveOrder(Order order)` | True only when seated, waiting for food, and the non-null order is the customer's assigned order. Do not require READY status; check customer eligibility and order identity only. |
| `void receiveOrder(Order order, int currentTurn)` | Validate eligibility and arguments, then set the served state and record the serving turn. Accept an order just marked SERVED. Do not update the order, release the table or collect payment. |

## Orders and pricing - Person 2

| Class and signature | Return value / required behaviour |
| --- | --- |
| `PricingPolicy.calculateBillCents(Customer customer, MenuItem item)` returning `int` | Final non-negative bill in integer cents. Apply customer discount before the active policy discount, using customer behaviour rather than type-check chains. Reject null arguments; do not change customers or existing orders. |
| `Order(Customer customer, MenuItem item, int billCents, int currentTurn)` | Constructor: create a PENDING order with zero completed preparation units; retain customer/item, lock the bill and record the turn. Reject null customer/item or negative bill. Do not attach or register the order. |
| `Customer Order.getCustomer()` | Non-null owning customer. Use this to validate order ownership. |
| `boolean Order.canServe(int currentTurn)` | True only when READY since an earlier turn; otherwise false. The preparation flow must record the turn the order becomes ready. |
| `void Order.markServed(int currentTurn)` | Validate serving eligibility, set SERVED and record the serving turn. Do not update the customer. |

Combo base prices are calculated before discounts. Apply the VIP discount before Happy Hour: a VIP Burger during Happy Hour costs 1080 cents. Lock the final bill when ordering; later policy changes must not affect it. Persons 1 and 2 must agree on the supporting customer-discount API; it is not specified here.

For serving, both eligibility checks occur before updates. After they pass, `receiveOrder()` must succeed when the same order has just been marked SERVED for the validated turn. Do not introduce a new readiness rejection or call the other object's update method back.

## Controller - Person 4

Use these staff entry points:

| Method | Return |
| --- | --- |
| `Waiter.seatCustomer(Customer customer, Table table)` | `void` |
| `Host.seatCustomer(Customer customer, Table table)` | `void` |
| `Waiter.takeOrder(Customer customer, Menu menu, PricingPolicy pricingPolicy, int currentTurn)` | Newly created `Order` |
| `Waiter.serveOrder(Order order, int currentTurn)` | `void` |

- Call `Staff.resetForNewTurn()` once per active staff member at turn start. Seating, ordering and serving share one Waiter allowance. Enable Host only in Stage 3 with a separate allowance.
- Validate restaurant membership of selected customers, tables and orders. Before seating, also verify queue membership; remove the customer only after success.
- For ordering, supply the current menu, non-null active pricing policy and current turn. Register the returned order exactly once, making it available to the Chef that turn. Do not attach it again or recalculate its bill.
- Run the Waiter before the Chef. A dish completed this turn cannot be served until a later turn. Do not repeat seating or serving state updates after successful staff calls.
- On validation failure, pass the message to the CLI and allow another choice without consuming or resetting the task allowance.
- Coordinate eating: served on T, ready to pay on T+1, payment eligible no earlier than T+2. Preserve satisfaction after serving; retain table occupancy until payment or departure.
- On abandonment, mark the customer departed, cancel any unfinished order, remove queue membership if present, and call `Table.releaseTable()` if seated. Preserve the departed state.

## Common rules

- Accessors and eligibility checks are read-only. Boolean checks return false when their stated eligibility conditions are unmet.
- For mutating operations, reject invalid arguments with `IllegalArgumentException` and disallowed states with `IllegalStateException`. Turn arguments must be at least 1. Use readable messages.
- Validate before changing state. Failures must not leave partial updates or consume staff tasks.
- Keep money in integer cents and console interaction in the CLI. Keep fields private and expose behaviour through methods.
- Match existing Java style: four-space indentation, opening braces on the declaration line, UpperCamelCase classes and lowerCamelCase methods/fields.

Payment APIs remain to be defined. Internal implementation choices belong to each module as long as these contracts are met.
