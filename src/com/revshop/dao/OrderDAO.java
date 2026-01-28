package com.revshop.dao;

import com.revshop.model.Order;
import com.revshop.model.OrderItem;
import com.revshop.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class OrderDAO {

    public int placeOrder(Order order, List<OrderItem> items) {
        String orderSql = "INSERT INTO orders (user_id, total_amount, status) VALUES (?, ?, ?)";
        String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        String stockSql = "UPDATE products SET stock = stock - ? WHERE product_id = ?";
        String clearCartSql = "DELETE FROM cart WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false); // Start transaction

            // 1️⃣ Insert Order
            PreparedStatement psOrder = con.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, order.getUserId());
            psOrder.setDouble(2, order.getTotalAmount());
            psOrder.setString(3, order.getStatus());
            psOrder.executeUpdate();

            ResultSet rs = psOrder.getGeneratedKeys();
            if (!rs.next()) {
                con.rollback();
                return 0;
            }
            int orderId = rs.getInt(1);

            // 2️⃣ Insert Order Items + Reduce Stock
            for (OrderItem item : items) {
                PreparedStatement psItem = con.prepareStatement(itemSql);
                psItem.setInt(1, orderId);
                psItem.setInt(2, item.getProductId());
                psItem.setInt(3, item.getQuantity());
                psItem.setDouble(4, item.getPrice());
                psItem.executeUpdate();

                PreparedStatement psStock = con.prepareStatement(stockSql);
                psStock.setInt(1, item.getQuantity());
                psStock.setInt(2, item.getProductId());
                psStock.executeUpdate();
            }

            // 3️⃣ Clear Cart
            PreparedStatement psClear = con.prepareStatement(clearCartSql);
            psClear.setInt(1, order.getUserId());
            psClear.executeUpdate();

            con.commit(); // End transaction
            return orderId;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
