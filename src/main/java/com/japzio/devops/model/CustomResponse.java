package com.japzio.devops.model;

import lombok.*;

@AllArgsConstructor
public class CustomResponse {
    public boolean isSuccessful;
    public int responseCode;
}
