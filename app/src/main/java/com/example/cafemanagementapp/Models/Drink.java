package com.example.cafemanagementapp.Models;

import java.io.Serializable;

public class Drink implements Serializable {

    private String id;
    private String name;
    private String unit;
    private String description;
    private String image;
    private String manufacture;

    // Empty constructor
    public Drink() {}

    // Full constructor
    public Drink(String id, String name, String unit, String description, String image, String manufacture) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.description = description;
        this.image = image;
        this.manufacture = manufacture;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getManufacture() {
        return manufacture;
    }

    public void setManufacture(String manufacture) {
        this.manufacture = manufacture;
    }

    @Override
    public String toString() {
        return "Drink{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", manufacture='" + manufacture + '\'' +
                '}';
    }
}
