
package restaurantrush.boundary;

import java.util.Scanner;

public class InputHandler {

    private final Scanner scanner;

    public InputHandler() {
        scanner = new Scanner(System.in);
    }
    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt) {
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
