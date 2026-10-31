
package restaurantrush.boundary;

import java.util.Scanner;
import java.io.EOFException;

public class InputHandler {

    private final Scanner scanner;

    public InputHandler() {
        scanner = new Scanner(System.in);
    }
    public String readLine(String prompt) throws EOFException {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new EOFException("Terminal input ended.");
        }
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt) throws EOFException {
        while (true) {
            String input = readLine(prompt);

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(
                    "Invalid input. Please enter a whole number."
                );
            }
        }
    }
}
