package com.mserafm.ghost_budget.model;

import java.util.Map;

public class Month {
    private String id;
    private String month;
    private Map<String, Expense> expenses;

    public Month(){}

    public Month(String month) {
        this.month = month;
    }

    public Month(String month, Map<String, Expense> expenses) {
        this.month = month;
        this.expenses = expenses;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Map<String, Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(Map<String, Expense> expenses) {
        this.expenses = expenses;
    }
}
