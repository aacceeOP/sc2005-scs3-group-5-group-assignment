package restaurantrush.model.customer;

import java.util.List;
import java.util.Random;
import restaurantrush.model.menu.Menu;
import restaurantrush.model.menu.MenuItem;

/**
 * VIP customer with lower waiting satisfaction loss
 * and a 10% discount.
 */
public class VIPCustomer extends Customer {
    private final Random random = new Random(67890);

    public VIPCustomer(int id) {
        super(id);
    }

    @Override
    protected int getSatisfactionLossRate() {
        return 5;
    }

    @Override
    public double getDiscountRate() {
        return 0.10;
    }

    @Override
    public MenuItem chooseMenuItem(Menu menu) {
        List<MenuItem> available = getAvailableMenuItems(menu);
        return available.get(random.nextInt(available.size()));
    }
}
