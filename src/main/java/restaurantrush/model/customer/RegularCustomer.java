package restaurantrush.model.customer;

import java.util.List;
import java.util.Random;
import restaurantrush.model.menu.Menu;
import restaurantrush.model.menu.MenuItem;

public class RegularCustomer extends Customer {
    private final Random random = new Random(12345); 

    public RegularCustomer(int id) { 
        super(id); 
    }

    @Override
    protected int getSatisfactionLossRate() {
        return 8; 
    }

    @Override
    public double getDiscountRate() {
        return 0.0; 
    }

    @Override
    public String getCustomerType() {
        return "Regular";
    }

    @Override
    public MenuItem chooseMenuItem(Menu menu) {
        List<MenuItem> available = getAvailableMenuItems(menu);
        return available.get(random.nextInt(available.size()));
    }
}
