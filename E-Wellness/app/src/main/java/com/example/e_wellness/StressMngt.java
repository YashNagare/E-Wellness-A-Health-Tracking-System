package com.example.e_wellness;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class StressMngt extends AppCompatActivity {

    private RadioGroup radioGroup1, radioGroup2, radioGroup3, radioGroup4, radioGroup5;
    Button btnPredict;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    float diabetes, chd, asthma;
    private static final String TAG = "StressMngt";
    TextView display_stress_level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stress_mngt);

        btnPredict = (Button)findViewById(R.id.btnPredict);
        radioGroup1 = (RadioGroup)findViewById(R.id.radioGroup1);
        radioGroup2 = (RadioGroup)findViewById(R.id.radioGroup2);
        radioGroup3 = (RadioGroup)findViewById(R.id.radioGroup3);
        radioGroup4 = (RadioGroup)findViewById(R.id.radioGroup4);
        radioGroup5 = (RadioGroup)findViewById(R.id.radioGroup5);
        radioGroup1.clearCheck();
        radioGroup2.clearCheck();
        radioGroup3.clearCheck();
        radioGroup4.clearCheck();
        radioGroup5.clearCheck();

        display_stress_level = (TextView) findViewById(R.id.display_stress_level);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userId = fAuth.getCurrentUser().getUid();


        DocumentReference documentReference = fStore.collection("Users").document(userId).collection("Health Details").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                diabetes = documentSnapshot.getLong("Diabetes");
                chd = documentSnapshot.getLong("CHD");
                asthma = documentSnapshot.getLong("Asthma");
            }
        });

        radioGroup();
    }


    private void radioGroup() {

        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                int Score = 0;
                int selectedId1 = radioGroup1.getCheckedRadioButtonId();
                int selectedId2 = radioGroup2.getCheckedRadioButtonId();
                int selectedId3 = radioGroup3.getCheckedRadioButtonId();
                int selectedId4 = radioGroup4.getCheckedRadioButtonId();
                int selectedId5 = radioGroup5.getCheckedRadioButtonId();
                if (selectedId1 == -1) {
                    Toast.makeText(StressMngt.this, "Q1 Answer not Selected", Toast.LENGTH_SHORT).show();
                }
                if (selectedId2 == -1) {
                    Toast.makeText(StressMngt.this, "Q2 Answer not Selected", Toast.LENGTH_SHORT).show();
                }
                if (selectedId3 == -1) {
                    Toast.makeText(StressMngt.this, "Q3 Answer not Selected", Toast.LENGTH_SHORT).show();
                }
                if (selectedId4 == -1) {
                    Toast.makeText(StressMngt.this, "Q4 Answer not Selected", Toast.LENGTH_SHORT).show();
                }
                if (selectedId5 == -1) {
                    Toast.makeText(StressMngt.this, "Q5 Answer not Selected", Toast.LENGTH_SHORT).show();
                }
                else {
                    RadioButton radioButton1 = (RadioButton)radioGroup1.findViewById(selectedId1);
                    RadioButton radioButton2 = (RadioButton)radioGroup2.findViewById(selectedId2);
                    RadioButton radioButton3 = (RadioButton)radioGroup3.findViewById(selectedId3);
                    RadioButton radioButton4 = (RadioButton)radioGroup4.findViewById(selectedId4);
                    RadioButton radioButton5 = (RadioButton)radioGroup5.findViewById(selectedId5);
                    if (radioButton1.getText().equals("  Yes")) {
                        Score = Score + 2;
                    }
                    if (radioButton2.getText().equals("  Yes")) {
                        Score = Score + 2;
                    }
                    if (radioButton3.getText().equals("  Yes")) {
                        Score = Score + 2;
                    }
                    if (radioButton4.getText().equals("  Yes")) {
                        Score = Score + 2;
                    }
                    if (radioButton5.getText().equals("  Yes")) {
                        Score = Score + 2;
                    }
                    if (diabetes == 1){
                        Score = Score + 5;
                    }
                    if (asthma == 1){
                        Score = Score + 5;
                    }
                    if (chd == 1){
                        Score = Score + 5;
                    }

                    if (Score <= 6) {
                        display_stress_level.setText("Low Stress");
                    }
                    else if (Score >= 7 && Score <= 15){
                        display_stress_level.setText("Moderate Stress");
                    }
                    else {
                        display_stress_level.setText("High Stress");
                    }

                    DocumentReference documentReference = fStore.collection("Users").document(userId).collection("Health Details").document(userId);
                    Map<String, Object> user = new HashMap<>();
                    user.put("Stress Score", Score);

                    documentReference.update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: User Profile is created for " + userId);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: " + e.toString());
                        }
                    });
                }
            }
        });

    }

}