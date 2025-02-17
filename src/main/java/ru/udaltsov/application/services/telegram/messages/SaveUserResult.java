package ru.udaltsov.application.services.telegram.messages;

public class SaveUserResult {
    public static class Success extends SaveUserResult {}

    public static class AlreadyExists extends SaveUserResult {}

    public static class Failure extends SaveUserResult {}
}
