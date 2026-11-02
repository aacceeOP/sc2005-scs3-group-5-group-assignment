package restaurantrush;

import java.util.List;

import restaurantrush.boundary.GameCLI;
import restaurantrush.control.GameController;
import restaurantrush.control.GameState;
import restaurantrush.model.Table;
import restaurantrush.model.customer.RegularCustomer;
import restaurantrush.model.customer.VIPCustomer;
import restaurantrush.model.customer.CriticCustomer;
import restaurantrush.model.menu.Menu;
import restaurantrush.model.pricing.StandardPricingPolicy;
import restaurantrush.model.staff.Cashier;
import restaurantrush.model.staff.Chef;
import restaurantrush.model.staff.Waiter;
import restaurantrush.model.staff.Host;

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
        Host host = new Host();

        GameState gameState = new GameState();
        gameState.configureRestaurant(
        	    tables, waiter, chef, cashier, host
        	);

        // Stage 2: Schedule six customers, including a VIP and a Critic.
        gameState.scheduleCustomerArrival(1, new RegularCustomer(1));
        gameState.scheduleCustomerArrival(4, new VIPCustomer(2));
        gameState.scheduleCustomerArrival(7, new RegularCustomer(3));
        gameState.scheduleCustomerArrival(10, new CriticCustomer(4));
        gameState.scheduleCustomerArrival(13, new RegularCustomer(5));
        gameState.scheduleCustomerArrival(16, new RegularCustomer(6));

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
