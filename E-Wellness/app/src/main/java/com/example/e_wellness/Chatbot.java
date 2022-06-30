package com.example.e_wellness;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.ibm.cloud.sdk.core.http.Response;
import com.ibm.cloud.sdk.core.http.ServiceCall;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v2.model.DialogNodeOutputOptionsElement;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.MessageInput;
import com.ibm.watson.assistant.v2.model.MessageOptions;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.SessionResponse;

import java.util.ArrayList;
import java.util.List;

public class Chatbot extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int RECORD_REQUEST_CODE = 101;
    private static String TAG = "MainActivity";
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId, BMI, Med_Record, Steps, Calories, HeartRate, Oxygen, Username, Body_Temperature;
    Number Diabetes, CHD, Asthma, Stress_Level;
    private RecyclerView recyclerView;
    private ChatAdapter mAdapter;
    private ArrayList messageArrayList;
    private EditText inputMessage;
    private ImageButton btnSend;
    private boolean initialRequest;
    private boolean permissionToRecordAccepted = false;
    private boolean listening = false;
    private Context mContext;
    private Assistant watsonAssistant;
    private Response<SessionResponse> watsonAssistantSession;

    private void createServices() {
        watsonAssistant = new Assistant("2019-02-28", new IamAuthenticator(mContext.getString(R.string.assistant_apikey)));
        watsonAssistant.setServiceUrl(mContext.getString(R.string.assistant_url));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        mContext = getApplicationContext();

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();


        DocumentReference documentReferenceHD = fStore.collection("Users").document(userId).collection("Health Details").document(userId);
        documentReferenceHD.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                BMI = documentSnapshot.getString("BMI");
                Med_Record = documentSnapshot.getString("Medical Records");
                Diabetes = documentSnapshot.getLong("Diabetes");
                CHD = documentSnapshot.getLong("CHD");
                Asthma = documentSnapshot.getLong("Asthma");
                Stress_Level = documentSnapshot.getLong("Stress Score");
            }
        });

        DocumentReference documentReferenceWD = fStore.collection("Users").document(userId).collection("Watch Details").document(userId);
        documentReferenceWD.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Steps = documentSnapshot.getString("Step Count");
                Calories = documentSnapshot.getString("Calories Count");
                HeartRate = documentSnapshot.getString("Heart Rate Count");
                Oxygen = documentSnapshot.getString("Oxygen Level Count");
                Body_Temperature = documentSnapshot.getString("Body Temperature Count");
            }
        });

        DocumentReference documentReferencePD = fStore.collection("Users").document(userId).collection("Personal Details").document(userId);
        documentReferencePD.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Username = documentSnapshot.getString("UserName");
            }
        });


        inputMessage = findViewById(R.id.message);
        btnSend = findViewById(R.id.btn_send);
        recyclerView = findViewById(R.id.recycler_view);

        messageArrayList = new ArrayList<>();
        mAdapter = new ChatAdapter(messageArrayList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        this.inputMessage.setText("");
        this.initialRequest = true;


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInternetConnection()) {
                    sendMessage();
                }
            }
        });

        createServices();
        sendMessage();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.chatbot);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.chatbot:
                        return true;

                    case R.id.homepage:
                        startActivity(new Intent(getApplicationContext(), HomePage.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), Profile.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                ;
                return false;
            }
        });
    }

    // Sending a message to Watson Assistant Service
    private void sendMessage() {

        final String inputmessage = this.inputMessage.getText().toString().trim();
        if (!this.initialRequest) {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("1");
            messageArrayList.add(inputMessage);
        } else {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("100");
            this.initialRequest = false;
        }

        this.inputMessage.setText("");
        mAdapter.notifyDataSetChanged();

        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    if (watsonAssistantSession == null) {
                        ServiceCall<SessionResponse> call = watsonAssistant.createSession(new CreateSessionOptions.Builder().assistantId(mContext.getString(R.string.assistant_id)).build());
                        watsonAssistantSession = call.execute();
                    }

                    MessageInput input = new MessageInput.Builder()
                            .text(inputmessage)
                            .build();
                    MessageOptions options = new MessageOptions.Builder()
                            .assistantId(mContext.getString(R.string.assistant_id))
                            .input(input)
                            .sessionId(watsonAssistantSession.getResult().getSessionId())
                            .build();
                    Response<MessageResponse> response = watsonAssistant.message(options).execute();
                    Log.i(TAG, "run: " + response.getResult());
                    if (response != null &&
                            response.getResult().getOutput() != null &&
                            !response.getResult().getOutput().getGeneric().isEmpty()) {

                        List<RuntimeResponseGeneric> responses = response.getResult().getOutput().getGeneric();

                        for (RuntimeResponseGeneric r : responses) {
                            Message outMessage;
                            switch (r.responseType()) {
                                case "text":

                                    outMessage = new Message();
                                    System.out.println(r.text());
                                    if (r.text().equals("You walked ")) {
                                        outMessage.setMessage(r.text() + Steps + " steps");
                                    }
                                    else if (r.text().equals("Your Oxygen Saturation level is ")) {
                                        outMessage.setMessage(r.text() + Oxygen +"%");
                                    }
                                    else if (r.text().equals("You have burned ")) {
                                        outMessage.setMessage(r.text() + Calories + " cal");
                                    }
                                    else if (r.text().equals("Your heart rate is ")) {
                                        outMessage.setMessage(r.text() + HeartRate + " bpm");
                                    }
                                    else if (r.text().equals("BMI is ")) {
                                        outMessage.setMessage(r.text() + BMI);
                                    }
                                    else if (r.text().equals("Here is your medical record - ")) {
                                        outMessage.setMessage(r.text() + Med_Record);
                                    }
                                    else if (r.text().equals("User ID is ")) {
                                        outMessage.setMessage(r.text() + Username);
                                    }
                                    else if (r.text().equals("Your Body Temperature is ")){
                                        outMessage.setMessage(r.text() + Body_Temperature + "°C");
                                    }
                                    else if (r.text().equals("You are ")) {
                                        if (String.valueOf(Diabetes).equals("1")) {
                                            outMessage.setMessage(r.text() + "Diabetic Person");
                                        }
                                        else {
                                            outMessage.setMessage(r.text() + "Non Diabetic Person");
                                        }
                                    }
                                    else if (r.text().equals("You are heart disease  ")) {
                                        if (String.valueOf(CHD).equals("1")) {
                                            outMessage.setMessage(r.text() + "affected person");
                                        }
                                        else {
                                            outMessage.setMessage(r.text() + "free person");
                                        }
                                    }
                                    else if (r.text().equals("You ")) {
                                        if (String.valueOf(Asthma).equals("1")) {
                                            outMessage.setMessage(r.text() + "might have Asthma");
                                        }
                                        else {
                                            outMessage.setMessage(r.text() + "Don't have Asthma");
                                        }
                                    }
                                    else if (r.text().equals("Your stress level is ")){
                                        if (Integer.parseInt(String.valueOf(Stress_Level)) < 6){
                                            outMessage.setMessage(r.text() + "Low");
                                        }
                                        else if (Integer.parseInt(String.valueOf(Stress_Level)) > 6 && Integer.parseInt(String.valueOf(Stress_Level)) < 15){
                                            outMessage.setMessage(r.text() + "Moderate");
                                        }
                                        else {
                                            outMessage.setMessage(r.text() + "High");
                                        }
                                    }
                                    else {
                                        outMessage.setMessage(r.text());
                                    }
                                    outMessage.setId("2");
                                    System.out.println(outMessage.getMessage());

                                    messageArrayList.add(outMessage);

                                    break;

                                case "option":
                                    outMessage = new Message();
                                    String title = r.title();
                                    String OptionsOutput = "";
                                    for (int i = 0; i < r.options().size(); i++) {
                                        DialogNodeOutputOptionsElement option = r.options().get(i);
                                        OptionsOutput = OptionsOutput + option.getLabel() + "\n";

                                    }
                                    outMessage.setMessage(title + "\n" + OptionsOutput);
                                    outMessage.setId("2");

                                    messageArrayList.add(outMessage);

                                    break;

                                case "image":
                                    outMessage = new Message(r);
                                    messageArrayList.add(outMessage);

                                    break;
                                default:
                                    Log.e("Error", "Unhandled message type");
                            }
                        }

                        runOnUiThread(new Runnable() {
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                                if (mAdapter.getItemCount() > 1) {
                                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);

                                }

                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    /**
     * Check Internet Connection
     *
     * @return
     */
    private boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            return true;
        } else {
            Toast.makeText(this, " No Internet Connection available ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

}


