package ru.udaltsov.models;

public abstract class UserAccessTokenFindResult {
    public static class Success extends UserAccessTokenFindResult {

        public UserAccessToken userAccessToken;

        public Success(UserAccessToken userAccessToken) {
            this.userAccessToken = userAccessToken;
        }
    }

    public static class NotFound extends UserAccessTokenFindResult {}

    public static class Failure extends UserAccessTokenFindResult {}
}
