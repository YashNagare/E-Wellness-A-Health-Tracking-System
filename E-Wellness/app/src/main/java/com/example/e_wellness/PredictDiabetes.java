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

import com.example.e_wellness.ml.Diabetes;
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
import org.w3c.dom.Text;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PredictDiabetes extends AppCompatActivity {

    EditText glucose,blood_pressure;
    Button btnPredict;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    TextView display_diabetes_result;
    private static final String TAG = "PredictDiabetes";

    Float pregnancies,glucose_res,BP_res, skin_thickness, insulin, BMI, DPF, age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict_diabetes);

        glucose = (EditText) findViewById(R.id.glucose);
        blood_pressure = (EditText) findViewById(R.id.bp);
        btnPredict = (Button) findViewById(R.id.predict);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        display_diabetes_result = (TextView) findViewById(R.id.display_diabetes_result);

        Intent intent = getIntent();
        pregnancies = Float.parseFloat(intent.getStringExtra("Pregnancies"));
        glucose_res = Float.parseFloat(intent.getStringExtra("Glucose"));
        glucose.setText(String.valueOf(glucose_res));
        BP_res = Float.parseFloat(intent.getStringExtra("Blood Pressure"));
        blood_pressure.setText(String.valueOf(BP_res));
        skin_thickness = Float.parseFloat(intent.getStringExtra("Skin Thickness"));
        insulin = Float.parseFloat(intent.getStringExtra("Insulin"));
        BMI = Float.parseFloat(intent.getStringExtra("BMI"));
        DPF = Float.parseFloat(intent.getStringExtra("DPF"));
        age = Float.parseFloat(intent.getStringExtra("Age"));

        btnPredict();

        DocumentReference documentReferenceHD = fStore.collection("Users").document(userId).collection("Health Details").document(userId);
        documentReferenceHD.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                BMI = Float.valueOf(documentSnapshot.getString("BMI"));
                age = Float.valueOf(documentSnapshot.getString("Age"));
                System.out.println(age);
                System.out.println(BMI);
            }
        });


    }

    private void btnPredict() {

        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(8 * 4);

                byteBuffer.putFloat(pregnancies);
                byteBuffer.putFloat(glucose_res);
                byteBuffer.putFloat(BP_res);
                byteBuffer.putFloat(skin_thickness);
                byteBuffer.putFloat(insulin);
                byteBuffer.putFloat(BMI);
                byteBuffer.putFloat(DPF);
                byteBuffer.putFloat(age);

                try {
                    Diabetes model = Diabetes.newInstance(PredictDiabetes.this);

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 8}, DataType.FLOAT32);
                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    Diabetes.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    float[] outputFeature = outputFeature0.getFloatArray();

                    System.out.println(outputFeature[0]);

                    if (outputFeature[0] == 1.0) {
                        display_diabetes_result.setText("Diabetic Person");
                        DocumentReference documentReference = fStore.collection("Users").document(userId).collection("Health Details").document(userId);
                        Map<String, Object> user = new HashMap<>();
                        user.put("Diabetes", 1);

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
                        display_diabetes_result.setText("Non-Diabetic Person");
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