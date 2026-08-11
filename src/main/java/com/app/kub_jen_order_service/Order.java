package com.app.kub_jen_order_service;

public class Order {

    private Long id;
    private String product;
    private double amount;

    public Order() {
    }

    public Order(Long id, String product, double amount) {
        this.id = id;
        this.product = product;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}