package com.aibert.dosw.domain.model.valueObjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserId")
class UserIdTest {

    @Test
    @DisplayName("creates successfully with a positive value")
    void createsWithPositiveValue() {
        UserId userId = new UserId(1L);
        assertThat(userId.value()).isEqualTo(1L);
    }

    @Test
    @DisplayName("throws when value is null")
    void throwsWhenNull() {
        assertThatThrownBy(() -> new UserId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserId must be a positive number");
    }

    @Test
    @DisplayName("throws when value is zero")
    void throwsWhenZero() {
        assertThatThrownBy(() -> new UserId(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserId must be a positive number");
    }

    @Test
    @DisplayName("throws when value is negative")
    void throwsWhenNegative() {
        assertThatThrownBy(() -> new UserId(-5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserId must be a positive number");
    }

    @Test
    @DisplayName("two UserIds with same value are equal")
    void equalityBySameValue() {
        assertThat(new UserId(42L)).isEqualTo(new UserId(42L));
    }

    @Test
    @DisplayName("two UserIds with different value are not equal")
    void inequalityByDifferentValue() {
        assertThat(new UserId(1L)).isNotEqualTo(new UserId(2L));
    }
}
