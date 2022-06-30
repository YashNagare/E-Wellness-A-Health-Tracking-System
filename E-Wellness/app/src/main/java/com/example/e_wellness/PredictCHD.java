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

import com.example.e_wellness.ml.CHD;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class PredictCHD extends AppCompatActivity {

    EditText cholesterol, glucose, blood_pressure;
    Button btnPredict;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId, Age, bmi, heartrate;
    TextView display_chd_result;
    private static final String TAG = "PredictCHD";

    Float cholesterol_res,sysBP,diaBP, glucose_res;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict_c_h_d);

        cholesterol = (EditText) findViewById(R.id.cholesterol);
        glucose = (EditText) findViewById(R.id.glucose);
        blood_pressure = (EditText) findViewById(R.id.bp);
        btnPredict = (Button) findViewById(R.id.predict);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        display_chd_result = (TextView) findViewById(R.id.display_chd_result);

        Intent intent = getIntent();
        cholesterol_res = Float.parseFloat(intent.getStringExtra("Cholesterol"));
        cholesterol.setText(String.valueOf(cholesterol_res));
        sysBP = Float.parseFloat(intent.getStringExtra("sysBP"));
        blood_pressure.setText(String.valueOf(sysBP));
        glucose_res = Float.parseFloat(intent.getStringExtra("Glucose"));
        glucose.setText(String.valueOf(glucose_res));

        diaBP = Float.parseFloat(intent.getStringExtra("diaBP"));


        DocumentReference documentReferenceHD = fStore.collection("Users").document(userId).collection("Health Details").document(userId);
        documentReferenceHD.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Age = documentSnapshot.getString("Age");
                bmi = documentSnapshot.getString("BMI");
            }
        });

        DocumentReference documentReferenceWD = fStore.collection("Users").document(userId).collection("Watch Details").document(userId);
        documentReferenceWD.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                heartrate = documentSnapshot.getString("Heart Rate Count");
                System.out.println(heartrate);
            }
        });


        btnPredict();

    }

    private void btnPredict() {
        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(7 * 4);

                byteBuffer.putFloat(Float.parseFloat(Age));
                byteBuffer.putFloat(cholesterol_res);
                byteBuffer.putFloat(sysBP);
                byteBuffer.putFloat(diaBP);
                byteBuffer.putFloat(Float.parseFloat(bmi));
                byteBuffer.putFloat(Float.parseFloat(heartrate));
                byteBuffer.putFloat(glucose_res);

                try {
                    CHD model = CHD.newInstance(PredictCHD.this);

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 7}, DataType.FLOAT32);
                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    CHD.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    float[] outputFeature = outputFeature0.getFloatArray();

                    if (outputFeature[0] == 1.0) {
                        display_chd_result.setText("CHD Person");

                        DocumentReference documentReference = fStore.collection("Users").document(userId).collection("Health Details").document(userId);
                        Map<String, Object> user = new HashMap<>();
                        user.put("CHD", 1);

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
                    else {
                        display_chd_result.setText("Non CHD Person");
                    }

                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e) {
                    // TODO Handle the exception
                }

            }
        });
    }
}