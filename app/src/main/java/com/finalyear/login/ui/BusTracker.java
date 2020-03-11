package com.finalyear.login.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.finalyear.login.R;
import com.finalyear.login.model.ConductorLocationBus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import javax.xml.datatype.Duration;

public class BusTracker extends AppCompatActivity {

    private TextView mBusNumber,mBusSource,mBusDest,mEta,mPassCount;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_tracker);

        fStore = FirebaseFirestore.getInstance();
        mBusNumber = findViewById(R.id.tv_busNumber);
        mBusSource = findViewById(R.id.tv_busSource);
        mBusDest = findViewById(R.id.tv_busDestination);
        mEta = findViewById(R.id.tv_busETA);
        mPassCount = findViewById(R.id.tv_busPassengerCount);

        Intent intent = getIntent();
        String refNo = intent.getStringExtra("selectedMarker");
        String ETA = intent.getStringExtra("selectedMarkerETA");
        String passengerCount = intent.getStringExtra("passengerCount");
        mEta.setText(ETA);
        mPassCount.setText(passengerCount);
        getMarkerInfo(refNo);
    }

    private void getMarkerInfo(String refNo){
        fStore.collection("Conductor Bus Location").document(refNo).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                ConductorLocationBus conductorLocationBus = task.getResult().toObject(ConductorLocationBus.class);
                mBusNumber.setText(conductorLocationBus.getBusNumber());
                mBusSource.setText(conductorLocationBus.getBusSource());
                mBusDest.setText(conductorLocationBus.getBusDestination());

            }
        });
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
            finish();
            startActivity(new Intent(getApplicationContext(), Login.class));
        }
        return true;
    }
}
