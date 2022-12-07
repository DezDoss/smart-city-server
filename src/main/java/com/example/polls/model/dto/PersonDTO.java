package com.example.polls.model.dto;

import lombok.Data;
import java.util.Date;

@Data
public class PersonDTO {
    private Long id;
    private String secondName;
    private String firstName;
    private String iin;
    private String phoneNumber;
    private String cardType;
    private Long cardId;
    private Long price;
    private String barCode;
    private String status;
    private Date createdDate;
}
