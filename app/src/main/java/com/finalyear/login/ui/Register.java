package com.finalyear.login.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.finalyear.login.R;
import com.finalyear.login.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.finalyear.login.ui.StartActivity.exitConstant;

public class Register extends AppCompatActivity {
    public static final String TAG = "Register";
    EditText fullname,email,password,confirmpass;
    Button register;
    TextView loginbtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullname = findViewById(R.id.et_fullname);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_pass);
        confirmpass = findViewById(R.id.et_confirmpass);
        register = findViewById(R.id.bt_register);
        loginbtn = findViewById(R.id.tv_login);
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String pass = password.getText().toString();
                final String conpass = confirmpass.getText().toString();

                final User user = new User();
                user.setFullname(fullname.getText().toString());
                user.setEmail(email.getText().toString());

                if(TextUtils.isEmpty(user.getFullname())){
                    fullname.setError("Full Name is required");
                    return;
                }

                if(TextUtils.isEmpty(user.getEmail())){
                    email.setError("Email is required");
                    return;
                }

                if(TextUtils.isEmpty(pass)){
                    password.setError("Password is required");
                    return;
                }

                if(TextUtils.isEmpty(conpass)){
                    confirmpass.setError("Please confirm password");
                    return;
                }

                if(pass.length()<6){
                    password.setError("Password must be more than 6 characters");
                    return;
                }
                if(!pass.equals(conpass)){
                    confirmpass.setError("Incorrect password");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //Register the User
                fAuth.createUserWithEmailAndPassword(user.getEmail(),pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Register.this,"User Created",Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("Users").document(userID);
                            Map<String,Object> data = new HashMap<>();
                            data.put("fullname",user.getFullname());
                            data.put("email",user.getEmail());

                            documentReference.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG,"User profile created for "+userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG,"onFailure: "+e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), Login.class));
                        }
                        else{
                            Toast.makeText(Register.this,"Error.. ! "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(exitConstant !=100){
            startActivity(new Intent(getApplicationContext(),StartActivity.class));
        }
    }
}
