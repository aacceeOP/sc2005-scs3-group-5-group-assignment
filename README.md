# SC2005 SCS3 Group 5 Group Assignment

Restaurant Rush is a Java command-line restaurant management game for SC2002/SC2302. This version implements the Stage 1 game with numbered input. The repository name retains the group's original naming.

Repository: https://github.com/aacceeOP/sc2005-scs3-group-5-group-assignment

## Current status

The local Stage 1 implementation compiled and passed the checks recorded on 1 November 2026 in [test notes](docs/stage-1/test-notes.md). The release commit and `stage-1` tag have not been created by the assistant. Confirm their publication before sharing a tag link.

Only Regular customers, the base menu, Waiter, Chef and Cashier are active. VIP/Critic, Host, combos and Happy Hour code is preserved as comments for later integration. This reflects asynchronous team development. Confirm that this treatment meets the briefing's stage-content requirement before final tagging; disabled source does not certify compliance.

## Build and run

Requires a JDK with `java` and `javac` on PATH. Verified with JDK 26.0.2.1. The source uses Java 11-era APIs; other JDK versions have not been verified.

Open PowerShell in the repository root, then run:

```powershell
New-Item -ItemType Directory -Force build/classes | Out-Null
$javaSources = @(Get-ChildItem src/main/java -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
javac -d build/classes @javaSources
```

If compilation succeeds:

```powershell
java -cp build/classes restaurantrush.Main
```

For a fresh build, use an empty output directory. Do not commit generated `.class` files. Existing class files can survive recompilation when a class is later commented out.

## Playing Stage 1

The game runs 18 turns with two tables. Six Regular customers arrive on turns 1, 4, 7, 10, 13 and 16. Salad costs $9 and needs one preparation unit; Burger costs $15 and needs one; Pasta costs $18 and needs two.

- Waiter: 1 seat, 2 take order, 3 serve, 4 pass. Enter the displayed customer/order ID when prompted.
- Chef: 1 prepare, 2 pass. The Chef acts after the Waiter.
- Cashier acts automatically, collecting at most one eligible payment per turn, oldest-ready first.
- Invalid input can be retried without consuming the staff action. End-of-input stops an unfinished game cleanly.

Each turn displays the queue, tables, orders and current choices. End-of-turn summaries use cumulative served/departure/paid counts, revenue, satisfaction recorded at payment and completed turns. With no payments, average satisfaction is N/A.

Victory after turn 18 requires at least four paid customers, $45 revenue and average paid satisfaction of at least 60. The game does not end early. Customer 6 cannot pay by turn 18 under the required timing.

## Reproducible evidence

See [Stage 1 test notes](docs/stage-1/test-notes.md), including exact inputs and captured output for both endings.

```powershell
Get-Content docs/stage-1/victory-input.txt | java -cp build/classes restaurantrush.Main
Get-Content docs/stage-1/defeat-input.txt | java -cp build/classes restaurantrush.Main
```

Start a fresh process for each replay. The current fixed random seed makes every Regular customer's first selection Burger; IDs start at 1 in a fresh process. Changing that policy requires updating the evidence.

## Stage releases and comparisons

| Stage | Tag | Status |
| --- | --- | --- |
| 1 | `stage-1` | Tested locally; release/tag publication pending |
| 2 | `stage-2` | Pending |
| 3 | `stage-3` | Pending |

For Stage 1 to 2 and Stage 2 to 3, add the new files, existing files changed or deleted with reasons, regression results, and the real commit/compare links once those versions exist. Do not describe pre-existing preserved code as newly authored during a later stage.

## Design and submission links

- Stage 1, Stage 2 and Stage 3 class diagrams: pending; each must match its tagged code.
- Complex Stage 2 or Stage 3 sequence diagram: pending.
- Group report with each member's named reflection: pending.
- Approximately 15-minute video and member/section timestamps: pending.
- Full ordered AI histories on each member's own branch: prepare at the end as agreed; links for all five members pending. A summary alone is insufficient.
- Required declarations: pending.

Keep these items accurate as work is completed. Submission deadline in the briefing: 15 November 2026 at 11:59 PM Singapore time; one submission per group, with accessible links.

## Development reference

The previous shared interface requirements are preserved in [integration contracts](docs/integration-contracts.md). See [release checklist](docs/stage-1/release-checklist.md) before creating the Stage 1 tag.
