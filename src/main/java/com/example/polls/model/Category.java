package com.example.polls.model;


//import lombok.Data;

import javax.persistence.*;

//@Data
@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "category")
    private String category;
    @Column(name = "price")
    private int price;
    @Lob
    @Column(name = "front_blank")
    private byte[] frontBlank;
    @Lob
    @Column(name = "back_blank")
    private byte[] backBlank;
}
