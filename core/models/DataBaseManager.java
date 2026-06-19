package models;

import models.enums.Gender;
import models.enums.SecurityQuestion;
import models.users.PasswordUtils;
import models.users.User;

import java.sql.*;

public class DataBaseManager {

    private static final String URL = "jdbc:sqlite:pvz2_game.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + " id TEXT PRIMARY KEY,"
                + " username TEXT UNIQUE NOT NULL,"
                + " password_hash TEXT NOT NULL,"
                + " nickname TEXT,"
                + " email TEXT,"
                + " gender TEXT,"
                + " security_question TEXT,"
                + " security_answer_hash TEXT,"
                + " coin INTEGER DEFAULT 0,"
                + " diamond INTEGER DEFAULT 0,"
                + " games_played INTEGER DEFAULT 0,"
                + " levels_completed INTEGER DEFAULT 0,"
                + " stay_logged_in INTEGER DEFAULT 0" // 0 = false, 1 = true
                + ");";

        // create setting table (for next)


        try (Connection conn = connect();
        Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");

            stmt.execute(createUsersTable);
            // for settings

            System.out.println("Connected to database successfully");

        } catch (SQLException e) {
            System.err.println("Error connecting to database" + e.getMessage());
        }
    }

    public static void saveOrUpdateUser(User user) {
        String sql = "INSERT INTO users(id, username, password_hash, nickname, email, gender, "
                + "security_question, security_answer_hash, coin, diamond, games_played, levels_completed, stay_logged_in) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?) "
                + "ON CONFLICT(id) DO UPDATE SET "
                + "username=excluded.username, password_hash=excluded.password_hash, nickname=excluded.nickname, "
                + "email=excluded.email, gender=excluded.gender, security_question=excluded.security_question, "
                + "security_answer_hash=excluded.security_answer_hash, coin=excluded.coin, diamond=excluded.diamond, "
                + "games_played=excluded.games_played, levels_completed=excluded.levels_completed, stay_logged_in=excluded.stay_logged_in;";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getNickname());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getGender() != null ? user.getGender().name() : null);
            pstmt.setString(7, user.getSecurityQuestion() != null ? user.getSecurityQuestion().name() : null);
            pstmt.setString(8, user.getSecurityAnswerHash());
            pstmt.setInt(9, user.getCoin());
            pstmt.setInt(10, user.getDiamond());
            pstmt.setInt(11, user.getGamesPlayed());
            pstmt.setInt(12, user.getLevelsCompleted());
            pstmt.setInt(13, user.isStayLoggedIn() ? 1 : 0);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving user" + e.getMessage());
        }
    }

    public static User getLoggedInUser() {
        String sql = "SELECT * FROM users WHERE stay_logged_in = 1 LIMIT 1;";

        try (
            Connection conn = connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                String genderStr = rs.getString("gender");
                Gender gender = genderStr != null ? Gender.valueOf(genderStr) : null;

                String sqStr = rs.getString("security_question");
                SecurityQuestion sq = sqStr != null ? SecurityQuestion.valueOf(sqStr) : null;

                return new User(
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("nickname"),
                        rs.getString("email"),
                        gender,
                        sq,
                        rs.getString("security_answer_hash"),
                        rs.getInt("coin"),
                        rs.getInt("diamond"),
                        rs.getInt("games_played"),
                        rs.getInt("levels_completed"),
                        true
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting logged in user" + e.getMessage());
        }

        return null;

    }

    public static void logoutUser(String id) {
        String sql = "UPDATE users SET stay_logged_in = 0 WHERE id = ?;";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error Logout User" + e.getMessage());
        }
    }

    public static User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?;";

        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPasswordHash = rs.getString("password_hash");
                    String inputPasswordHash = PasswordUtils.hashPassword(password);

                    if (storedPasswordHash.equals(inputPasswordHash)) {
                        String genderStr = rs.getString("gender");
                        Gender gender = genderStr != null ? Gender.valueOf(genderStr) : null;

                        String sqStr = rs.getString("security_question");
                        SecurityQuestion sq = sqStr != null ? SecurityQuestion.valueOf(sqStr) : null;

                        return new User(
                                rs.getString("id"),
                                rs.getString("username"),
                                storedPasswordHash,
                                rs.getString("nickname"),
                                rs.getString("email"),
                                gender,
                                sq,
                                rs.getString("security_answer_hash"),
                                rs.getInt("coin"),
                                rs.getInt("diamond"),
                                rs.getInt("games_played"),
                                rs.getInt("levels_completed"),
                                rs.getInt("stay_logged_in") == 1
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting logged in user" + e.getMessage());
        }

        return  null;
    }

    public static User getUserForRecovery(String username, String email) {
        String sql = "SELECT * FROM users WHERE username = ? AND email = ?;";

        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String genderStr = rs.getString("gender");
                    Gender gender = genderStr != null ? Gender.valueOf(genderStr) : null;

                    String sqStr = rs.getString("security_question");
                    SecurityQuestion sq = sqStr != null ? SecurityQuestion.valueOf(sqStr) : null;

                    return new User(
                            rs.getString("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("nickname"),
                            rs.getString("email"),
                            gender,
                            sq,
                            rs.getString("security_answer_hash"),
                            rs.getInt("coin"),
                            rs.getInt("diamond"),
                            rs.getInt("games_played"),
                            rs.getInt("levels_completed"),
                            rs.getInt("stay_logged_in") == 1
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user for recovery" + e.getMessage());
        }

        return null;
    }

    public static void updateForgotPassword(String username, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?;";

        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPasswordHash);
            pstmt.setString(2, username);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating password for user" + e.getMessage());
        }
    }

    public static boolean updateUsername(User currentUser, String newUsername) {
        String sql = "UPDATE users SET username = ? WHERE id = ?;";

        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newUsername);
            pstmt.setString(2, currentUser.getId());
            pstmt.executeUpdate();
            currentUser.setUsername(newUsername);
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating username " + e.getMessage());
            return false;
        }
    }

    public static void updateEmail(User currentUser, String newEmail) {
        String sql = "UPDATE users SET email = ? WHERE id = ?;";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newEmail);
            pstmt.setString(2, currentUser.getId());
            pstmt.executeUpdate();
            currentUser.setEmail(newEmail);
            System.out.println("email updated");

        } catch (SQLException e) {
            System.err.println("Error updating email " + e.getMessage());
        }
    }

    public static void updateNickname(User currentUser, String newNickname) {
        String sql = "UPDATE users SET nickname = ? WHERE id = ?;";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newNickname);
            pstmt.setString(2, currentUser.getId());
            pstmt.executeUpdate();
            currentUser.setNickname(newNickname);
            System.out.println("Nickname updated");

        } catch (SQLException e) {
            System.err.println("Error updating nickname " + e.getMessage());
        }
    }

    public static boolean updatePassword(User currentUser, String oldPasswordInput, String newPasswordInput) {
        String oldPasswordHash = PasswordUtils.hashPassword(oldPasswordInput);

        if (!oldPasswordHash.equals(currentUser.getPasswordHash())) {
            System.out.println("Old password does not match");
            return false;
        }

        String newPasswordHash = PasswordUtils.hashPassword(newPasswordInput);
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?;";

        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPasswordHash);
            pstmt.setString(2, currentUser.getId());
            pstmt.executeUpdate();

            currentUser.setPasswordHash(newPasswordHash);
            System.out.println("Password updated");
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating password " + e.getMessage());
            return false;
        }
    }
}
