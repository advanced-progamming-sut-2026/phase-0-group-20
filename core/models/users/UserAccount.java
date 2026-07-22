package models.users;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.Gender;
import models.enums.SecurityQuestion;

import java.util.UUID;

public class UserAccount {
    private final String id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private Gender gender;
    private SecurityQuestion securityQuestion;
    private String securityAnswerHash;
    private boolean stayLoggedIn;

    public UserAccount(String username, String passwordHash, String nickname, String email,
                       Gender gender, SecurityQuestion securityQuestion, String securityAnswerHash) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.securityQuestion = securityQuestion;
        this.securityAnswerHash = securityAnswerHash;
        this.stayLoggedIn = false;
    }

    @JsonCreator
    public UserAccount(@JsonProperty("id") String id, @JsonProperty("username") String username,
                       @JsonProperty("passwordHash") String passwordHash, @JsonProperty("nickname") String nickname,
                       @JsonProperty("email") String email, @JsonProperty("gender") Gender gender,
                       @JsonProperty("securityQuestion") SecurityQuestion securityQuestion,
                       @JsonProperty("securityAnswerHash") String securityAnswerHash,
                       @JsonProperty("stayLoggedIn") boolean stayLoggedIn) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.securityQuestion = securityQuestion;
        this.securityAnswerHash = securityAnswerHash;
        this.stayLoggedIn = stayLoggedIn;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public SecurityQuestion getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(SecurityQuestion securityQuestion) { this.securityQuestion = securityQuestion; }
    public String getSecurityAnswerHash() { return securityAnswerHash; }
    public void setSecurityAnswerHash(String securityAnswerHash) { this.securityAnswerHash = securityAnswerHash; }
    public boolean isStayLoggedIn() { return stayLoggedIn; }
    public void setStayLoggedIn(boolean stayLoggedIn) { this.stayLoggedIn = stayLoggedIn; }
}