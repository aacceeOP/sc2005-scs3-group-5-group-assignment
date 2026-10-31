package restaurantrush;

import java.util.List;

import restaurantrush.boundary.GameCLI;
import restaurantrush.control.GameController;
import restaurantrush.control.GameState;
import restaurantrush.model.Table;
import restaurantrush.model.customer.RegularCustomer;
import restaurantrush.model.menu.Menu;
import restaurantrush.model.pricing.StandardPricingPolicy;
import restaurantrush.model.staff.Cashier;
import restaurantrush.model.staff.Chef;
import restaurantrush.model.staff.Waiter;

/**
 * Main.java
 * Stage: 1
 * Responsibility: Configure the restaurant and start the game.
 */
public class Main {

    public static void main(String[] args) {
        // Set up two tables and one of each Stage 1 staff member.
        List<Table> tables = List.of(
            new Table(1),
            new Table(2)
        );

        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        Cashier cashier = new Cashier();

        GameState gameState = new GameState();
        gameState.configureRestaurant(
            tables, waiter, chef, cashier
        );

        // Schedule six Regular customers.
        int[] arrivalTurns = {1, 4, 7, 10, 13, 16};

        for (int i = 0; i < arrivalTurns.length; i++) {
            gameState.scheduleCustomerArrival(
                arrivalTurns[i],
                new RegularCustomer(i + 1)
            );
        }

        // Menu already contains Salad, Burger and Pasta.
        Menu menu = new Menu();
        StandardPricingPolicy pricingPolicy =
            new StandardPricingPolicy();

        GameController controller = new GameController(
            gameState, menu, pricingPolicy
        );

        GameCLI cli = new GameCLI(controller);
        cli.run();
    }
}
