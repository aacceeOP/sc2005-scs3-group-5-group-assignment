// Stage 2 deferred: preserved for later integration.
// package restaurantrush.model.customer;
//
// import restaurantrush.model.menu.Menu;
// import restaurantrush.model.menu.MenuItem;
//
// public class CriticCustomer extends Customer {
//
//     public CriticCustomer(int id) {
//         super(id);
//     }
//
//     @Override
//     protected int getSatisfactionLossRate() {
//         return 12;
//     }
//
//     @Override
//     public double getDiscountRate() {
//         return 0.0;
//     }
//
//     @Override
//     public MenuItem chooseMenuItem(Menu menu) {
//         MenuItem mostExpensive = null;
//         int maxPriceCents = -1;
//
//         for (MenuItem item : getAvailableMenuItems(menu)) {
//             if (item.getPriceCents() > maxPriceCents) {
//                 maxPriceCents = item.getPriceCents();
//                 mostExpensive = item;
//             }
//         }
//         return mostExpensive;
//     }
// }
