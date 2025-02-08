package ru.udaltsov.models;

public abstract class UserAccessTokenAddResult {

    public static class Success extends UserAccessTokenAddResult {}

    public static class AlreadyExists extends UserAccessTokenAddResult {}

    public static class Failure extends UserAccessTokenAddResult {}
}
