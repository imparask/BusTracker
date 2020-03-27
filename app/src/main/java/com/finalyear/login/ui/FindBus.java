package com.finalyear.login.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.finalyear.login.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.admin.v1beta1.Progress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.finalyear.login.ui.StartActivity.exitConstant;

public class FindBus extends AppCompatActivity {

    private static final String TAG = "FindBus" ;

    private AutoCompleteTextView mEnterDestination,mEnterSource;
    private TextView mShowBuses,mShowBusStops;
    private Button mViewOnMap,mSearchBus;
    private FirebaseFirestore fStore;
    private ProgressBar progressBar;
    private Map<String,Integer> availableBuses;
    private ProgressDialog progressDialog;
    public static boolean filterFlag;
    private Toast backPressedToast;
    private long backButtonPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_bus);

        mEnterDestination = findViewById(R.id.actv_enterDestination);
        mEnterSource = findViewById(R.id.actv_enterSource);
        mShowBuses = findViewById(R.id.tv_busesNum);
        mShowBusStops = findViewById(R.id.tv_busesStops);
        mViewOnMap = findViewById(R.id.bt_viewOnMap);
        mSearchBus = findViewById(R.id.bt_submitSearchBus);
        progressBar = findViewById(R.id.pgb_findBus);

        fStore = FirebaseFirestore.getInstance();

        initialiseSearch();

        mSearchBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if(!(TextUtils.isEmpty(mEnterSource.getText().toString()) && TextUtils.isEmpty(mEnterDestination.getText().toString()))) {
                    showAvailableBuses();
                }
            }
        });
        mViewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                filterFlag = false;
                if(availableBuses.size()!=0){
                    Object[] availBuses = availableBuses.keySet().toArray();
                    ArrayList<String> availBusList = new ArrayList<>();

                    Log.d(TAG,availBuses.toString());

                    for(int i=0;i<availBuses.length;i++){
                        availBusList.add(availBuses[i].toString());
                    }
                    Log.d(TAG,availBusList.toString());

                    intent.putStringArrayListExtra("filterBuses", availBusList);
                    filterFlag=true;
                }
                startActivity(intent);
            }
        });
    }



    private void initialiseSearch() {

        progressDialog = new ProgressDialog(FindBus.this);
        progressDialog.setMessage("Loading...");

        Log.d(TAG,"intialiseDestinationSearch called..");

        final List<String> busstops = new ArrayList<>();
        final ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,busstops);
        adapter1.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mEnterDestination.setAdapter(adapter1);
        mEnterDestination.setThreshold(1);

        final ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,busstops);
        adapter2.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mEnterSource.setAdapter(adapter2);
        mEnterSource.setThreshold(1);

        progressDialog.show();

        fStore.collection("BusStop").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        String busstop = documentSnapshot.getString("busStop");
                        busstops.add(busstop);
                    }
                    adapter1.notifyDataSetChanged();
                    adapter2.notifyDataSetChanged();
                    progressDialog.cancel();
                }
            }
        });
    }

    private void showAvailableBuses() {
        Log.d(TAG,"showAvailableBuses called..");

        availableBuses = new HashMap<>();

        fStore.collection("BusRoute").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        boolean sourceFlag=false;
                        boolean destFlag = false;
                        int count = -1;
                        for (int i = 1; i <= documentSnapshot.getData().size(); i++) {
                            String stop = documentSnapshot.getString("busStop" + i);
                            if(stop.equals(mEnterSource.getText().toString())){
                                sourceFlag = true;
                                if(count == -1) {
                                    count = 0;
                                }
                            }
                            if(stop.equals( mEnterDestination.getText().toString())){
                                destFlag = true;
                                if(count == -1) {
                                    count = 0;
                                }
                            }
                            if(count >= 0){
                                count++;
                            }
                            Log.d(TAG,"busStop" + i+". "+stop);
                            Log.d(TAG,"sourceFlag - "+ sourceFlag+" destinationFlag - "+destFlag);
                            Log.d(TAG,"Stop count : "+count);
                            if(sourceFlag && destFlag){
                                availableBuses.put(documentSnapshot.getId(),count-1);
                                break;
                            }

                        }
                    }
                    Log.d(TAG," Available Buses from "+mEnterSource.getText().toString()+"to "+mEnterDestination.getText().toString()+": "+availableBuses);
                    displayBuses();
                }
            }
        });

    }

    private void displayBuses(){
        String text1 = "Available Buses : ";
        String text2 = "Number of Stops : ";
        mShowBuses.setText(text1);
        mShowBusStops.setText(text2);

        Object [] busnumbers = availableBuses.keySet().toArray();

        Log.d(TAG," Available Buses : "+ Arrays.toString(busnumbers) +"........"+availableBuses.keySet());

        for(int i = 1;i<=availableBuses.size();i++) {
            mShowBuses.append("\n"+i+". "+busnumbers[i-1].toString());
            mShowBusStops.append("\n"+availableBuses.get(busnumbers[i-1].toString()));
        }
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {

        if(backButtonPressedTime+2000 >= System.currentTimeMillis()){
            super.onBackPressed();
            backPressedToast.cancel();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }
        else{
            backPressedToast = Toast.makeText(this, "Press Back Again to Logout", Toast.LENGTH_SHORT);
            backPressedToast.show();
        }
        backButtonPressedTime = System.currentTimeMillis();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }
        return true;
    }
}
