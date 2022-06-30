package com.example.e_wellness;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class PredictAsthma extends AppCompatActivity {

    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String userID;

    EditText SpO2, heartrate, respiration;
    Float oxygen, heart_rate, respiration_val;
    TextView asthma_result;
    Button predict_asthma;

    private static final String TAG = "PredictAsthma";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict_asthma);

        SpO2 = (EditText) findViewById(R.id.oxygen);
        heartrate = (EditText) findViewById(R.id.heartrate);
        respiration = (EditText) findViewById(R.id.respiration);
        asthma_result = (TextView) findViewById(R.id.display_asthma_result);
        predict_asthma = (Button) findViewById(R.id.predict);


        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        Intent intent = getIntent();
        oxygen = Float.valueOf(intent.getStringExtra("SpO2"));
        SpO2.setText(String.valueOf(oxygen));
        heart_rate = Float.valueOf(intent.getStringExtra("Heart_Rate"));
        heartrate.setText(String.valueOf(heart_rate));
        respiration_val = Float.valueOf(intent.getStringExtra("Respiratory_Rate"));
        respiration.setText(String.valueOf(respiration_val));


        DocumentReference documentReference = fStore.collection("Users").document(userID).collection("Watch Details").document(userID);
        Map<String, Object> user = new HashMap<>();

        user.put("Respiratory Rate Count", String.valueOf(respiration_val));

        documentReference.update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
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

        predict_asthma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (oxygen < 95 && heart_rate > 100 && respiration_val > 20){
                    asthma_result.setText("You might have Asthma");
                    DocumentReference documentReference = fStore.collection("Users").document(userID).collection("Health Details").document(userID);
                    final Map<String, Object> user = new HashMap<>();
                    user.put("Asthma", 1);

                    documentReference.update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                }
                else {
                    asthma_result.setText("You Don't have Asthma");
                }
            }
        });

    }
}