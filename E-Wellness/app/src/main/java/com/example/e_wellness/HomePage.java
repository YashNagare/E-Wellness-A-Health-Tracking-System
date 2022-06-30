package com.example.e_wellness;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class HomePage extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_OAUTH = 1000;
    String HCage;
    String pregnancies = "";
    String glucose = "";
    String bp = "";
    String st = "";
    String insulin = "";
    String dpf = "";
    String bmi = "";
    String cholesterol = "";
    String sysBP = "";
    String diaBP = "";
    String chdglucose = "";

    //FAB Button
    FloatingActionButton add_btn, glucose_btn, medical_record_btn, blood_pressure_btn;
    float traslationY = 100f;
    OvershootInterpolator interpolator = new OvershootInterpolator();
    Boolean isMenu_open = false;

    //Step Counter
    String userID, body_temperature, respiratory_rate;
    int oxygenCount;
    long stepCount;
    float caloriesCount, heartRateCount;

    //Firebase
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    StorageReference storageReference;
    private boolean authInProgress = false;
    private GoogleApiClient mClient = null;

    //Medical Record Popup
    private Context mContext;
    private Activity mActivity;


    //  Progress Bar
    private ProgressBar circular_pro;
    private Button step_counter, calories, heartrate, oxygen;
    private TextView status;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private String TAG = "Fitness";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        circular_pro = (ProgressBar) findViewById(R.id.progress_bar);
        status = (TextView) findViewById(R.id.text_view_progress);
        step_counter = (Button) findViewById(R.id.steps);
        step_counter();
        calories = (Button) findViewById(R.id.calories);
        calories();
        heartrate = (Button) findViewById(R.id.heartrate);
        heartrate();
        oxygen = (Button) findViewById(R.id.oxygen);
        oxygen();


        //Firebase
        storageReference = FirebaseStorage.getInstance().getReference();
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        mContext = getApplicationContext();
        mActivity = HomePage.this;


        DocumentReference documentReferenceHD = fStore.collection("Users").document(userID).collection("Health Details").document(userID);
        documentReferenceHD.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                HCage = documentSnapshot.getString("Age");
                System.out.println("Age" + HCage);
            }
        });


        // Server Data
        fetch_serverdata();

        // Server Data End

        bottomNavigationView.setSelectedItemId(R.id.homepage);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.chatbot:
                        startActivity(new Intent(getApplicationContext(), Chatbot.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.homepage:
                        return true;

                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), Profile.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });


        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.CONFIG_API)
                .addApi(ActivityRecognition.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .useDefaultAccount()
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                //Async To fetch steps
                                new FetchStepsAsync().execute();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                ).addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult result) {

                                Log.i(TAG, "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            HomePage.this, 0).show();
                                    return;
                                }
                                if (!authInProgress) {
                                    try {
                                        Log.i(TAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(HomePage.this, REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(TAG,
                                                "Exception while starting resolution activity", e);
                                    }
                                }
                            }
                        }
                ).build();
        mClient.connect();

    }


    public void step_counter() {

        step_counter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                circular_pro.setProgress(0);
                status.setText(String.valueOf(stepCount));
                progressStatus = 0;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (progressStatus < Float.parseFloat(String.valueOf(stepCount)) / 5000 * 100) {
                            progressStatus += 1;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    circular_pro.setProgress(progressStatus);
                                }
                            });
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        });
    }

    private void calories() {

        calories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                circular_pro.setProgress(0);
                double cal_cnt = Double.parseDouble(String.valueOf(caloriesCount));
                status.setText(String.valueOf((int) cal_cnt));
                progressStatus = 0;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (progressStatus < Float.parseFloat(String.valueOf(caloriesCount)) / 5000 * 100) {
                            progressStatus += 1;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    circular_pro.setProgress(progressStatus);
                                }
                            });
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        });

        initFAB();

    }


    private void oxygen() {

        oxygen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Random rand = new Random();
                int low = 92;
                int high = 100;
                oxygenCount = rand.nextInt(high - low) + low;
                circular_pro.setProgress(0);
                status.setText(String.valueOf(oxygenCount));
                progressStatus = 0;

                DocumentReference documentReference = fStore.collection("Users").document(userID).collection("Watch Details").document(userID);
                Map<String, Object> user = new HashMap<>();
                user.put("Oxygen Level Count", String.valueOf((int) oxygenCount));

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

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (progressStatus < oxygenCount) {
                            progressStatus += 1;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    circular_pro.setProgress(progressStatus);
                                }
                            });
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        });

    }

    private void heartrate() {

        heartrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circular_pro.setProgress(0);
                double cal_cnt = Double.parseDouble(String.valueOf(heartRateCount));
                status.setText(String.valueOf((int) cal_cnt));
                progressStatus = 0;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (progressStatus < Float.parseFloat(String.valueOf(heartRateCount))) {
                            progressStatus += 1;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    circular_pro.setProgress(progressStatus);
                                }
                            });
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        });
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.add_btn:
                if (isMenu_open) {
                    close_menu();
                } else {
                    open_menu();
                }
                break;

            case R.id.add_medical_records:

                selectPDF();

                break;

            case R.id.blood_pressure:
                updateWeight();
                break;

            case R.id.glucose_btn:
                updateHeight();
                break;

        }

    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        super.onDestroy();
    }

    private void initFAB() {
        add_btn = findViewById(R.id.add_btn);
        glucose_btn = findViewById(R.id.glucose_btn);
        medical_record_btn = findViewById(R.id.add_medical_records);
        blood_pressure_btn = findViewById(R.id.blood_pressure);

        glucose_btn.setAlpha(0f);
        medical_record_btn.setAlpha(0f);
        blood_pressure_btn.setAlpha(0f);

        medical_record_btn.setTranslationY(traslationY);
        blood_pressure_btn.setTranslationY(traslationY);
        glucose_btn.setTranslationY(traslationY);

        add_btn.setOnClickListener(this);
        glucose_btn.setOnClickListener(this);
        medical_record_btn.setOnClickListener(this);
        blood_pressure_btn.setOnClickListener(this);
    }

    private void open_menu() {
        isMenu_open = !isMenu_open;

        add_btn.animate().setInterpolator(interpolator).rotation(45f).setDuration(300).start();
        medical_record_btn.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        blood_pressure_btn.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        glucose_btn.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
    }

    private void close_menu() {
        isMenu_open = !isMenu_open;
        add_btn.animate().setInterpolator(interpolator).rotation(0f).setDuration(300).start();

        medical_record_btn.animate().translationY(traslationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        blood_pressure_btn.animate().translationY(traslationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        glucose_btn.animate().translationY(traslationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();

    }

    private void selectPDF() {

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "PDF FILE SELECT"), 12);

    }


    private void updateWeight() {
        ExampleDialog exampleDialog = new ExampleDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }

    private void updateHeight() {
        HeightDialog heightDialog = new HeightDialog();
        heightDialog.show(getSupportFragmentManager(), "height dialog");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 12 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadPDFFileFireStore(data.getData());
        }

    }

    private void uploadPDFFileFireStore(Uri data) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("File is loading");
        progressDialog.show();

        StorageReference reference = storageReference.child("uploadPDF" + System.currentTimeMillis() + ".pdf");

        reference.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isComplete()) ;
                        Uri uri = uriTask.getResult();

                        System.out.println(uri); //This is URL of Uploaded File

                        DocumentReference documentReference = fStore.collection("Users").document(userID).collection("Health Details").document(userID);
                        Map<String, Object> user = new HashMap<>();
                        user.put("Medical Records", String.valueOf(uri));

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


                        Toast.makeText(HomePage.this, "File Uploaded", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                double progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                progressDialog.setMessage("Uploading Records.." + (int) progress + "%");

            }
        });

    }

    public void btndiabetes(View view) {
        fetchdata();
    }

    private void fetchdata() {
        if (HCage.equals("")) {
            Toast.makeText(this, "Please enter an age", Toast.LENGTH_LONG).show();
            return;
        }

        String url = config.DATA_URL + HCage;

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showJSON(response);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomePage.this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void showJSON(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(config.JSON_ARRAY);
            JSONObject collegeData = result.getJSONObject(0);
            pregnancies = collegeData.getString(config.KEY_PREGNANCIES);
            glucose = collegeData.getString(config.KEY_GLUCOSE);
            bp = collegeData.getString(config.KEY_BP);
            st = collegeData.getString(config.KEY_ST);
            insulin = collegeData.getString(config.KEY_INSULIN);
            bmi = collegeData.getString(config.KEY_BMI);
            dpf = collegeData.getString(config.KEY_DPF);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(HomePage.this, PredictDiabetes.class);
        intent.putExtra("Pregnancies", pregnancies);
        intent.putExtra("Glucose", glucose);
        intent.putExtra("Blood Pressure", bp);
        intent.putExtra("Skin Thickness", st);
        intent.putExtra("Insulin", insulin);
        intent.putExtra("BMI", bmi);
        intent.putExtra("DPF", dpf);
        intent.putExtra("Age", HCage);
        startActivity(intent);
    }

    //Diabetes Prediction

    public void btnCHD(View view) {

        fetchCHDData();

    }

    private void fetchCHDData() {

        if (HCage.equals("")) {
            Toast.makeText(this, "Please enter an id", Toast.LENGTH_LONG).show();
            return;
        }

        String chd_url = config.CHD_DATA_URL + HCage;

        StringRequest stringRequest = new StringRequest(chd_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showCHDJSON(response);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomePage.this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void showCHDJSON(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(config.JSON_ARRAY);
            JSONObject collegeData = result.getJSONObject(0);
            cholesterol = collegeData.getString(config.CHD_KEY_CHOLESTEROL);
            sysBP = collegeData.getString(config.CHD_KEY_SYSBP);
            diaBP = collegeData.getString(config.CHD_KEY_DIABP);
            chdglucose = collegeData.getString(config.CHD_KEY_GLUCOSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(HomePage.this, PredictCHD.class);
        intent.putExtra("Cholesterol", cholesterol);
        intent.putExtra("sysBP", sysBP);
        intent.putExtra("diaBP", diaBP);
        intent.putExtra("Glucose", chdglucose);
        startActivity(intent);

    }

    // CHD Prediction

    public void btnAsthma(View view) {

        Intent intent = new Intent(HomePage.this, PredictAsthma.class);
        intent.putExtra("SpO2", String.valueOf(oxygenCount));
        intent.putExtra("Heart_Rate", String.valueOf(heartRateCount));
        intent.putExtra("Respiratory_Rate", String.valueOf(respiratory_rate));
        startActivity(intent);

    }


    public void btnStress(View view) {

        Intent intent = new Intent(HomePage.this, StressMngt.class);
        startActivity(intent);

    }


    private void fetch_serverdata() {

        String url = config.SERVER_DATA_URL;

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray result = jsonObject.getJSONArray(config.JSON_ARRAY);
                    JSONObject collegeData = result.getJSONObject(0);
                    body_temperature = collegeData.getString(config.SERVER_KEY_BODY_TEMPERATURE);
                    respiratory_rate = collegeData.getString(config.SERVER_KEY_RESPIRATORY_RATE);
                    System.out.println(respiratory_rate);

                    DocumentReference documentReference = fStore.collection("Users").document(userID).collection("Watch Details").document(userID);
                    Map<String, Object> user = new HashMap<>();
                    user.put("Body Temperature Count", String.valueOf(body_temperature));

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

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomePage.this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private class FetchStepsAsync extends AsyncTask<Object, Object, Long> {
    protected Long doInBackground(Object... params) {
        PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_STEP_COUNT_DELTA);
        DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
        if (totalResult.getStatus().isSuccess()) {
            DataSet totalSet = totalResult.getTotal();
            if (totalSet != null) {
                stepCount = totalSet.isEmpty()
                        ? 0
                        : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                DocumentReference documentReference = fStore.collection("Users").document(userID).collection("Watch Details").document(userID);
                Map<String, Object> user = new HashMap<>();
                user.put("Step Count", String.valueOf((int) stepCount));

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
        } else {
            Log.w(TAG, "There was a problem getting the step count.");
        }
        return stepCount;
    }


    @Override
    protected void onPostExecute(Long aLong) {
        super.onPostExecute(aLong);
        //Total steps covered for that day
        Log.i(TAG, "Total steps: " + aLong);
        new HomePage.FetchCalorieAsync().execute();
    }
}


private class FetchCalorieAsync extends AsyncTask<Object, Object, Float> {
    protected Float doInBackground(Object... params) {
        try {
            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_CALORIES_EXPENDED);
            DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                if (totalSet != null) {
                    caloriesCount = totalSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();

                    DocumentReference documentReference = fStore.collection("Users").document(userID).collection("Watch Details").document(userID);
                    Map<String, Object> user = new HashMap<>();
                    user.put("Calories Count", String.valueOf((int) caloriesCount));

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
            } else {
                Log.w(TAG, "There was a problem getting the calories.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return caloriesCount;
    }


    @Override
    protected void onPostExecute(Float aLong) {
        super.onPostExecute(aLong);
        //Total calories burned for that day
        Log.i(TAG, "Total calories: " + aLong);
        new FetchHeartAsync().execute();
    }
}

// Server Data

private class FetchHeartAsync extends AsyncTask<Object, Object, Float> {
    protected Float doInBackground(Object... params) {
        try {
            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_HEART_RATE_BPM);
            DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                if (totalSet != null) {
                    heartRateCount = totalSet.getDataPoints().get(0).getValue(Field.FIELD_AVERAGE).asFloat();

                    DocumentReference documentReference = fStore.collection("Users").document(userID).collection("Watch Details").document(userID);
                    Map<String, Object> user = new HashMap<>();
                    user.put("Heart Rate Count", String.valueOf((int) heartRateCount));

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
            } else {
                Log.w(TAG, "There was a problem getting the calories.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return heartRateCount;
    }

    @Override
    protected void onPostExecute(Float aLong) {
        super.onPostExecute(aLong);
        //Total calories burned for that day
        Log.i(TAG, "Total heartrate: " + aLong);
    }
}

}
