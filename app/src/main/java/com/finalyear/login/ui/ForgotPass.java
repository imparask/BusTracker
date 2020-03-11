package com.finalyear.login.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.finalyear.login.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ForgotPass extends AppCompatActivity {


    private static final String TAG = "ForgotPass" ;
    private EditText mForgotPassUsername;
    private Button mSendPass;
    private ProgressBar mProgressBar;

    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);


        mForgotPassUsername = findViewById(R.id.et_forgotpassusername);
        mSendPass = findViewById(R.id.bt_forgotpass);
        mProgressBar = findViewById(R.id.pgB_forgotpass);
        fAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        String emailid = intent.getStringExtra("emailid");
        mForgotPassUsername.setText(emailid);

        mSendPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                final String username = mForgotPassUsername.getText().toString();
                if(!(TextUtils.isEmpty(username))) {
                    fAuth.sendPasswordResetEmail(username).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(ForgotPass.this, "Password reset link sent to your email", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getApplicationContext(), Login.class));
                            } else {
                                mProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(ForgotPass.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else{
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mForgotPassUsername.setError("Email is required");
                }
            }
        });

    }
}
