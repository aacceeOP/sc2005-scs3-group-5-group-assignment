/**
 * Menu.java
 * Stage: 1
 * Responsibility: Hold the available meals, including Salad, Burger and Pasta.
 */


package restaurantrush.model.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the meals available for customers to choose.
 * Prices are stored in integer cents.
 */
public class Menu {
    private final List<MenuItem> items;

    /**
     * Creates the required base menu.
     * Salad: $9.00, 1 preparation unit
     * Burger: $15.00, 1 preparation unit
     * Pasta: $18.00, 2 preparation units
     */
    public Menu() {
        items = new ArrayList<>();
        items.add(new MenuItem("Salad", 900, 1));
        items.add(new MenuItem("Burger", 1500, 1));
        items.add(new MenuItem("Pasta", 1800, 2));
    }

    /**
     * Adds a non-null menu item.
     * Duplicate item names are not allowed.
     */
    public void addItem(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException(
                "Menu item must not be null."
            );
        }

        for (MenuItem existing : items) {
            if (existing.getName().equalsIgnoreCase(item.getName())) {
                throw new IllegalArgumentException(
                    "A menu item with that name already exists."
                );
            }
        }

        items.add(item);
    }

    /**
     * Returns a read-only view of the menu items.
     */
    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Finds a menu item by name, ignoring letter case.
     * Returns null if no matching item exists.
     */
    public MenuItem getItemByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        for (MenuItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }

        return null;
    }
}
