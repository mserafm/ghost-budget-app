package com.mserafm.ghost_budget.model;

public class User {
    private String email="";
    private float limit=0;

    public User(String email) {
        this.email = email;
    }

    public User(String email, float limit) {
        this.email = email;
        this.limit = limit;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
}
