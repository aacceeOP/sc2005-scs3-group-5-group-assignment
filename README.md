# SC2005 SCS3 Group 5 Group Assignment

## Required Customer methods - Person 1

The customer module must provide these public methods in `restaurantrush.model.customer.Customer`. `Table` refers to `restaurantrush.model.Table`.

| Method | Expected return value | Required behaviour |
| --- | --- | --- |
| `public boolean isWaitingForTable()` | `true` when waiting for seating with no assigned table; otherwise `false`. | Check only; do not change state. |
| `public void seatAt(Table table)` | No return value (`void`). | Assign the table and change the customer state to seated. Reject null with `IllegalArgumentException`; reject an ineligible or already-seated customer with `IllegalStateException`. Validate before making changes. Do not call `Table.seatCustomer()` back. |
| `public void clearTable()` | No return value (`void`). | Clear the assigned table reference only, preserving the customer state. Safe when already unassigned. Do not call `Table.releaseTable()` back. |

Required accessor for upcoming staff/payment integration:

| Method | Expected return value | Required behaviour |
| --- | --- | --- |
| `public Table getTable()` | The assigned table, or `null` if none. | Read-only access; do not change state. |

## Required controller coordination - Person 4

Please coordinate the following for the table and staff module:

- At the start of each turn, call `Staff.resetForNewTurn()` once on each active staff member. It returns `void` and restores that staff member's task allowance. Do not reset staff between actions in the same turn.
- Before seating, verify that the selected customer belongs to the restaurant's waiting queue and that the selected table belongs to the restaurant.
- Call `Waiter.seatCustomer(Customer, Table)` or, in Stage 3, `Host.seatCustomer(Customer, Table)`. Both return `void`; normal completion means seating succeeded. Remove the customer from the queue only after success.
- If seating raises a validation exception, retain the customer in the queue and allow another choice. Pass the failure message to the CLI. Do not consume a staff task for an invalid choice.
- After successful seating, do not repeat the customer-state or table-assignment updates; only remove the customer from the waiting queue.
- Enable the Host only in Stage 3. Its task allowance is independent of the Waiter's, so each can act once in the same turn.
- Coordinate abandonment with the customer and order modules: mark the customer as departed, cancel the unfinished order if present, remove the customer from the waiting queue if present, and call `Table.releaseTable()` if they occupy a table. This call returns `void`; preserve the departed state.

These are coordination requirements, not new controller method signatures. The order-taking and serving API requirements are below; payment APIs will be agreed separately.

## Required order-taking APIs

The customer, order and pricing modules must provide the public signatures and behaviour below. Method names, parameter types and order, return types, and documented behaviour must match so the staff module can call them. Coordinate any contract change with the staff module.

Types use the existing packages: `Customer` in `restaurantrush.model.customer`, `Menu` and `MenuItem` in `restaurantrush.model.menu`, `Order` in `restaurantrush.model`, and `PricingPolicy` in `restaurantrush.model.pricing`.

### Customer module - Person 1

| Required signature | Return value | Required behaviour |
| --- | --- | --- |
| `public boolean canOrder()` | `true` only when seated at a table and no order has been placed; otherwise `false`. | Read-only eligibility check. |
| `public MenuItem chooseMenuItem(Menu menu)` | A non-null `MenuItem` available in the supplied menu. | The customer chooses using a documented deterministic rule or seeded randomness. Do not attach an order or change service state. Reject a null or unusable menu with `IllegalArgumentException`. Dispatch customer-type behaviour through customer objects. |
| `public void placeOrder(Order order)` | No return value (`void`). | Attach the order and update the customer to the appropriate waiting-for-food state. Reject null or an order belonging to a different customer with `IllegalArgumentException`; reject an ineligible customer or duplicate order with `IllegalStateException`. Validate before changing anything. |

### Orders and pricing module - Person 2

| Required signature | Return value | Required behaviour |
| --- | --- | --- |
| `public int calculateBillCents(Customer customer, MenuItem item)` on `PricingPolicy` | The final non-negative bill in integer cents. | Apply the customer discount first, then the active pricing-policy discount. Use customer behaviour rather than a chain of customer-type checks. Do not change customer state or existing orders. Reject null arguments with `IllegalArgumentException`. |
| `public Order(Customer customer, MenuItem item, int billCents, int currentTurn)` | Constructor: creates an `Order`; no return type. | Create a `PENDING` order, retain the customer and chosen item, lock the supplied bill, record the order-taking turn and start with zero completed preparation units. Reject null customer/item, negative bill, or a turn below 1 with `IllegalArgumentException`. Do not attach the order to the customer or register it with the restaurant. |

