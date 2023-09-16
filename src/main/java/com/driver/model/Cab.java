package com.driver.model;

import javax.persistence.*;

@Entity
public class Cab {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int Id;
    @Column(nullable = false)
    int perKmRate;
    boolean available;
    @OneToOne
    @JoinColumn
    Driver driver;

    public Cab() {
    }

    public Cab(int id, int perKmRate, boolean available, Driver driver) {
        Id = id;
        this.perKmRate = perKmRate;
        this.available = available;
        this.driver = driver;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getPerKmRate() {
        return perKmRate;
    }

    public void setPerKmRate(int perKmRate) {
        this.perKmRate = perKmRate;
    }

    public boolean getAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }
}