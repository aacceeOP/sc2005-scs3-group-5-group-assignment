package restaurantrush.controller;

import restaurantrush.model.Order;
import restaurantrush.model.OrderStatus;
import restaurantrush.model.Table;
import restaurantrush.model.customer.Customer;
import restaurantrush.model.menu.Menu;
import restaurantrush.model.pricing.PricingPolicy;
import restaurantrush.model.pricing.HappyHourPolicy;
import restaurantrush.model.staff.Cashier;
import restaurantrush.model.staff.Chef;
import restaurantrush.model.staff.Host;
import restaurantrush.model.staff.Waiter;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Coordinates Restaurant Rush turns. The CLI owns all input/output and calls:
 * startGame(), beginTurn(...), one choice per active phase, then finishTurn().
 *
 * Integration notes:
 * - Waiter.takeOrder(Customer, Menu, PricingPolicy, int) creates and attaches
 *   the Order and returns it. This controller only registers it in GameState.
 * - Waiter.serveOrder(Order, int) marks the Order SERVED; afterwards the
 *   controller calls Customer.receiveOrder(Order, int). Neither object should
 *   update the other object as a side effect.
 * - The README does not define package names for staff classes or the Chef,
 *   Cashier, Happy Hour, and payment APIs. The imports and extra methods used
 *   here are the proposed integration contracts documented for Person 4.
 */
public class GameController {
    private static final int FINAL_TURN = 18;
    private static final int MIN_PAID_CUSTOMERS = 4;
    private static final int MIN_REVENUE_CENTS = 4_500;
    private static final double MIN_AVERAGE_SATISFACTION = 60.0;

    private final GameState gameState;
    private final Menu menu;
    private final PricingPolicy pricingPolicy;
    private final HappyHourPolicy happyHourPolicy;
    private final boolean stage3Enabled;

    private boolean gameStarted;
    private boolean turnStarted;
    private boolean happyHourRequestRejectedThisTurn;
    private int managerChoicePhase;
    private String lastErrorMessage = "";

    // Store turns by object identity: order/customer IDs are not used as map keys.
    private final Map<Order, Integer> servedTurnByOrder = new IdentityHashMap<>();
    private final Map<Customer, Integer> readyToPayTurnByCustomer = new IdentityHashMap<>();

