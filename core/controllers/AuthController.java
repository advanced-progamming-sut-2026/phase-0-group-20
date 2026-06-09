package controllers;

import models.Result;

public class AuthController {





    public static Result login(String username, String password) {
        return new Result(true, "Login successful");
    }


    private static Result isValidUsername(String username) {
        return new Result(true, "");
    }


    private static Result isValidPassword(String password) {
        return new Result(true, "");
    }


    private static Result isValidEmail(String email) {
        return new Result(true, "");
    }






}
