# Restaurant Rush Stage 1

Java command-line restaurant management game for SC2002/SC2302, using numbered action menus.

- [Group repository](https://github.com/aacceeOP/sc2005-scs3-group-5-group-assignment)
- [Stage 1 snapshot](https://github.com/aacceeOP/sc2005-scs3-group-5-group-assignment/tree/stage-1)

## Stage 1 scope

The game runs all 18 turns with two tables, one Waiter, one Chef and one Cashier. Six Regular customers arrive at the start of turns 1, 4, 7, 10, 13 and 16.

| Menu item | Price | Preparation units |
| --- | --- | --- |
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |

Customers choose their own food using seeded randomness. Each Regular customer uses seed 12345; with the current menu order, their first choice is Burger. The manager chooses service priorities, not the food.

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
- Queued and seated customers who have not been served lose 8 satisfaction at turn end. At zero they leave without paying, unfinished orders are cancelled, and their table is released.
- Invalid choices can be retried without consuming a task. Ending terminal input stops the unfinished game without declaring a result.

The display shows the queue, tables, customer satisfaction, orders and current staff choices. End-of-turn summaries show cumulative served, unhappy departure and paid counts, revenue, average satisfaction recorded at payment, and turns used. Before any payment, the average is N/A.

Victory after turn 18 requires all three targets:

- At least four paid customers.
- Revenue of at least $45.00.
- Average paid-customer satisfaction of at least 60.

Otherwise the result is defeat. There is no early ending. The turn-16 arrival cannot pay within 18 turns under the required timing.

## Reproducible test runs

See [Stage 1 test notes](docs/stage-1/test-notes.md) for instructions, results and verification scope.

After compiling, run each replay in a fresh Java process:

```powershell
Get-Content docs/stage-1/victory-input.txt | java -cp build/classes restaurantrush.Main
Get-Content docs/stage-1/defeat-input.txt | java -cp build/classes restaurantrush.Main
```

| Replay | Result | Served | Unhappy departures | Paid | Revenue | Average paid satisfaction | Turns |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Victory | VICTORY | 6 | 0 | 5 | $75.00 | 84.0 | 18 |
| Defeat | DEFEAT | 0 | 2 | 0 | $0.00 | N/A | 18 |

The inputs depend on the documented food-selection rule and order IDs starting at 1 in a fresh process. The sixth customer is served but unpaid in the victory replay. Later arrivals have not all reached zero satisfaction in the defeat replay.

## Stage 1 class diagram

![Stage 1 class diagram](docs/diagrams/stage-1-class-diagram.png)

[Open the full-resolution diagram](docs/diagrams/stage-1-class-diagram.png).
