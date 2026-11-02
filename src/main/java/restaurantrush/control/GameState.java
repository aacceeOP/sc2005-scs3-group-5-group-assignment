package restaurantrush.control;

import restaurantrush.model.Order;
import restaurantrush.model.enums.CustomerStatus;
import restaurantrush.model.Table;
import restaurantrush.model.customer.Customer;
import restaurantrush.model.staff.Cashier;
import restaurantrush.model.staff.Chef;
import restaurantrush.model.staff.Host;
import restaurantrush.model.staff.Waiter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Authoritative state owned by the controller layer.
 * Tables and staff are injected by the application bootstrap so this class does
 * not assume constructors that the README leaves to the domain/staff modules.
 */
public class GameState {
    private static final int FINAL_TURN = 18;

    private int currentTurn = 1;
    private int revenueCents;
    private boolean configured;

    // Live customers are added when they arrive, preserving arrival order.
    private final List<Customer> customers = new ArrayList<>();
    private final Queue<Customer> waitingQueue = new ArrayDeque<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<Table> tables = new ArrayList<>();

    // Identity semantics avoid treating two distinct Customer objects as the same.
    private final Set<Customer> registeredCustomers =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Integer, List<Customer>> scheduledArrivals = new LinkedHashMap<>();
    private final Map<Customer, Integer> paidSatisfactionSnapshots = new IdentityHashMap<>();

    private Waiter waiter;
    private Chef chef;
    private Cashier cashier;
    private Host host; 

    public GameState() {
        // Call configureRestaurant(...) before GameController.startGame().
    }

    /**
     * Injects restaurant resources. The base setup must contain two distinct
     * tables and all required staff members.
     */
    public void configureRestaurant(
            List<Table> initialTables,
            Waiter waiter,
            Chef chef,
            Cashier cashier,
            Host host){
        if (configured) {
            throw new IllegalStateException("Restaurant resources have already been configured.");
        }
        if (initialTables == null || initialTables.size() != 2) {
            throw new IllegalArgumentException("The base restaurant must have exactly two tables.");
        }
        List<Table> validatedTables = new ArrayList<>();
        for (Table table : initialTables) {
            if (table == null) {
                throw new IllegalArgumentException("Restaurant tables cannot be null.");
            }
            for (Table existing : validatedTables) {
                if (existing == table) {
                    throw new IllegalArgumentException("The same Table object cannot be registered twice.");
                }
            }
            if (!table.isAvailable()) {
                throw new IllegalArgumentException("Restaurant tables must be free during initial setup.");
            }
            validatedTables.add(table);
        }
        if (waiter == null || chef == null || cashier == null || host == null) {
            throw new IllegalArgumentException("Waiter, Chef, Cashier and Host are required.");
        }

        tables.addAll(validatedTables);
        this.waiter = waiter;
        this.chef = chef;
        this.cashier = cashier;
        this.host = host;
        configured = true;
    }

