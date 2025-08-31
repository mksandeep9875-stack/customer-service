package com.menon;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "tokens")
@Getter
@Setter
public class Token {

    @Id
    String tokenValue;
    String phone;
    String status;
    Instant createdAt;
    Integer expiresIn; //seconds

}
