
package restaurantrush.boundary;

import restaurantrush.control.GameController;
import restaurantrush.control.GameResult;
import restaurantrush.model.Order;
import restaurantrush.model.Table;
import restaurantrush.model.customer.Customer;
import java.util.List;
import java.io.EOFException;
import java.util.Locale;

public class GameCLI {

    private final GameController controller;
    private final InputHandler inputHandler;

    public GameCLI(GameController controller) {
        this.controller = controller;
        this.inputHandler = new InputHandler();
    }

    public void run() {
        controller.startGame();

        while (!controller.isGameOver()) {
            controller.beginTurn();

            while (!controller.isTurnComplete()) {
                displayState();

                String choice;
                try {
                    choice = getManagerChoice();
                } catch (EOFException ex) {
                    displayMessage("\nInput ended. Game stopped before completion.");
                    return;
                }
                boolean accepted = controller.processManagerChoice(choice);

                if (!accepted) {
                    displayMessage(controller.getLastErrorMessage());
                }
            }

            controller.finishTurn();
            displayTurnSummary();
        }

        displayResult(controller.checkResult());
    }

    public void displayState() {
        System.out.printf("%n=== Restaurant Rush | Turn %d of 18 ===%n",
            controller.getCurrentTurn());
        System.out.printf(Locale.US, "Revenue: $%.2f | Paid customers: %d%n",
            controller.getRevenueCents() / 100.0,
            controller.getPaidCustomerCount());
        if (controller.getPaidCustomerCount() == 0) {
            System.out.println("Average paid satisfaction: N/A");
        } else {
            System.out.printf(Locale.US, "Average paid satisfaction: %.1f%n",
                controller.getAveragePaidSatisfaction());
        }

        System.out.println("\nWaiting queue:");
        List<Customer> waiting = controller.getWaitingCustomers();
        if (waiting.isEmpty()) {
            System.out.println("None");
        }
        for (Customer customer : waiting) {
            System.out.printf(
                "Customer %d | Type: %s | Satisfaction: %d%n",
                customer.getCustomerId(),
                customer.getCustomerType(),
                customer.getSatisfaction());
}

        System.out.println("\nTables:");
        for (Table table : controller.getTables()) {
            Customer customer = table.getCustomer();
            if (customer == null) {
                System.out.printf("Table %d | Available%n", table.getTableId());
            } else {
                System.out.printf(
                    "Table %d | Customer %d | Type: %s | Status: %s | Satisfaction: %d%n",
                    table.getTableId(),
                    customer.getCustomerId(),
                    customer.getCustomerType(),
                    customer.getState(),
                    customer.getSatisfaction());
            }
        }

        System.out.println("\nOrders:");
        List<Order> orders = controller.getOrders();
        if (orders.isEmpty()) {
            System.out.println("None");
        }
        for (Order order : orders) {
            System.out.printf(
                "Order %d | Customer %d | %s | %s | Preparation: %d/%d%n",
                    order.getOrderId(),
                    order.getCustomer().getCustomerId(),
                    order.getCustomer().getCustomerType(),
                    order.getItem().getName(),
                    order.getStatus(),
                    order.getPreparationProgress(),
                    order.getItem().getPreparationUnits());
        }
    }

    public String getManagerChoice() throws EOFException {
        String phase = controller.getCurrentPhase();
        while (true) {
            System.out.println("\nCurrent phase: " + phase);
            if ("WAITER".equals(phase)) {
                System.out.println("1) Seat a customer");
                System.out.println("2) Take an order");
                System.out.println("3) Serve an order");
                System.out.println("4) Pass");
                int choice = inputHandler.readInt("Choose an action: ");
                switch (choice) {
                    case 1:
                        return "WAITER:SEAT:" + readPositiveId("Customer ID: ");
                    case 2:
                        return "WAITER:TAKE_ORDER:" + readPositiveId("Customer ID: ");
                    case 3:
                        return "WAITER:SERVE:" + readPositiveId("Order ID: ");
                    case 4:
                        return "PHASE:PASS";
                    default:
                        displayMessage("Please choose a number from 1 to 4.");
                }
            } else if ("CHEF".equals(phase)) {
                System.out.println("1) Prepare an order");
                System.out.println("2) Pass");
                int choice = inputHandler.readInt("Choose an action: ");
                switch (choice) {
                    case 1:
                        return "CHEF:PREPARE:" + readPositiveId("Order ID: ");
                    case 2:
                        return "PHASE:PASS";
                    default:
                        displayMessage("Please choose 1 or 2.");
                }
            } else {
                throw new IllegalStateException("Unknown staff phase: " + phase);
            }
        }
    }

    private int readPositiveId(String prompt) throws EOFException {
        while (true) {
            int id = inputHandler.readInt(prompt);
            if (id > 0) {
                return id;
            }
            displayMessage("Please enter an ID greater than zero.");
        }
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void displayTurnSummary() {
        System.out.printf("%n=== End of Turn %d ===%n", controller.getTurnsUsed());
        System.out.println("Customers served: " + controller.getServedCustomerCount());
        System.out.println("Unhappy departures: " + controller.getUnhappyDepartureCount());
        System.out.println("Customers paid: " + controller.getPaidCustomerCount());
        System.out.printf(Locale.US, "Revenue: $%.2f%n", controller.getRevenueCents() / 100.0);
        if (controller.getPaidCustomerCount() == 0) {
            System.out.println("Average paid satisfaction: N/A");
        } else {
            System.out.printf(Locale.US, "Average paid satisfaction: %.1f%n",
                controller.getAveragePaidSatisfaction());
        }
        System.out.printf("Turns used: %d of 18%n", controller.getTurnsUsed());
    }

    public void displayResult(GameResult result) {
        System.out.println("Game over! Result: " + result);
    }
}
