package com.mserafm.ghost_budget.model;

public class Expense {
    private String date;
    private String name;
    private double cost;
    private String chart;
    private String type;
    private String repetition;
    private String key;


    public Expense(String date, String name, double cost, String chart, String type) {
        this.date = date;
        this.name = name;
        this.cost = cost;
        this.chart = chart;
        this.type = type;
    }


    public Expense(String date, String name, double cost, String chart, String type, String repetition) {
        this.date = date;
        this.name = name;
        this.cost = cost;
        this.chart = chart;
        this.type = type;
        this.repetition = repetition;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRepetition() {
        return repetition;
    }

    public void setRepetition(String repetition) {
        this.repetition = repetition;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
