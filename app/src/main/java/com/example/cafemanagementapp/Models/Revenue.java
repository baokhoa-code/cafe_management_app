package com.example.cafemanagementapp.Models;

public class Revenue {

    private String sellDate;
    private String idDrink;
    private int quantity;
    private int totalPrice;

    public Revenue() {
    }

    public Revenue(String sellDate, String idDrink, int quantity, int totalPrice) {
        this.sellDate = sellDate;
        this.idDrink = idDrink;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "Revenue{" +
                "sellDate='" + sellDate + '\'' +
                ", idDrink='" + idDrink + '\'' +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                '}';
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
