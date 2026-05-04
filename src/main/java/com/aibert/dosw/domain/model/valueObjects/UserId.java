package com.aibert.dosw.domain.model.valueObjects;

public record UserId(Long value) {

    public UserId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("UserId must be a positive number");
        }
    }
}
