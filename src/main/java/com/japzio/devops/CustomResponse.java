package com.japzio.devops;

import lombok.*;

@AllArgsConstructor
public class CustomResponse {
    public boolean isSuccessful;
    public int responseCode;
}
