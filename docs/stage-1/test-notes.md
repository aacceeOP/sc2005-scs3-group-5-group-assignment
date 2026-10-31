# Stage 1 test notes

Verified locally on 1 November 2026 using JDK 26.0.2.1. These results cover the working-tree source containing the final-summary and end-of-input fixes, before its release commit. Re-run on the exact commit selected for the stage-1 tag.

## Build and controller checks

All Java sources compiled successfully. A standalone controller harness outside the repository passed 294 assertions over four scenarios: complete victory, complete defeat, seated abandonment before ordering, and seated abandonment with an unfinished order. It checked arrivals, phases, failed-action availability, serving/payment timing, table cleanup, cancellation, payment totals, satisfaction snapshots and no early end. The external harness is not a repository test suite; the CLI replay inputs, instructions and verified results below provide reproducible evidence.

## Victory replay

[Replay input](victory-input.txt).

For customer n from 1 to 6, use this three-turn pattern:

1. Turn 3n - 2: Waiter 1, customer ID n; Chef 2.
2. Turn 3n - 1: Waiter 2, customer ID n; Chef 1, order ID n.
3. Turn 3n: Waiter 3, order ID n; Chef 2.

Final result: VICTORY, six customers served, zero unhappy departures, five paid customers, $75.00 revenue, average paid satisfaction 84.0, 18 turns used. Customer 6 is served but unpaid at the end. Paid customers remain included in the served count.

## Defeat replay

[Replay input](defeat-input.txt).

For all 18 turns, choose Waiter 4 and Chef 2. Final result: DEFEAT, zero served, two unhappy departures, zero paid, $0.00 revenue, average N/A and 18 turns used. Later arrivals have not all reached zero satisfaction by turn 18.

## Input and reporting checks

- Both replays printed exactly 18 end-of-turn summaries, with final statistics before the result.
- Nonnumeric action input, out-of-range actions, nonpositive IDs and nonexistent customer IDs were rejected and the game continued.
- EOF at the action prompt, ID prompt, Chef prompt and after invalid input exited with no stack trace and no victory/defeat declaration.
- Reproduction for immediate EOF in PowerShell: `@() | java -cp build/classes restaurantrush.Main`. Expected message: `Input ended. Game stopped before completion.`

## Coverage limits

Not exhaustive. Additional cases should cover multi-unit Pasta preparation, alternative service priorities, oldest-ready-first selection/ties and each victory threshold independently. Later-stage functionality is disabled and was not certified. Other JDK versions and a tagged checkout have not yet been verified.
