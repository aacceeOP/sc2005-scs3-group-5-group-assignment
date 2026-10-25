
package restaurantrush.boundary;

import java.util.Scanner;

import restaurantrush.control.GameController;
import restaurantrush.control.GameResult;

/**
 * GameCLI.java
 * Stage: 1
 * Responsibility: Display menus, game state, available tasks, validation
 * messages and results; collect manager input without implementing game rules.
 */
public class GameCLI {

    private GameController controller;
    private final Scanner scanner;

    public GameCLI() {
        this.controller = new GameController();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts and runs the game through the command-line interface.
     */
    public void run() {
        controller.startGame();

        GameResult result = controller.checkResult();

        while (result == GameResult.IN_PROGRESS) {
            displayState();

            String choice = getManagerChoice();
            controller.processManagerChoice(choice);
            controller.playTurn();

            result = controller.checkResult();
        }

        displayResult(result);
    }

    /**
     * Displays the current game state.
     * TODO: Implement after the GameState display format is agreed.
     */
    public void displayState() {
        // Placeholder for displaying customers, staff, orders and tables.
    }

    /**
     * Collects the manager's choice from the console.
     *
     * @return the manager's input
     */
    public String getManagerChoice() {
        System.out.print("Enter your choice: ");
        return scanner.nextLine().trim();
    }

    /**
     * Displays a message to the manager.
     *
     * @param message the message to display
     */
    public void displayMessage(String message) {
        System.out.println(message);
    }

    /**
     * Displays the final game result.
     *
     * @param result the result of the game
     */
    public void displayResult(GameResult result) {
        System.out.println("Game over! Result: " + result);
    }
}
