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

These are coordination requirements, not new controller method signatures. The order-taking API requirements are below; serving and payment APIs will be agreed separately.

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

The customer module and pricing module must agree how customer-specific discounts are exposed; that supporting method has not been specified yet. Likewise, any additional Order accessors needed to validate order ownership must be agreed between those modules. Do not infer that the signatures above define every internal dependency.

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
