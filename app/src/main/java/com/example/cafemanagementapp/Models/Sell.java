package com.example.cafemanagementapp.Models;

public class Sell {
    private String id;
    private String sellDate;
    private String idDrink;
    private int unitPrice;
    private int quantity;
    private int totalPrice;

    public Sell() {
    }

    public Sell(String id, String sellDate, String idDrink, int unitPrice, int quantity, int totalPrice) {
        this.id = id;
        this.sellDate = sellDate;
        this.idDrink = idDrink;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "Sell{" +
                "id='" + id + '\'' +
                ", sellDate='" + sellDate + '\'' +
                ", idDrink='" + idDrink + '\'' +
                ", unitPrice=" + unitPrice +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSellDate() {
        return sellDate;
    }

    public void setSellDate(String sellDate) {
        this.sellDate = sellDate;
    }

    public String getIdDrink() {
        return idDrink;
    }

    public void setIdDrink(String idDrink) {
        this.idDrink = idDrink;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
}
