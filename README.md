# Restaurant Rush Stage 2

Java command-line restaurant management game for SC2002/SC2302, using numbered action menus.

- [Group repository](https://github.com/aacceeOP/sc2005-scs3-group-5-group-assignment)
- [Stage 1 snapshot](https://github.com/aacceeOP/sc2005-scs3-group-5-group-assignment/tree/stage-1)

## Stage 2 scope

The game runs all 18 turns with two tables, one Waiter, one Chef and one Cashier. Six customers arrive at the start of turns 1, 4, 7, 10, 13 and 16: Regular, VIP, Regular, Critic, Regular and Regular respectively.

| Menu item | Price | Preparation units |
| --- | --- | --- |
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |

Customers choose their own food. The manager chooses service priorities, not the food.

| Customer | Satisfaction lost per waiting turn | Discount | Selection rule |
| --- | --- | --- | --- |
| Regular | 8 | None | Seeded randomness, seed 12345; first choice is Burger |
| VIP | 5 | 10% | Seeded randomness, seed 67890; first choice is Burger |
| Critic | 12 | None | Most expensive item; first in menu order if prices tie. Currently Pasta |

Each Regular and VIP customer has its own seeded random generator. Prices are stored in cents and locked when an order is taken; a VIP Burger costs $13.50. All customer types use the same service and payment flow.

## Build and run

Install a JDK and make sure `java` and `javac` are available on PATH. Verified with JDK 26.0.2.1; other versions have not been verified. No network access or external Java libraries are required to play.

Open PowerShell in the repository root and compile:

```powershell
New-Item -ItemType Directory -Force build/classes | Out-Null
$javaSources = @(Get-ChildItem src/main/java -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
javac -d build/classes @javaSources
```

If compilation succeeds, start the game:

```powershell
java -cp build/classes restaurantrush.Main
```

Use an empty output directory for a clean build. Generated `.class` files should not be committed.

## Controls and turn rules

- Waiter: **1** seat a customer, **2** take an order, **3** serve an order, **4** pass. Enter the displayed customer or order ID when prompted.
- Chef: **1** prepare an order, **2** pass. Enter the order ID when prompted.
- The Waiter acts before the Chef. Each has at most one successful task per turn; the Chef prepares one unit.
- An order becoming READY this turn can be served next turn at the earliest.
- Served on turn T means READY_TO_PAY on T+1 and payment no earlier than T+2. The Cashier automatically processes at most one eligible payment per turn, oldest-ready first.
- Queued and seated customers who have not been served lose satisfaction at turn end according to their customer type. At zero they leave without paying, unfinished orders are cancelled, and their table is released.
- Invalid choices can be retried without consuming a task. Ending terminal input stops the unfinished game without declaring a result.

The display shows the queue, tables, customer types and satisfaction, orders and current staff choices. End-of-turn summaries show cumulative served, unhappy departure and paid counts, revenue, average satisfaction recorded at payment, and turns used. Before any payment, the average is N/A.

Victory after turn 18 requires all three targets:

- At least four paid customers.
- Revenue of at least $45.00.
- Average paid-customer satisfaction of at least 60.

Otherwise the result is defeat. There is no early ending. The turn-16 arrival cannot pay within 18 turns under the required timing.

## Stage 2 verification

Verified locally on 2 November 2026 with JDK 26.0.2.1. All Java sources compiled. Focused customer checks confirmed waiting losses of 8, 5 and 12, VIP pricing, and the Critic preference. Complete scripted CLI runs produced:

| Replay | Result | Served | Unhappy departures | Paid | Revenue | Average paid satisfaction | Turns |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Victory | VICTORY | 5 | 0 | 5 | $76.50 | 79.6 | 18 |
| Defeat | DEFEAT | 0 | 2 | 0 | $0.00 | N/A | 18 |

To reproduce the victory, start a fresh Java process and follow this schedule. Numbers in the action descriptions are customer IDs for seating/ordering and order IDs for serving/preparation.

| Turn | Waiter action | Chef action |
| --- | --- | --- |
| 1 | Seat 1 | Pass |
| 2 | Take order 1 | Prepare 1 |
| 3 | Serve 1 | Pass |
| 4 | Seat 2 | Pass |
| 5 | Take order 2 | Prepare 2 |
| 6 | Serve 2 | Pass |
| 7 | Seat 3 | Pass |
| 8 | Take order 3 | Prepare 3 |
| 9 | Serve 3 | Pass |
| 10 | Seat 4 | Pass |
| 11 | Take order 4 | Prepare 4 |
| 12 | Pass | Prepare 4 |
| 13 | Serve 4 | Pass |
| 14 | Seat 5 | Pass |
| 15 | Take order 5 | Prepare 5 |
| 16 | Serve 5 | Pass |
| 17 | Seat 6 | Pass |
| 18 | Take order 6 | Prepare 6 |

The Critic orders Pasta, requiring two preparation turns. Customer 6 has a READY order but is not served or paid by the end. These results depend on the documented arrival schedule, menu order and random seeds.

For the defeat run, start a fresh process and choose Waiter 4 (Pass) and Chef 2 (Pass) on every turn. Two customers abandon; later arrivals have not all reached zero satisfaction by turn 18.

These checks cover the stated scenarios, not every possible action sequence. The existing files under `docs/stage-1/` describe the Stage 1 snapshot and are not Stage 2 replay instructions.

## Changes from Stage 1

- Enabled VIPCustomer and CriticCustomer, keeping selection, waiting-loss and discount behaviour in customer subclasses.
- Added getCustomerType() to Customer and its subclasses for display labels.
- Updated Main to register the mixed arrival schedule, and GameCLI to show customer types and correctly format order details.
- Reused the existing staff, table, pricing and turn-flow implementations. The Stage 2 runs exercised the shared preparation, serving and payment flow with the new customer types.

The published Stage 1 snapshot remains available through the link above. Stage 2 has not yet been tagged; its class diagram will be added separately.
