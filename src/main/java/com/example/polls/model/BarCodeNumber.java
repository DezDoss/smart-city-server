package com.example.polls.model;

import javax.persistence.*;

@Entity
@Table(name = "BAR_CODE_NUMBER")
public class BarCodeNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "CARD_ID")
    private int cardId;
    @Column(name = "BARCODE_NUMBER")
    private Long barCodeNumber;

    public Long getId() {
        return id;
    }


    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

   public Long getBarCodeNumber() {
        return barCodeNumber;
    }

    public void setBarCodeNumber(Long barCodeNumber) {
        this.barCodeNumber = barCodeNumber;
    }
}
