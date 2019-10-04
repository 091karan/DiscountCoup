package com.aakib78.hospitonadmin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    private Button emailLogin;
    private TextView forgotText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);
        emailLogin=(Button)findViewById(R.id.emailLogin);
        forgotText=(TextView)findViewById(R.id.forgotText);
        forgotText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,PassswordReset.class));
            }
        });

        emailLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailTxt=email.getText().toString();
                String passwordTxt=password.getText().toString();
                if (TextUtils.isEmpty(emailTxt) || TextUtils.isEmpty(passwordTxt )){
                    Toast.makeText(LoginActivity.this, "A valid email and password required.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(emailTxt, passwordTxt)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                        finish();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("Login", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Incorrect login or password.",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });



    }
}
