package com.example.polls.model;

//import lombok.Data;

import javax.persistence.*;

//@Data
@Entity
@Table(name = "hystorical_data")
public class HistorycalData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "second_name")
    private String secondName;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "iin")
    private String iin;
    @Column(name = "card_type")
    private String cardType;
    @Column(name = "barcode")
    private String barCode;

    public Long getId() {
        return id;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getIin() {
        return iin;
    }

    public void setIin(String iin) {
        this.iin = iin;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }
}
