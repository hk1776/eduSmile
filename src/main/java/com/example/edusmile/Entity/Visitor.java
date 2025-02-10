package com.example.edusmile.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class Visitor {
    @Id
    private LocalDate date;

    @Column(nullable = false)
    private int count;


    public Visitor() {

    }
    public Visitor(LocalDate date) {
        this.date = date;
        this.count = 1;
    }
    public void increaseCount() {
        this.count++;
    }
}
