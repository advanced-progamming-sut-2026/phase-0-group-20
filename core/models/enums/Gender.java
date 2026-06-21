package models.enums;

public enum Gender {
    MALE("male"),
    FEMALE("female"),
    OTHER("other"),
    PREFERRED_NOT_TO_SAY("preferred not to say");

    private final String value;
    Gender(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Gender findByValue(String value) {
        for(Gender gender : Gender.values()) {
            if(gender.getValue().equalsIgnoreCase(value))
                return gender;
        }
        return null;
    }


}
