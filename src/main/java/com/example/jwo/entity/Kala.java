package com.example.jwo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Kala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String laji;
    private Double pituus;
    private Double paino;

    @ManyToOne
    private User user; // Reference to the user who added the fish

    public Kala() {
    }

    public Kala(String laji, Double pituus, Double paino, User user) {
        this.laji = laji;
        this.pituus = pituus;
        this.paino = paino;
        this.user = user;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLaji() {
        return laji;
    }

    public void setLaji(String laji) {
        this.laji = laji;
    }

    public Double getPituus() {
        return pituus;
    }

    public void setPituus(Double pituus) {
        this.pituus = pituus;
    }

    public Double getPaino() {
        return paino;
    }

    public void setPaino(Double paino) {
        this.paino = paino;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}