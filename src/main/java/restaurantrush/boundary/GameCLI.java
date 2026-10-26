
package restaurantrush.boundary;

import restaurantrush.control.GameController;
import restaurantrush.control.GameResult;
import restaurantrush.model.Table;

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
            displayState();

            String answer = inputHandler.readLine(
                "Activate Happy Hour this turn? (y/n): "
            );
            boolean activateHappyHour =
                answer.equalsIgnoreCase("y");

            controller.beginTurn(activateHappyHour);

            while (!controller.isTurnComplete()) {
                String choice = inputHandler.readLine(
                    "Enter manager command: "
                );

                boolean accepted =
                    controller.processManagerChoice(choice);

                if (!accepted) {
                    displayMessage(
                        controller.getLastErrorMessage()
                    );
                }
            }

            controller.finishTurn();
        }

        displayResult(controller.checkResult());
    }

    public void displayState() {
        System.out.println("\n--- Restaurant Rush ---");
        System.out.println(
            "Turn: " + controller.getCurrentTurn()
        );
        System.out.println("--- Tables ---");

        for (Table table : controller.getTables()) {
            String status = table.isAvailable()
                ? "Available"
                : "Occupied";

            System.out.println(
                "Table " + table.getTableId() + ": " + status
            );
        }
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void displayResult(GameResult result) {
        System.out.println("Game over! Result: " + result);
    }
}
