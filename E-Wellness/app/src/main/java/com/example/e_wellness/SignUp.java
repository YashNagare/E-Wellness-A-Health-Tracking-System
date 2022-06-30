package com.example.e_wellness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "TAG";

    EditText sFirstName, sLastName, sPassword, sMobile, sEmail;
    Button sRegisterbtn;
    TextView bktlogin;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        sFirstName = (EditText) findViewById(R.id.FirstName);
        sLastName = (EditText) findViewById(R.id.LastName);
        sPassword = (EditText) findViewById(R.id.Password);
        sMobile = (EditText) findViewById(R.id.MobileNo);
        sEmail = (EditText) findViewById(R.id.Email);
        sRegisterbtn = (Button) findViewById(R.id.Register);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();


        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), HomePage.class));
            finish();
        }

        sRegisterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String firstname = sFirstName.getText().toString().trim();
                final String lastname = sLastName.getText().toString().trim();
                final String email = sEmail.getText().toString().trim();
                String password = sPassword.getText().toString().trim();
                final String mobile = sMobile.getText().toString().trim();
                final String uuid = UUID();

                if (TextUtils.isEmpty(firstname)) {
                    sFirstName.setError("FirstName Required");
                    return;
                }
                if (TextUtils.isEmpty(lastname)) {
                    sLastName.setError("LastName Required");
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    sEmail.setError("Email Required");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    sPassword.setError("Password Required");
                    return;
                }
                if (TextUtils.isEmpty(mobile)) {
                    sMobile.setError("Mobile No Required");
                    return;
                }
                if (password.length() < 6) {
                    sPassword.setError("Password must be >= 6 characters");
                }

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("Users").document(userID).collection("Personal Details").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("UserName", uuid);
                            user.put("FirstName", firstname);
                            user.put("LastName", lastname);
                            user.put("Email", email);
                            user.put("Mobile", mobile);

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: User Profile is created for " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), HealthDetails.class));
                        }
                        else {
                            Toast.makeText(SignUp.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });


        bktlogin = (TextView) findViewById(R.id.backtologin);
        bktlogin();
    }

    private void bktlogin() {
        bktlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private String UUID(){
        byte[] array = new byte[256];
        new Random().nextBytes(array);
        int n = 6;

        String randomString = new String(array, StandardCharsets.UTF_8);
        StringBuffer r = new StringBuffer();

        for (int k = 0; k < randomString.length(); k++) {
            char ch = randomString.charAt(k);
            if ((ch >= 'A' && ch <='Z') && (n != 4)) {
                r.append(ch);
                n--;
            }
        }

        r.append('#');

        for (int k = 0; k < randomString.length(); k++) {

            char ch = randomString.charAt(k);

            if ((ch >= '0' && ch <= '9') && (n > 0)) {
                r.append(ch);
                n--;
            }
        }
        return String.valueOf(r);

    }

}