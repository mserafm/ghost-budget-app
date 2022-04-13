package com.mserafm.ghost_budget.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mserafm.ghost_budget.model.Expense;
import com.mserafm.ghost_budget.model.User;
import com.mserafm.ghost_budget.repository.BudgetRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BudgetViewModel extends ViewModel {

    private static final String TAG = "BudgetViewModel class: ";

    private BudgetRepository budgetRepository = new BudgetRepository();
    private MutableLiveData<List<Expense>> monthlyExpenses = new MutableLiveData();
    private MutableLiveData<Float> limit = new MutableLiveData<>();

    public MutableLiveData<Integer> getItemCount() {
        return itemCount;
    }

    private MutableLiveData<Integer> itemCount = new MutableLiveData<>();

    private MutableLiveData<Boolean> finishJob = new MutableLiveData<>();


    public void addUser(User user) {
        budgetRepository.addUser(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Document succesfully written using on addUser()");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document on addUser()", e);
                    }
                });
    }

    public LiveData<Float> getLimit(String email){
        budgetRepository.getLimit(email)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Listen failed on getMonthlyExpenses().", error);
                            limit.setValue(0f);
                            return;
                        }

                        if (value != null && value.exists()) {
                            double aux = value.getDouble("limit");
                            limit.setValue((float) aux);
                        }
                    }
                });
        return limit;
    }


    public void updateLimit(String email, float limit) {
        budgetRepository.updateLimit(email, limit)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Document succesfully updated using on updateLimit()");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document on updateLimit()", e);
                    }
                });
        ;
    }

    public void addMonth(String email, String month) {
        budgetRepository.addMonth(email, month)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Document succesfully written using on addMonth()");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document on addMonth()", e);
                    }
                });
    }

    public void addExpense(String email, String month, Expense expense, int itemN) {
        budgetRepository.addExpense(email, month, expense, itemN).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "Document succesfully updated using on addExpense()");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document on addExpense()", e);
                    }
                });
    }

    public LiveData<List<Expense>> getMonthlyExpensesData(String email, String month) {
        budgetRepository.getMonthExpenses(email, month)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Listen failed on getMonthlyExpenses().", error);
                            monthlyExpenses.setValue(null);
                            return;
                        }

                        int i = 1;

                        if (value != null && value.exists()) {


                            if (value.get("item_1") != null) {

                                List<Expense> monthlyExpensesList = new ArrayList<>();

                                Map<String, Object> map = value.getData();
                                if (map != null) {

                                    for (int j =map.size(); j > 0; j--){
                                        String auxKey = "item_" + String.valueOf(j);
                                        if(value.getString(auxKey + ".date") !=null){
                                            Expense expense = new Expense(value.getString(auxKey + ".date"),
                                                    value.getString(auxKey+ ".name"),
                                                    value.getDouble(auxKey + ".cost"),
                                                    value.getString(auxKey + ".chart"),
                                                    value.getString(auxKey + ".type"),
                                                    value.getString(auxKey + ".repetition"));
                                            expense.setKey(auxKey);

                                            monthlyExpensesList.add(expense);
                                        }

                                        i++;
                                    }
                                }
                                monthlyExpenses.setValue(monthlyExpensesList);
                            }



                        } else {
                            addMonth(email, month);
                            Log.d(TAG, "Month added");
                        }
                        itemCount.setValue(i);

                    }
                });

        return monthlyExpenses;
    }

    public void deleteExpense(String email, String month, Expense expense) {
        budgetRepository.deleteExpense(email, month, expense).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "Document succesfully updated using on deleteExpense()");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document on deleteExpense()", e);
                    }
                });
    }


    public void updateExpense(String email, String month, Expense currentExpense, Expense updatedExpense) {
        budgetRepository.updateExpense(email, month, currentExpense, updatedExpense).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "Document succesfully updated using on updateExpense()");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document on updateExpense()", e);
                    }
                });
    }

}
