package com.revshop.ui;

import com.revshop.dao.CartDAO;
import com.revshop.dao.OrderDAO;
import com.revshop.dao.ProductDAO;
import com.revshop.dao.UserDAO;
import com.revshop.model.Cart;
import com.revshop.model.Order;
import com.revshop.model.OrderItem;
import com.revshop.model.Product;
import com.revshop.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserDAO userDAO = new UserDAO();
        ProductDAO productDAO = new ProductDAO();
        CartDAO cartDAO = new CartDAO();
        OrderDAO orderDAO = new OrderDAO();

        System.out.println("===== Welcome to RevShop =====");

        while (true) {
            System.out.println("\n1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            if (choice == 1) {
                System.out.print("Enter Email: ");
                String email = sc.nextLine();
                System.out.print("Enter Password: ");
                String password = sc.nextLine();

                User user = userDAO.loginUser(email, password);
                if (user != null) {
                    System.out.println("✅ Login successful! Welcome " + user.getName());

                    if (user.getRole().equalsIgnoreCase("SELLER")) {
                        sellerMenu(user, productDAO, sc);
                    } else {
                        buyerMenu(user, productDAO, cartDAO, orderDAO, sc);
                    }

                } else {
                    System.out.println("❌ Invalid credentials!");
                }

            } else if (choice == 2) {
                System.out.print("Enter Name: ");
                String name = sc.nextLine();
                System.out.print("Enter Email: ");
                String email = sc.nextLine();
                System.out.print("Enter Password: ");
                String password = sc.nextLine();
                System.out.print("Enter Role (BUYER/SELLER): ");
                String role = sc.nextLine().toUpperCase();
                String business = null;

                if (role.equals("SELLER")) {
                    System.out.print("Enter Business Name: ");
                    business = sc.nextLine();
                }

                User newUser = new User(0, name, email, password, role, business);
                if (userDAO.registerUser(newUser)) {
                    System.out.println("✅ Registration successful! Please login.");
                } else {
                    System.out.println("❌ Registration failed!");
                }

            } else if (choice == 3) {
                System.out.println("Goodbye!");
                break;
            } else {
                System.out.println("❌ Invalid choice!");
            }
        }

        sc.close();
    }

    // Seller Menu
    private static void sellerMenu(User seller, ProductDAO productDAO, Scanner sc) {
        while (true) {
            System.out.println("\n--- Seller Menu ---");
            System.out.println("1. Add Product");
            System.out.println("2. View My Products");
            System.out.println("3. Update Stock");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            if (choice == 1) {
                System.out.print("Enter Product Name: ");
                String name = sc.nextLine();
                System.out.print("Enter Description: ");
                String desc = sc.nextLine();
                System.out.print("Enter Price: ");
                double price = sc.nextDouble();
                System.out.print("Enter Stock: ");
                int stock = sc.nextInt();
                sc.nextLine();

                if (productDAO.addProduct(new Product(seller.getUserId(), name, desc, price, stock))) {
                    System.out.println("✅ Product added successfully!");
                } else {
                    System.out.println("❌ Failed to add product!");
                }

            } else if (choice == 2) {
                System.out.println("--- My Products ---");
                List<Product> products = productDAO.getProductsBySeller(seller.getUserId());
                for (Product p : products) {
                    System.out.println(p.getProductId() + ": " + p.getName() + " | Price: " + p.getPrice() + " | Stock: " + p.getStock());
                }

            } else if (choice == 3) {
                System.out.print("Enter Product ID to update stock: ");
                int pid = sc.nextInt();
                System.out.print("Enter New Stock: ");
                int newStock = sc.nextInt();
                sc.nextLine();

                if (productDAO.updateStock(pid, newStock)) {
                    System.out.println("✅ Stock updated successfully!");
                } else {
                    System.out.println("❌ Failed to update stock!");
                }

            } else if (choice == 4) {
                System.out.println("Logging out...");
                break;
            } else {
                System.out.println("❌ Invalid choice!");
            }
        }
    }

    // Buyer Menu
    private static void buyerMenu(User buyer, ProductDAO productDAO, CartDAO cartDAO, OrderDAO orderDAO, Scanner sc) {
        while (true) {
            System.out.println("\n--- Buyer Menu ---");
            System.out.println("1. Browse Products");
            System.out.println("2. View Cart");
            System.out.println("3. Place Order");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            if (choice == 1) {
                System.out.println("--- All Products ---");
                List<Product> products = productDAO.getAllProducts();
                for (Product p : products) {
                    System.out.println(p.getProductId() + ": " + p.getName() + " | Price: " + p.getPrice() + " | Stock: " + p.getStock());
                }

                System.out.print("Enter Product ID to add to cart (0 to cancel): ");
                int pid = sc.nextInt();
                if (pid == 0) continue;

                System.out.print("Enter Quantity: ");
                int qty = sc.nextInt();
                sc.nextLine();

                if (cartDAO.addToCart(new Cart(buyer.getUserId(), pid, qty))) {
                    System.out.println("✅ Added to cart!");
                } else {
                    System.out.println("❌ Failed to add to cart!");
                }

            } else if (choice == 2) {
                System.out.println("--- Your Cart ---");
                List<Cart> cartItems = cartDAO.getCartItems(buyer.getUserId());
                if (cartItems.isEmpty()) {
                    System.out.println("Cart is empty!");
                } else {
                    for (Cart c : cartItems) {
                        Product p = productDAO.getProductById(c.getProductId());
                        System.out.println(c.getCartId() + ": " + p.getName() + " | Qty: " + c.getQuantity() + " | Price: " + p.getPrice());
                    }
                }

            } else if (choice == 3) {
                List<Cart> cartItems = cartDAO.getCartItems(buyer.getUserId());
                if (cartItems.isEmpty()) {
                    System.out.println("❌ Cart is empty!");
                } else {
                    double total = 0;
                    List<OrderItem> items = new ArrayList<>();
                    for (Cart c : cartItems) {
                        Product p = productDAO.getProductById(c.getProductId());
                        total += p.getPrice() * c.getQuantity();
                        items.add(new OrderItem(0, p.getProductId(), c.getQuantity(), p.getPrice()));
                    }

                    Order order = new Order(buyer.getUserId(), total, "PLACED");
                    int orderId = orderDAO.placeOrder(order, items);

                    if (orderId > 0) {
                        System.out.println("✅ Order placed! Order ID: " + orderId);
                    } else {
                        System.out.println("❌ Failed to place order!");
                    }
                }

            } else if (choice == 4) {
                System.out.println("Logging out...");
                break;
            } else {
                System.out.println("❌ Invalid choice!");
            }
        }
    }
}
