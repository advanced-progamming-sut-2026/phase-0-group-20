package com.Project.PVZ.models.users;

import com.Project.PVZ.models.enums.Gender;

public record PendingRegistration(String username, String passwordHash,
                                  String nickname, String email, Gender gender) {
}
