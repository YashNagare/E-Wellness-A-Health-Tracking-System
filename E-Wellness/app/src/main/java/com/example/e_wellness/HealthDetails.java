package com.example.e_wellness;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firestore.v1.WriteResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HealthDetails extends AppCompatActivity {

    public static TextView dateText;

    private static final String TAG = "TAG";
    private static String Age;

    EditText hdweight, hdheight;
    Spinner gender;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;

    Button hdnextbtn;
    String userID, gender_item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_details);


        hdweight = (EditText) findViewById(R.id.Weight);
        hdheight = (EditText) findViewById(R.id.Height);
        gender = findViewById(R.id.gender);
        hdnextbtn = (Button) findViewById(R.id.NextHomeBtn);

        dateText = (TextView) findViewById(R.id.date);
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();


        List<String> categories = new ArrayList<>();
        categories.add(0, "Choose Gender");
        categories.add("Male");
        categories.add("Female");

        final ArrayAdapter<String> dataAdapter;
        dataAdapter = new ArrayAdapter(HealthDetails.this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(dataAdapter);

        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (parent.getItemAtPosition(position).equals("Gender")) {

                }
                else {
                    gender_item = parent.getItemAtPosition(position).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        hdnextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String height = hdheight.getText().toString().trim();
                String weight = hdweight.getText().toString().trim();
                String dob = dateText.getText().toString().trim();
                userID = fAuth.getCurrentUser().getUid();

                DocumentReference documentReference = fStore.collection("Users").document(userID).collection("Health Details").document(userID);
                Map<String, Object> user = new HashMap<>();
                user.put("Height", height);
                user.put("Weight", weight);
                user.put("Gender", gender_item);
                user.put("DOB", dob);
                user.put("BMI", String.valueOf(Float.parseFloat(weight) / ((Integer.parseInt(height) / 100)  ^ 2)));
                user.put("Age", Age);
                user.put("Diabetes", 0);
                user.put("CHD", 0);
                user.put("Asthma", 0);
                user.put("Stress Score", 0);

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

                DocumentReference documentReferenceWD = fStore.collection("Users").document(userID).collection("Watch Details").document(userID);
                Map<String, Object> userWD= new HashMap<>();
                userWD.put("Steps", "5000");
                userWD.put("Step Count", "0");
                userWD.put("Calories Count", "0");
                userWD.put("Heart Rate Count", "0");
                userWD.put("Oxygen Level Count", "0");
                userWD.put("Body Temperature Count", "0");
                userWD.put("Respiratory Rate Count", "0");
                documentReferenceWD.set(userWD).addOnSuccessListener(new OnSuccessListener<Void>() {
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

                startActivity(new Intent(getApplicationContext(), HomePage.class));
            }
        });

    }


    public void btn_PickerDate(View view) {

        DialogFragment fragment = new DateFragment();
        fragment.show(getSupportFragmentManager(), "date picker");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void populateSetDateText(int year, int month, int day) {
        dateText.setText(day+ "/" +month+ "/" +year);

        LocalDate today = LocalDate.now();
        LocalDate birthday = LocalDate.of(year, month, day);  //Birth date

        Period p = Period.between(birthday, today);

        Age = String.valueOf(p.getYears());

    }

}