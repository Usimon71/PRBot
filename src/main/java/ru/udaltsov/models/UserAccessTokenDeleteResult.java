package ru.udaltsov.models;

public abstract class UserAccessTokenDeleteResult {

    public static class Success extends UserAccessTokenDeleteResult {}

    public static class NotFound extends UserAccessTokenDeleteResult {}

    public static class Failure extends UserAccessTokenDeleteResult {}
}