Standard pricing still applies any customer discount. Happy Hour pricing applies its additional 20% discount after that customer discount. A VIP Burger during Happy Hour must produce `1080` cents. Combo pricing is calculated by the menu module before customer and Happy Hour discounts; Salad + Pasta has a base combo price of `2430` cents. Locked order bills must not change when Happy Hour starts or ends.

The customer module and pricing module must agree how customer-specific discounts are exposed; that supporting method has not been specified yet. Use the required `Order.getCustomer()` accessor below to check order ownership; any other supporting accessors must be coordinated between those modules. Do not infer that the signatures above define every internal dependency.

### Controller coordination - Person 4

The controller must call the waiter using this signature:

```java
public Order takeOrder(
        Customer customer,
        Menu menu,
        PricingPolicy pricingPolicy,
        int currentTurn)
```

- Supply a valid restaurant customer, the current menu, the active non-null pricing policy and the current turn number (starting at 1).
- Register the returned `Order` with the restaurant/kitchen exactly once after successful completion. Do not create another order, attach it again, or recalculate its locked bill.
- On validation failure, allow another manager choice without consuming or resetting the staff task allowance. Forward the validation message to the CLI.
- Make the registered order available to the Chef in the same turn, after the Waiter's action.

### Interface requirements and coding conventions

- Keep the exact public method names, parameter order and return types above. Keep package declarations aligned with the existing directory structure.
- Match the existing Java style: four-space indentation, opening braces on the declaration line, `UpperCamelCase` class names and `lowerCamelCase` fields/methods. Keep fields private and expose behaviour through methods.
- Use `IllegalArgumentException` for invalid arguments and `IllegalStateException` for actions disallowed by the object's current state. Include a readable message for the CLI to display.
- Validate before modifying state. Failed actions must not partially update customer/order state or consume a staff task.
- `void` methods return nothing; successful completion indicates success. Do not replace the agreed contract with boolean success flags, strings or printed messages.
- Domain classes must not read console input or print menus/errors. The CLI handles interaction; methods return their agreed values or throw validation exceptions.
- Store bills as integer cents. Internal implementation choices remain with each module as long as these contracts are met.

## Required serving APIs

The following public signatures, return values and behaviour are required for staff integration.

### Order module - Person 2

| Required signature | Return value | Required behaviour |
| --- | --- | --- |
| `public Customer getCustomer()` | The owning `Customer`; non-null for a valid order. | Read-only accessor. |
| `public boolean canServe(int currentTurn)` | `true` only when the order is `READY` and its ready turn is earlier than `currentTurn`; otherwise `false`. | Read-only check. The Chef preparation flow must record the turn when the order first becomes ready. |
| `public void markServed(int currentTurn)` | No return value (`void`). | Reject a turn below 1 with `IllegalArgumentException` or an ineligible order with `IllegalStateException`, before changing state. Set the order to `SERVED` and record the serving turn. Do not update the customer. |

### Customer module - Person 1

| Required signature | Return value | Required behaviour |
| --- | --- | --- |
| `public boolean canReceiveOrder(Order order)` | `true` only when seated at an assigned table, waiting for food, and the non-null supplied order is the customer's assigned order; otherwise `false`. | Read-only check of customer eligibility and order identity. Do not require the order to remain `READY`; order readiness is checked separately. |
| `public void receiveOrder(Order order, int currentTurn)` | No return value (`void`). | Validate arguments and customer eligibility before changing state. Record the customer as served and store the serving turn for eating progression. Accept the customer's order after it has just been marked `SERVED`; do not require `READY`. Do not change the order, release the table, or collect payment. |

Serving checks both objects before updating them. `receiveOrder()` must succeed after `canReceiveOrder()` passes and the same order is marked served for the validated turn. It must not introduce a new failing readiness condition after the order update, which would leave the order and customer inconsistent. Neither update method should call the other object's update method back.

### Controller coordination - Person 4

- Call `public void serveOrder(Order order, int currentTurn)` with a registered restaurant order and the current turn number, starting at 1. There is no return value; normal completion means success.
- Run the Waiter's action before the Chef's action. An order finished this turn must not be served until a later turn.
- Treat seating, taking an order and serving as alternatives sharing one Waiter task allowance. Do not reset that allowance between actions.
- On invalid choices, pass the validation message to the CLI and allow another choice without consuming the task. Do not repeat the order or customer serving updates after success.
- Coordinate eating progression using the recorded serving turn: served on turn T, ready to pay on T+1, payment eligible no earlier than T+2. Preserve the customer's satisfaction after serving. Do not release the table until payment or abandonment.
