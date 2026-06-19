package models.users;

import java.util.UUID;

public class IdGenerator {
    public static String generateUserID() {
        return UUID.randomUUID().toString();
    }
}
