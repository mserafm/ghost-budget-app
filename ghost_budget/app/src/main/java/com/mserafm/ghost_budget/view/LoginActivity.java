package com.mserafm.ghost_budget.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.ghost_budget.R;
import com.mserafm.ghost_budget.model.User;
import com.mserafm.ghost_budget.viewmodel.BudgetViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;
    private Button btnSignUp, btnSignIn;
    private EditText etEmail, etPassword;
    private BudgetViewModel budgetViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.setTitle(getResources().getString(R.string.login));

        session();

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_pass);
        btnSignIn = findViewById(R.id.btn_signin);
        btnSignUp = findViewById(R.id.btn_signup);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etEmail.getText().toString().trim()) && !TextUtils.isEmpty(etPassword.getText().toString().trim())) {
                    createAccount(etEmail.getText().toString().trim(), etPassword.getText().toString().trim());
                }
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etEmail.getText().toString().trim()) && !TextUtils.isEmpty(etPassword.getText().toString().trim())) {
                    signIn(etEmail.getText().toString().trim(), etPassword.getText().toString().trim());
                }
            }
        });


    }

    private void session() {

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        String auth = sharedPreferences.getString("auth", null);
        String email = sharedPreferences.getString("email", null);
        LinearLayout layout = findViewById(R.id.auth_layout);

        if (auth != null) {
            setVisibilityChildren(layout, View.INVISIBLE);
            goHome(auth, email.toLowerCase(Locale.ROOT).trim());
        } else {
            setVisibilityChildren(layout, View.VISIBLE);
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        session();

    }

    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            budgetViewModel = new ViewModelProvider(LoginActivity.this).get(BudgetViewModel.class);
                            budgetViewModel.addUser(new User(email, 0));

                            goHome(user.toString(), email.toLowerCase(Locale.ROOT).trim());
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            toastShort(getString(R.string.auth_failed));
                        }
                    }
                });
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            goHome(user.toString(), email.toLowerCase(Locale.ROOT).trim());
                        } else {
                            toastShort(getString(R.string.auth_failed));
                        }
                    }
                });
    }

    public void toastShort(String sentence) {
        Toast.makeText(LoginActivity.this, sentence, Toast.LENGTH_SHORT).show();
    }

    public void toastLong(String sentence) {
        Toast.makeText(LoginActivity.this, sentence, Toast.LENGTH_LONG).show();
    }

    private void goHome(String auth, String email) {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.putExtra("auth", auth);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    private static void setVisibilityChildren(View view, int visibility) {
        view.setVisibility(visibility);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setVisibilityChildren(child, visibility);
            }
        }
    }


}