package io.java.pvz.models.users;

import io.java.pvz.models.enums.Gender;

public record PendingRegistration(String username, String passwordHash,
                                  String nickname, String email, Gender gender) {
}
