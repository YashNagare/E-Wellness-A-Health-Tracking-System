package com.example.e_wellness;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Profile extends AppCompatActivity {

    TextView prousername, profirstname, prolastname, pflname, progender, prodob, proheight, proweight, prosteps;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId, downloadMR, ref;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    LinearLayout download_medical_record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        prousername = (TextView) findViewById(R.id.PUserName);
        profirstname = (TextView) findViewById(R.id.PFirstName);
        prolastname = (TextView) findViewById(R.id.PLastName);
        pflname = (TextView) findViewById(R.id.PFLName);
        progender = (TextView) findViewById(R.id.gender);
        prodob = (TextView) findViewById(R.id.dob);
        proheight = (TextView) findViewById(R.id.Height);
        proweight = (TextView) findViewById(R.id.Weight);
        prosteps = (TextView) findViewById(R.id.steps);
        download_medical_record = (LinearLayout) findViewById(R.id.download_medical_records);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userId = fAuth.getCurrentUser().getUid();

        System.out.println("InProfile");

        final DocumentReference documentReference = fStore.collection("Users").document(userId).collection("Personal Details").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                prousername.setText(documentSnapshot.getString("UserName"));
                profirstname.setText(documentSnapshot.getString("FirstName"));
                prolastname.setText(documentSnapshot.getString("LastName"));
                pflname.setText(documentSnapshot.getString("FirstName").substring(0, 1).toUpperCase() + documentSnapshot.getString("LastName").substring(0, 1).toUpperCase());

            }
        });

        DocumentReference documentReferenceHD = fStore.collection("Users").document(userId).collection("Health Details").document(userId);
        documentReferenceHD.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                progender.setText(documentSnapshot.getString("Gender"));
                prodob.setText(documentSnapshot.getString("DOB"));
                proweight.setText(documentSnapshot.getString("Weight") + " kg");
                proheight.setText(documentSnapshot.getString("Height") + " cm");
                downloadMR = documentSnapshot.getString("Medical Records");
            }
        });

        DocumentReference documentReferenceWD = fStore.collection("Users").document(userId).collection("Watch Details").document(userId);
        documentReferenceWD.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                prosteps.setText(documentSnapshot.getString("Steps"));
            }
        });


        download_medical_record();


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.profile);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.homepage:
                        startActivity(new Intent(getApplicationContext(), HomePage.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.chatbot:
                        startActivity(new Intent(getApplicationContext(), Chatbot.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.profile:
                        return true;
                };
                return false;
            }
        });



    }

    public void download_medical_record() {

        download_medical_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storageReference = firebaseStorage.getInstance().getReference();
                DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(downloadMR);
                DownloadManager.Request request = new DownloadManager.Request(uri);

                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(Profile.this, "Downloads", "Medical Record.pdf");

                downloadManager.enqueue(request);

            }
        });

    }

}