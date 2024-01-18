package com.example.nadhir;

import java.util.Date;

public class CloudData {

    public String houseNumber,category,tenantName,tenantPhone,roomNumber,location;
    public double rentPrice;
    public Date dateOut;

    public CloudData() {
    }

    public CloudData(String houseNumber, String category, String tenantName, String tenantPhone, String roomNumber, String location, double rentPrice, Date dateOut) {
        this.houseNumber = houseNumber;
        this.category = category;
        this.tenantName = tenantName;
        this.tenantPhone = tenantPhone;
        this.roomNumber = roomNumber;
        this.location = location;
        this.rentPrice = rentPrice;
        this.dateOut = dateOut;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getTenantPhone() {
        return tenantPhone;
    }

    public void setTenantPhone(String tenantPhone) {
        this.tenantPhone = tenantPhone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getRentPrice() {
        return rentPrice;
    }

    public void setRentPrice(double rentPrice) {
        this.rentPrice = rentPrice;
    }

    public Date getDateOut() {
        return dateOut;
    }

    public void setDateOut(Date dateOut) {
        this.dateOut = dateOut;
    }

}
