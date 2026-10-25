
package restaurantrush.boundary;

import restaurantrush.control.GameController;
import restaurantrush.control.GameResult;

public class GameCLI {

    private GameController controller;
    private final InputHandler inputHandler;

    public GameCLI() {
        this.controller = new GameController();
        this.inputHandler = new InputHandler();
    }

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
    public void displayState() {
        // Placeholder for displaying customers, staff, orders and tables.
    }

    public String getManagerChoice() {
    return inputHandler.readLine("Enter your choice: ");

    }
    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void displayResult(GameResult result) {
        System.out.println("Game over! Result: " + result);
    }
}
