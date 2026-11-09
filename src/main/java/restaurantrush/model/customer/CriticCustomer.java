package restaurantrush.model.customer;

import restaurantrush.model.menu.Menu;
import restaurantrush.model.menu.MenuItem;

/**
 * Critic customer who prefers the most expensive menu item.
 * If prices tie, selects the first matching item in menu order.
 */

/**
 * Customer Type: CriticCustomer
 * 
 * Behaviour & Preferences:
 * 1. Waiting Loss: High impatience rate of 12 satisfaction points per unserved waiting turn.
 * 2. Menu Preference: Selects the single menu item with the highest price in cents.
 * 3. Tie-Breaking Rule: If multiple menu items share the highest price, selects the first matching item in menu order.
 * 4. Billing Rule: Pays full price (0.0 discount rate).
 */

public class CriticCustomer extends Customer {

    public CriticCustomer(int id) {
        super(id);
    }

    @Override
    protected int getSatisfactionLossRate() {
        return 12;
    }

    @Override
    public double getDiscountRate() {
        return 0.0;
    }

    @Override
    public String getCustomerType() {
        return "Critic";
    }

    @Override
    public MenuItem chooseMenuItem(Menu menu) {
        MenuItem mostExpensive = null;
        int maxPriceCents = -1;

        for (MenuItem item : getAvailableMenuItems(menu)) {
            if (item.getPriceCents() > maxPriceCents) {
                maxPriceCents = item.getPriceCents();
                mostExpensive = item;
            }
        }

        return mostExpensive;
    }
}
