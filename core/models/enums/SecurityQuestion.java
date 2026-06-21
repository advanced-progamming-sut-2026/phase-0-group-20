package models.enums;

public enum SecurityQuestion {
    PET_NAME("what is your pet's name?\n"),
    BIRTHDAY("what is your birthday?\n"),
    FAVORITE_COLOR("what is your favorite color?\n"),
    FAVORITE_NAME("what is your favorite name?\n"),
    FAVORITE_NUMBER("what is your favorite number?\n");


    private final String question;

    SecurityQuestion(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }


}