    public boolean isConfigured() {
        return configured;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public void incrementTurn() {
        if (currentTurn < 1 || currentTurn > FINAL_TURN) {
            throw new IllegalStateException("Only active turns 1 through 18 can be completed.");
        }
        currentTurn++;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    /** Registers a future customer arrival; it is not active until its scheduled turn. */
    public void scheduleCustomerArrival(int turn, Customer customer) {
        if (turn < 1 || turn > FINAL_TURN) {
            throw new IllegalArgumentException("Arrival turn must be between 1 and 18.");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Scheduled customer cannot be null.");
        }
        if (!registeredCustomers.add(customer)) {
            throw new IllegalArgumentException("This Customer object is already registered.");
        }
        scheduledArrivals.computeIfAbsent(turn, ignored -> new ArrayList<>()).add(customer);
    }

    /** Returns and removes only the arrivals scheduled for the requested turn. */
    public List<Customer> takeScheduledArrivals(int turn) {
        if (turn < 1 || turn > FINAL_TURN) {
            throw new IllegalArgumentException("Turn must be between 1 and 18.");
        }
        List<Customer> arrivals = scheduledArrivals.remove(turn);
        return arrivals == null ? Collections.emptyList() : new ArrayList<>(arrivals);
    }

    /** Moves a scheduled customer into the live roster and waiting queue exactly once. */
    public void enqueueArrivedCustomer(Customer customer) {
        if (customer == null || !registeredCustomers.contains(customer)) {
            throw new IllegalArgumentException("Customer is not registered in this restaurant.");
        }
        if (containsIdentity(customers, customer) || containsIdentity(new ArrayList<>(waitingQueue), customer)) {
            throw new IllegalStateException("Customer has already arrived.");
        }
        customers.add(customer);
        waitingQueue.add(customer);
    }

    public boolean isRestaurantCustomer(Customer customer) {
        return customer != null && registeredCustomers.contains(customer);
    }

    public boolean isActiveCustomer(Customer customer) {
        return customer != null && containsIdentity(customers, customer);
    }

    public boolean isInWaitingQueue(Customer customer) {
        return customer != null && containsIdentity(new ArrayList<>(waitingQueue), customer);
    }

    public boolean removeFromWaitingQueue(Customer customer) {
        if (customer == null) {
            return false;
        }
        // Remove by object identity, not potentially overridden equals().
        for (Customer queued : new ArrayList<>(waitingQueue)) {
            if (queued == customer) {
                return waitingQueue.remove(queued);
            }
        }
        return false;
    }

    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(new ArrayList<>(customers));
    }

    public List<Customer> getWaitingCustomers() {
        return Collections.unmodifiableList(new ArrayList<>(waitingQueue));
    }

    public Queue<Customer> getWaitingQueue() {
        return new ArrayDeque<>(waitingQueue);
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(new ArrayList<>(orders));
    }

    public void addOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }
        if (!isActiveCustomer(order.getCustomer())) {
            throw new IllegalArgumentException("An order must belong to an active restaurant customer.");
        }
        if (containsOrder(order)) {
            throw new IllegalArgumentException("The same Order object cannot be registered twice.");
        }
        orders.add(order);
    }

    public boolean containsOrder(Order target) {
        if (target == null) {
            return false;
        }
        for (Order order : orders) {
            if (order == target) {
                return true;
            }
        }
        return false;
    }

    public List<Table> getTables() {
        return Collections.unmodifiableList(new ArrayList<>(tables));
    }

    public boolean containsTable(Table target) {
        if (target == null) {
            return false;
        }
        for (Table table : tables) {
            if (table == target) {
                return true;
            }
        }
        return false;
    }

    public Table getFreeTable() {
        for (Table table : tables) {
            if (table.isAvailable()) {
                return table;
            }
        }
        return null;
    }

    /** Performs every check needed before a payment can be committed. */
    public void validatePaymentRecord(Customer customer, int amountCents) {
        if (customer == null || !isActiveCustomer(customer)) {
            throw new IllegalArgumentException("Paid customer must be active in this restaurant.");
        }
        if (amountCents < 0) {
            throw new IllegalArgumentException("Payment cannot be negative.");
        }
        if (paidSatisfactionSnapshots.containsKey(customer)) {
            throw new IllegalStateException("Payment for this customer has already been recorded.");
        }
        Math.addExact(revenueCents, amountCents);
    }

    /**
     * Records payment once and snapshots satisfaction at the moment of payment.
     * The Cashier is expected to return the charged amount without changing
     * customer/order status, revenue, or table occupancy.
     */
    public void recordPayment(Customer customer, int amountCents) {
        validatePaymentRecord(customer, amountCents);
        int updatedRevenue = Math.addExact(revenueCents, amountCents);
        int satisfactionSnapshot = customer.getSatisfaction();
        paidSatisfactionSnapshots.put(customer, satisfactionSnapshot);
        revenueCents = updatedRevenue;
    }

    public boolean hasRecordedPayment(Customer customer) {
        return customer != null && paidSatisfactionSnapshots.containsKey(customer);
    }

    /** Cumulative customers served, including those who have since paid. */
    public int getServedCustomerCount() {
        int count = 0;
        for (Customer customer : customers) {
            if (customer.getServedTurn() >= 1) {
                count++;
            }
        }
        return count;
    }

    public int getUnhappyDepartureCount() {
        int count = 0;
        for (Customer customer : customers) {
            if (customer.getState() == CustomerStatus.ABANDONED) {
                count++;
            }
        }
        return count;
    }

    public int getTurnsUsed() {
        return currentTurn - 1;
    }

    public int getPaidCustomerCount() {
        return paidSatisfactionSnapshots.size();
    }

    public double getAveragePaidSatisfaction() {
        if (paidSatisfactionSnapshots.isEmpty()) {
            return 0.0;
        }
        int total = 0;
        for (Integer satisfaction : paidSatisfactionSnapshots.values()) {
            total += satisfaction;
        }
        return (double) total / paidSatisfactionSnapshots.size();
    }

    public Waiter getWaiter() {
        return waiter;
    }

    public Chef getChef() {
        return chef;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public Host getHost() {
        return host;
    }

    private static boolean containsIdentity(List<Customer> values, Customer target) {
        for (Customer value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}
