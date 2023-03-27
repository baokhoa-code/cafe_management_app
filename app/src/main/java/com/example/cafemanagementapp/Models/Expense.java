package com.example.cafemanagementapp.Models;

public class Expense {

    private String importDate;
    private String idDrink;
    private int quantity;
    private int totalPrice;

    public Expense() {
    }

    public Expense(String importDate, String idDrink, int quantity, int totalPrice) {
        this.importDate = importDate;
        this.idDrink = idDrink;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "importDate='" + importDate + '\'' +
                ", idDrink='" + idDrink + '\'' +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                '}';
    }

    public String getImportDate() {
        return importDate;
    }

    public void setImportDate(String importDate) {
        this.importDate = importDate;
    }

    public String getIdDrink() {
        return idDrink;
    }

    public void setIdDrink(String idDrink) {
        this.idDrink = idDrink;
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
