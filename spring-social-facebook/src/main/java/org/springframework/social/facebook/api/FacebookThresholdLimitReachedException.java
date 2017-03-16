package org.springframework.social.facebook.api;

public class FacebookThresholdLimitReachedException extends RuntimeException {
    public FacebookThresholdLimitReachedException() {
        super("The client has exceeded the API Calls threshold");
    }
}