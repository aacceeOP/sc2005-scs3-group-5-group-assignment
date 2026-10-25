
package restaurantrush.boundary;

import java.util.Scanner;

import restaurantrush.control.GameController;
import restaurantrush.control.GameResult;

public class GameCLI {

    private GameController controller;
    private final Scanner scanner;

    public GameCLI() {
        this.controller = new GameController();
        this.scanner = new Scanner(System.in);
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
        System.out.print("Enter your choice: ");
        return scanner.nextLine().trim();
    }
    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void displayResult(GameResult result) {
        System.out.println("Game over! Result: " + result);
    }
}
