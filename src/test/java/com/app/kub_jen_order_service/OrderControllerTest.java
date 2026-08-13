package com.app.kub_jen_order_service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderControllerTest {

    @Test
    void shouldReturnOrder() {

        OrderController controller = new OrderController();

        Order order = controller.getOrder(100L);

        assertEquals(100L, order.getId());
        assertEquals("Laptop", order.getProduct());
        assertEquals(75000, order.getAmount());
    }

    @Test
    void shouldCancelOrder() {

        OrderController controller = new OrderController();

        String result = controller.cancelOrder(100L);

        assertEquals(
                "Order 100 cancelled successfully",
                result
        );
    }
}