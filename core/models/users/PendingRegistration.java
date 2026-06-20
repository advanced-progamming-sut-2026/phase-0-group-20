package models.users;

import models.enums.Gender;

public record PendingRegistration(String username, String passwordHash,
                                  String nickname, String email, Gender gender) {
}