package com.zybooks.evidencetracker;

public class AuthenticationResult {

    private final boolean credentialsValid;
    private final int accountStatus;
    private final String role;

    public AuthenticationResult(
            boolean credentialsValid,
            int accountStatus,
            String role) {

        this.credentialsValid = credentialsValid;
        this.accountStatus = accountStatus;
        this.role = role;
    }

    public boolean areCredentialsValid() {
        return credentialsValid;
    }

    public int getAccountStatus() {
        return accountStatus;
    }

    public String getRole() {
        return role;
    }
}
