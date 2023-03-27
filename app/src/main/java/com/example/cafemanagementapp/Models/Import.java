package com.example.cafemanagementapp.Models;

public class Import {
    private String id;
    private String importDate;
    private String idDrink;
    private int unitPrice;
    private int quantity;
    private int totalPrice;

    public Import() {
    }

    public Import(String id, String importDate, String idDrink, int unitPrice, int quantity, int totalPrice) {
        this.id = id;
        this.importDate = importDate;
        this.idDrink = idDrink;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "Import{" +
                "id='" + id + '\'' +
                ", importDate='" + importDate + '\'' +
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