    public GameController(
            GameState gameState,
            Menu menu,
            PricingPolicy pricingPolicy,
            HappyHourPolicy happyHourPolicy,
            boolean stage3Enabled) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
        this.menu = Objects.requireNonNull(menu, "menu");
        this.pricingPolicy = Objects.requireNonNull(pricingPolicy, "pricingPolicy");
        this.happyHourPolicy = Objects.requireNonNull(happyHourPolicy, "happyHourPolicy");
        this.stage3Enabled = stage3Enabled;
    }

    /**
     * Validates preconfigured dependencies. This method intentionally does not
     * construct Table/Staff/Menu objects because their constructors belong to
     * the team's model modules and are not specified by the README.
     */
    public void startGame() {
        if (gameStarted) {
            throw new IllegalStateException("startGame() has already been called.");
        }
        if (!gameState.isConfigured()) {
            throw new IllegalStateException(
                    "Configure GameState with the restaurant tables and staff before starting.");
        }
        if (gameState.getTables().size() != 2) {
            throw new IllegalStateException("The restaurant must start with exactly two tables.");
        }
        if (stage3Enabled && gameState.getHost() == null) {
            throw new IllegalStateException("Stage 3 requires a Host.");
        }
        gameStarted = true;
    }

    /**
     * Starts one turn. Resets each active staff member exactly once, handles
     * Happy Hour, then activates scheduled arrivals. The CLI should display the
     * state only after this method returns.
     */
    public void beginTurn(boolean activateHappyHour) {
        requireStarted();
        if (isGameOver()) {
            throw new IllegalStateException("The game has ended.");
        }
        if (turnStarted) {
            throw new IllegalStateException("The current turn has already started.");
        }

        int currentTurn = gameState.getCurrentTurn();
        if (currentTurn < 1 || currentTurn > FINAL_TURN) {
            throw new IllegalStateException("Turn number must be between 1 and 18.");
        }

        resetActiveStaffForTurn();
        happyHourRequestRejectedThisTurn = activateHappyHour
                && !happyHourPolicy.activate(currentTurn);
        // Activate scheduled customers before applying the turn-start bonus so
        // every customer who is waiting during this turn's start is eligible.
        processArrivals(currentTurn);
        applyTurnStartEffects(currentTurn);

        managerChoicePhase = 0;
        turnStarted = true;
        lastErrorMessage = "";
    }

    /**
     * Accepts one action for the current phase. Invalid actions return false,
     * set a readable CLI message, and leave the same phase available for retry.
     * Commands:
     *   HOST:SEAT:<customerId>          (Stage 3 only)
     *   WAITER:SEAT:<customerId>
     *   WAITER:TAKE_ORDER:<customerId>
     *   WAITER:SERVE:<orderId>
     *   CHEF:PREPARE:<orderId>
     *   PHASE:PASS
     */
    public boolean processManagerChoice(String choice) {
        if (!turnStarted) {
            return reject("Begin the turn before submitting an action.");
        }
        if (managerChoicePhase >= getPhaseCount()) {
            return reject("All staff phases for this turn are complete.");
        }
        if (choice == null || choice.trim().isEmpty()) {
            return reject("Enter a staff action or PHASE:PASS.");
        }

        String[] parts = choice.trim().split("\\s*:\\s*");
        if (parts.length == 2
                && parts[0].equalsIgnoreCase("PHASE")
                && parts[1].equalsIgnoreCase("PASS")) {
            managerChoicePhase++;
            lastErrorMessage = "";
            return true;
        }

        String phase = getCurrentPhase();
        boolean successful;
        if ("HOST".equals(phase)) {
            successful = processHostChoice(parts);
        } else if ("WAITER".equals(phase)) {
            successful = processWaiterChoice(parts);
        } else {
            successful = processChefChoice(parts);
        }

        if (!successful) {
            if (lastErrorMessage.isEmpty()) {
                lastErrorMessage = "That action is not valid for the current phase or game state.";
            }
            return false;
        }

        managerChoicePhase++;
        lastErrorMessage = "";
        return true;
    }

    /** Finishes the turn once all active phases have acted or passed. */
    public void finishTurn() {
        requireStarted();
        if (!turnStarted || managerChoicePhase < getPhaseCount()) {
            throw new IllegalStateException("Complete or pass every active staff phase first.");
        }

        int currentTurn = gameState.getCurrentTurn();
        processEatingAndPayment(currentTurn);
        processAbandonment();
        gameState.incrementTurn();
        turnStarted = false;
    }

    public boolean wasHappyHourRequestRejectedThisTurn() {
        return happyHourRequestRejectedThisTurn;
    }

    /** The CLI should show this message when processManagerChoice returns false. */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public boolean isGameOver() {
        return gameState.getCurrentTurn() > FINAL_TURN;
    }

    public GameResult checkResult() {
        if (!isGameOver()) {
            return GameResult.IN_PROGRESS;
        }
        return gameState.getPaidCustomerCount() >= MIN_PAID_CUSTOMERS
                && gameState.getRevenueCents() >= MIN_REVENUE_CENTS
                && gameState.getAveragePaidSatisfaction() >= MIN_AVERAGE_SATISFACTION
                ? GameResult.VICTORY : GameResult.DEFEAT;
    }

    private void resetActiveStaffForTurn() {
        // One reset call per active staff instance. Waiter seating/order/serving
        // shares one allowance because all three operations use the same Waiter.
        gameState.getWaiter().resetForNewTurn();
        gameState.getChef().resetForNewTurn();
        gameState.getCashier().resetForNewTurn();
        if (stage3Enabled) {
            gameState.getHost().resetForNewTurn();
        }
    }

    private int getPhaseCount() {
        return stage3Enabled ? 3 : 2;
    }

    private String getCurrentPhase() {
        if (stage3Enabled) {
            if (managerChoicePhase == 0) return "HOST";
            if (managerChoicePhase == 1) return "WAITER";
            return "CHEF";
        }
        return managerChoicePhase == 0 ? "WAITER" : "CHEF";
    }

    private boolean processHostChoice(String[] parts) {
        if (!stage3Enabled) {
            return reject("Host seating is only available in Stage 3.");
        }
        if (parts.length != 3
                || !parts[0].equalsIgnoreCase("HOST")
                || !parts[1].equalsIgnoreCase("SEAT")) {
            return reject("Use HOST:SEAT:<customerId> or PHASE:PASS.");
        }

        Customer customer = findActiveCustomer(parsePositiveId(parts[2]));
        if (customer == null) {
            return reject("That customer is not an active member of this restaurant.");
        }
        if (!gameState.isInWaitingQueue(customer) || !customer.isWaitingForTable()) {
            return reject("Select a customer who is waiting for a table.");
        }

        Table table = gameState.getFreeTable();
        if (table == null) {
            return reject("There is no free table.");
        }
        if (!gameState.containsTable(table)) {
            return reject("The selected table does not belong to this restaurant.");
        }

        // Host owns the seating state changes; the controller only updates queue membership.
        try {
            gameState.getHost().seatCustomer(customer, table);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return reject(ex.getMessage() == null ? "Host could not seat that customer." : ex.getMessage());
        }
        validateSuccessfulSeating(customer, table);
        if (!gameState.removeFromWaitingQueue(customer)) {
            throw new IllegalStateException("Customer was seated but was not removable from the waiting queue.");
        }
        return true;
    }

    private boolean processWaiterChoice(String[] parts) {
        if (parts.length < 2 || !parts[0].equalsIgnoreCase("WAITER")) {
            return reject("Use WAITER:SEAT:<customerId>, WAITER:TAKE_ORDER:<customerId>, "
                    + "WAITER:SERVE:<orderId>, or PHASE:PASS.");
        }

        String action = parts[1].toUpperCase(Locale.ROOT);
        if ("SEAT".equals(action)) {
            if (parts.length != 3) {
                return reject("Use WAITER:SEAT:<customerId>.");
            }
            Customer customer = findActiveCustomer(parsePositiveId(parts[2]));
            if (customer == null) {
                return reject("That customer is not an active member of this restaurant.");
            }
            if (!gameState.isInWaitingQueue(customer) || !customer.isWaitingForTable()) {
                return reject("Select a customer who is waiting for a table.");
            }
            Table table = gameState.getFreeTable();
            if (table == null) {
                return reject("There is no free table.");
            }
            if (!gameState.containsTable(table)) {
                return reject("The selected table does not belong to this restaurant.");
            }

            try {
                gameState.getWaiter().seatCustomer(customer, table);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                return reject(ex.getMessage() == null ? "Waiter could not seat that customer." : ex.getMessage());
            }
            validateSuccessfulSeating(customer, table);
            if (!gameState.removeFromWaitingQueue(customer)) {
                throw new IllegalStateException("Customer was seated but was not removable from the waiting queue.");
            }
            return true;
        }

        if ("TAKE_ORDER".equals(action)) {
            if (parts.length != 3) {
                return reject("Use WAITER:TAKE_ORDER:<customerId>.");
            }
            Customer customer = findActiveCustomer(parsePositiveId(parts[2]));
            if (customer == null) {
                return reject("That customer is not an active member of this restaurant.");
            }
            if (!customer.canOrder()) {
                return reject("That customer is not seated and eligible to order.");
            }
            Table customerTable = customer.getTable();
            if (customerTable == null || !gameState.containsTable(customerTable)) {
                return reject("That customer's table does not belong to this restaurant.");
            }

            int currentTurn = gameState.getCurrentTurn();
            // Waiter owns menu selection, PricingPolicy calculation, Order creation,
            // price locking and Customer.placeOrder(...). Do not repeat any of them here.
            Order order;
            try {
                order = gameState.getWaiter().takeOrder(
                        customer, menu, pricingPolicy, currentTurn);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                return reject(ex.getMessage() == null ? "Waiter could not take that order." : ex.getMessage());
            }
            if (order == null) {
                throw new IllegalStateException("Waiter.takeOrder() returned null.");
            }
            if (order.getCustomer() != customer) {
                throw new IllegalStateException("Waiter returned an order owned by a different customer.");
            }
            if (gameState.containsOrder(order)) {
                throw new IllegalStateException("Waiter returned an order that was already registered.");
            }
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new IllegalStateException("Waiter.takeOrder() must return a PENDING order.");
            }

            // Register exactly once. Waiter has already attached the Order to the Customer.
            gameState.addOrder(order);
            return true;
        }

        if ("SERVE".equals(action)) {
            if (parts.length != 3) {
                return reject("Use WAITER:SERVE:<orderId>.");
            }
            Order order = findOrder(parsePositiveId(parts[2]));
            if (order == null) {
                return reject("That order does not belong to this restaurant.");
            }
            int currentTurn = gameState.getCurrentTurn();
            Customer customer = order.getCustomer();
            if (!gameState.isActiveCustomer(customer)) {
                return reject("The order's customer is not active in this restaurant.");
            }
            Table customerTable = customer.getTable();
            if (customerTable == null || !gameState.containsTable(customerTable)) {
                return reject("The order's customer is not seated at a restaurant table.");
            }

            // Both checks must happen before either object is updated.
            if (!order.canServe(currentTurn)) {
                return reject("The order must have been READY since an earlier turn.");
            }
            if (!customer.canReceiveOrder(order)) {
                return reject("The customer is not eligible to receive this order.");
            }

            // Waiter marks Order SERVED; Customer then records that same serving turn.
            // Neither controller nor either object updates the other a second time.
            try {
                gameState.getWaiter().serveOrder(order, currentTurn);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                return reject(ex.getMessage() == null ? "Waiter could not serve that order." : ex.getMessage());
            }
            if (order.getStatus() != OrderStatus.SERVED) {
                throw new IllegalStateException("Waiter.serveOrder() must mark the Order SERVED.");
            }
            customer.receiveOrder(order, currentTurn);
            servedTurnByOrder.put(order, currentTurn);
            return true;
        }

        return reject("Unknown Waiter action: " + action + ".");
    }

    private boolean processChefChoice(String[] parts) {
        if (parts.length != 3
                || !parts[0].equalsIgnoreCase("CHEF")
                || !parts[1].equalsIgnoreCase("PREPARE")) {
            return reject("Use CHEF:PREPARE:<orderId> or PHASE:PASS.");
        }
        Order order = findOrder(parsePositiveId(parts[2]));
        if (order == null) {
            return reject("That order does not belong to this restaurant.");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            return reject("Only a PENDING order can be prepared.");
        }

        // Proposed integration API: currentTurn must be recorded when an order
        // first becomes READY so Order.canServe(currentTurn) enforces next-turn serving.
        try {
            gameState.getChef().prepare(order, gameState.getCurrentTurn());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return reject(ex.getMessage() == null ? "Chef could not prepare that order." : ex.getMessage());
        }
        return true;
    }

    private void validateSuccessfulSeating(Customer customer, Table table) {
        if (table.isFree() || customer.getTable() != table) {
            throw new IllegalStateException(
                    "Staff seating must occupy the table and assign it to the customer.");
        }
    }

    private void processArrivals(int currentTurn) {
        for (Customer customer : gameState.takeScheduledArrivals(currentTurn)) {
            gameState.enqueueArrivedCustomer(customer);
        }
    }

    private void applyTurnStartEffects(int currentTurn) {
        if (!happyHourPolicy.isActive(currentTurn)) {
            return;
        }

        List<Customer> eligible = new ArrayList<>();
        for (Customer customer : gameState.getCustomers()) {
            if (customer.isWaitingForTable() || customer.getTable() != null) {
                eligible.add(customer);
            }
        }
        happyHourPolicy.applySatisfactionBonus(eligible);
    }

    /**
     * Served on T -> READY_TO_PAY at the end of T+1 -> payment at the earliest
     * end of T+2. This separates eating progression from the cashier task.
     */
    private void processEatingAndPayment(int currentTurn) {
        for (Map.Entry<Order, Integer> entry : servedTurnByOrder.entrySet()) {
            Order order = entry.getKey();
            int servedTurn = entry.getValue();
            if (currentTurn >= servedTurn + 1
                    && order.getStatus() == OrderStatus.SERVED) {
                Customer customer = order.getCustomer();
                if (!readyToPayTurnByCustomer.containsKey(customer)) {
                    customer.markReadyToPay();
                    readyToPayTurnByCustomer.put(customer, currentTurn);
                }
            }
        }

        Customer oldestEligible = null;
        Order payableOrder = null;
        int earliestReadyTurn = Integer.MAX_VALUE;
        // Honour Cashier's oldest-ready-to-pay-first rule. Arrival order breaks
        // ties because GameState keeps customers in arrival order.
        for (Customer customer : gameState.getCustomers()) {
            Integer readyTurn = readyToPayTurnByCustomer.get(customer);
            if (readyTurn == null || currentTurn < readyTurn + 1) {
                continue;
            }
            Order customerOrder = findServedUnpaidOrderFor(customer);
            if (customerOrder == null) {
                continue;
            }
            Integer servedTurn = servedTurnByOrder.get(customerOrder);
            if (servedTurn == null || currentTurn < servedTurn + 2) {
                continue;
            }
            if (readyTurn < earliestReadyTurn) {
                earliestReadyTurn = readyTurn;
                oldestEligible = customer;
                payableOrder = customerOrder;
            }
        }
        if (oldestEligible == null || payableOrder == null) {
            return;
        }

        int expectedBillCents = payableOrder.getBillCents();
        Table table = oldestEligible.getTable();
        if (table == null || !gameState.containsTable(table)) {
            throw new IllegalStateException(
                    "A customer must remain seated at a restaurant table until payment.");
        }
        gameState.validatePaymentRecord(oldestEligible, expectedBillCents);

        int chargedCents = gameState.getCashier().processPayment(oldestEligible, payableOrder);
        if (chargedCents != expectedBillCents) {
            throw new IllegalStateException(
                    "Cashier must charge the order's locked bill in integer cents.");
        }

        // The Cashier collects/validates payment; controller records the result
        // and each state transition exactly once under the agreed API contract.
        payableOrder.markPaid();
        oldestEligible.markPaid();
        gameState.recordPayment(oldestEligible, chargedCents);
        if (table != null) {
            table.releaseTable(); // Table owns clearing both occupant references.
        }
    }

    private void processAbandonment() {
        // Use a snapshot because the waiting queue is modified as customers leave.
        for (Customer customer : gameState.getWaitingCustomers()) {
            if (!customer.isWaitingForTable()) {
                continue;
            }
            customer.loseSatisfaction(customer.getWaitingLoss());
            if (customer.getSatisfaction() <= 0) {
                abandonCustomer(customer);
            }
        }
    }

    private void abandonCustomer(Customer customer) {
        // Mark departure first; the Customer API must preserve this state when
        // Table.releaseTable() calls Customer.clearTable().
        customer.markDeparted();
        gameState.removeFromWaitingQueue(customer);

        for (Order order : gameState.getOrders()) {
            if (order.getCustomer() == customer
                    && (order.getStatus() == OrderStatus.PENDING
                    || order.getStatus() == OrderStatus.READY)) {
                order.cancel();
            }
        }

        Table table = customer.getTable();
        if (table != null) {
            if (!gameState.containsTable(table)) {
                throw new IllegalStateException("Departing customer's table is not part of this restaurant.");
            }
            table.releaseTable();
        }
    }

    private Customer findActiveCustomer(int customerId) {
        if (customerId < 1) {
            return null;
        }
        for (Customer customer : gameState.getCustomers()) {
            if (customer.getCustomerId() == customerId) {
                return customer;
            }
        }
        return null;
    }

    private Order findOrder(int orderId) {
        if (orderId < 1) {
            return null;
        }
        for (Order order : gameState.getOrders()) {
            if (order.getOrderId() == orderId) {
                return order;
            }
        }
        return null;
    }

    private Order findServedUnpaidOrderFor(Customer customer) {
        for (Order order : gameState.getOrders()) {
            if (order.getCustomer() == customer
                    && order.getStatus() == OrderStatus.SERVED
                    && servedTurnByOrder.containsKey(order)) {
                return order;
            }
        }
        return null;
    }

    private int parsePositiveId(String raw) {
        try {
            int id = Integer.parseInt(raw.trim());
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException ignored) {
            // Rejected below with the same safe validation path as other bad choices.
        }
        return -1;
    }

    private boolean reject(String message) {
        lastErrorMessage = message;
        return false;
    }

    private void requireStarted() {
        if (!gameStarted) {
            throw new IllegalStateException("Call startGame() before this operation.");
        }
    }
}
