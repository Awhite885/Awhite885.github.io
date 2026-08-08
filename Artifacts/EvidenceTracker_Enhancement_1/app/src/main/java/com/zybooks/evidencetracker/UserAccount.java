package com.zybooks.evidencetracker;

public class UserAccount {

    private final int id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String username;
    private final String role;
    private final String requestDate;

    public UserAccount(
            int id,
            String firstName,
            String lastName,
            String email,
            String username,
            String role,
            String requestDate) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.role = role;
        this.requestDate = requestDate;
    }

    public int getId() {
        return id;
    }
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getRequestDate() {
        return requestDate;
    }
}