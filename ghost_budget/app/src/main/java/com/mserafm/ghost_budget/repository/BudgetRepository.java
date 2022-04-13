package com.mserafm.ghost_budget.repository;

import com.mserafm.ghost_budget.model.Expense;
import com.mserafm.ghost_budget.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BudgetRepository {

    private static final String TAG = "BudgetRepository class: ";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String collectionUsers = "users";
    User user;

    public Task<Void> addUser(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("limit", user.getLimit());

        DocumentReference documentReference = db.collection(collectionUsers).document(user.getEmail());
        return documentReference.set(userData);
    }

    public DocumentReference getLimit(String email){
        DocumentReference docRef = db.collection(collectionUsers).document(email);
        return docRef;
    }

    public Task<Void> updateLimit(String email, float limit) {
        DocumentReference docRef = db.collection(collectionUsers).document(email);
        return docRef.update("limit", limit);
    }

    public Task<Void> addMonth(String email, String month) {
        DocumentReference docRef = db.collection(collectionUsers).document(email);

        Map<String, Object> monthData = new HashMap<>();
        monthData.put("month", month);

        String collectionName = email + "_months";
        String documentName = email + "_" + month;

        DocumentReference newDocRef = docRef.collection(collectionName).document(documentName);
        return newDocRef.set(monthData);

    }

    public Task<Void> addExpense(String email, String month, Expense expense, int itemN) {

        String collectionName = email + "_months";
        String documentName = email + "_" + month;
        DocumentReference docRef = db.collection(collectionUsers).document(email).collection(collectionName).document(documentName);

        Map<String, Object> expenseData = new HashMap<>();
        expenseData.put("date", expense.getDate());
        expenseData.put("name", expense.getName());
        expenseData.put("cost", expense.getCost());
        expenseData.put("chart", expense.getChart());
        expenseData.put("type", expense.getType());
        expenseData.put("repetition", expense.getRepetition());

        String nestedName = "item_" + String.valueOf(itemN);

        Map<String, Object> monthData = new HashMap<>();
        monthData.put(nestedName, expenseData);

        return docRef.update(monthData);
    }

    public Task<Void> deleteExpense(String email, String month, Expense expense){

        String collectionName = email + "_months";
        String documentName = email + "_" + month;
        DocumentReference docRef = db.collection(collectionUsers).document(email).collection(collectionName).document(documentName);

        Map<String,Object> expenseData = new HashMap<>();
        expenseData.put(expense.getKey() + ".date", FieldValue.delete());
        expenseData.put(expense.getKey() + ".name", FieldValue.delete());
        expenseData.put(expense.getKey() + ".cost", FieldValue.delete());
        expenseData.put(expense.getKey() + ".chart", FieldValue.delete());
        expenseData.put(expense.getKey() + ".type", FieldValue.delete());
        expenseData.put(expense.getKey() + ".repetition", FieldValue.delete());

        return docRef.update(expenseData);

    }


    public Task<Void> updateExpense(String email, String month, Expense currentExpense, Expense updatedExpense) {
        String collectionName = email + "_months";
        String documentName = email + "_" + month;
        DocumentReference docRef = db.collection(collectionUsers).document(email).collection(collectionName).document(documentName);

        Map<String,Object> expenseData = new HashMap<>();
        expenseData.put("date", updatedExpense.getDate());
        expenseData.put("name", updatedExpense.getName());
        expenseData.put("cost", updatedExpense.getCost());
        expenseData.put("chart", updatedExpense.getChart());
        expenseData.put("type", updatedExpense.getType());
        expenseData.put("repetition", updatedExpense.getRepetition());

        Map<String, Object> monthData = new HashMap<>();
        monthData.put(currentExpense.getKey(), expenseData);

        return docRef.update(monthData);
    }


    public DocumentReference getMonthExpenses(String email, String month){
        String collectionName = email + "_months";
        String documentName = email + "_" + month;
        DocumentReference docRef = db.collection(collectionUsers).document(email).collection(collectionName).document(documentName);
        return docRef;
    }

}