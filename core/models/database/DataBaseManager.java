package models.database;

import models.users.PasswordUtils;
import models.users.User;

import java.util.List;


public class DataBaseManager {

    private static UserRepository userRepository = new UserRepository();

    public static void initializeDatabase() {

        System.out.println("JSON user storage ready");
    }

    public static void saveOrUpdateUser(User user) {
        userRepository.save(user);
    }

    public static User getLoggedInUser() {
        return userRepository.findLoggedInUser().orElse(null);
    }

    public static void logoutUser(String id) {
        userRepository.logout(id);
    }

    public static User authenticateUser(String username, String password) {
        return userRepository.authenticate(username, password).orElse(null);
    }

    public static User getUserForRecovery(String username, String email) {
        return userRepository.findForRecovery(username, email).orElse(null);
    }

    public static void updateForgotPassword(String username, String newPasswordHash) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setPasswordHash(newPasswordHash);
            userRepository.save(user);
        });
    }

    public static boolean updateUsername(User currentUser, String newUsername) {
        currentUser.setUsername(newUsername);
        userRepository.save(currentUser);
        return true;
    }

    public static void updateEmail(User currentUser, String newEmail) {
        currentUser.setEmail(newEmail);
        userRepository.save(currentUser);
    }

    public static void updateNickname(User currentUser, String newNickname) {
        currentUser.setNickname(newNickname);
        userRepository.save(currentUser);
    }

    public static boolean updatePassword(User currentUser, String oldPasswordInput, String newPasswordInput) {
        String oldPasswordHash = PasswordUtils.hashPassword(oldPasswordInput);

        if (!oldPasswordHash.equals(currentUser.getPasswordHash()))
            return false;

        String newPasswordHash = PasswordUtils.hashPassword(newPasswordInput);
        currentUser.setPasswordHash(newPasswordHash);
        userRepository.save(currentUser);
        return true;
    }

    public static boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public static List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public static void setRepositoryForTest(UserRepository repository) {
        DataBaseManager.userRepository = repository;
    }

    public static void resetRepositoryToDefault() {
        DataBaseManager.userRepository = new UserRepository();
    }

}