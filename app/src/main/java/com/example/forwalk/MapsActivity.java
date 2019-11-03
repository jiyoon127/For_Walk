package com.example.forwalk;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private RecyclerView rView1;
    private GpsAdapter mGpsAdapter;
    private List<GpsItem> mGpsArray;
    private LinearLayoutManager layoutManager;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("app");

    final int MSG_1=1, MSG_2=2;

    static String[] loc;
    static String[] locs = new String[5];
    static String[] times = new String[5];
    static LatLng[] current = new LatLng[5];
    static Marker[] markers = new Marker[5];
    static String con_id="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        con_id = getIntent().getStringExtra("connected_id");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);

        init();

        Query last5 = ref.child(con_id).child("gps").child("history").limitToLast(5);
        last5.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i=0;
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    locs[i] = postSnapshot.child("loc").getValue().toString();
                    Log.d("loc", "loc " + i + " = " + locs[i]);
                    times[i] = postSnapshot.child("time").getValue().toString();
                    if(times[i].equals("null"))
                        times[i]="위치정보가 존재하지 않습니다";
                    i++;
                }
                handler.sendEmptyMessage(1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void init(){
        rView1 = (RecyclerView) findViewById(R.id.rView1);
    }

    private void dataInit(){
        mGpsArray = new ArrayList<GpsItem>();

        for(int i=0;i<5;i++){
            GpsItem item = new GpsItem();
            item.setNum(i+1);
            item.setTime(times[i]);

            mGpsArray.add(item);
        }

        layoutManager = new LinearLayoutManager(MapsActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rView1.setLayoutManager(layoutManager);
        mGpsAdapter = new GpsAdapter(mGpsArray);

        rView1.setAdapter(mGpsAdapter);
        rView1.setItemAnimator(new DefaultItemAnimator());
        mGpsAdapter.setOnItemClickListener(new GpsAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(GpsAdapter.ViewHolder holder, View view, int position) {
                GpsItem item = GpsAdapter.getItem(position);
                CardView cv = (CardView) findViewById(R.id.cardView);
                cv.setCardBackgroundColor(Color.WHITE);
                Toast.makeText(getApplicationContext(),String.valueOf(position+1),Toast.LENGTH_LONG).show();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(current[position]));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                markers[position].showInfoWindow();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
    }


    public void getLocations(){
        mMap.clear();

        for(int i=0;i<5;i++) {
            if(!locs[i].equals("null")){
                loc = locs[i].split(",");
                current[i] = new LatLng(Float.parseFloat(loc[0]), Float.parseFloat(loc[1]));
                markers[i] = mMap.addMarker(new MarkerOptions().position(current[i]).title((i+1)+" 번 "+times[i]));
                markers[i].showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(current[i]));
                mMap.setMinZoomPreference(5);
            }else{
                current[i]=null;
            }
        }
        Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(true));
        for(int i=0;i<5;i++){
            if(current[i]!=null){
                polyline1 = mMap.addPolyline(new PolylineOptions().clickable(true).add(current[i]));
            }
        }
        polyline1.setColor(Color.parseColor("#FF0000"));
        mMap.setMinZoomPreference(5);
    }
//---------------------handler-------------------------

    private final Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case MSG_1:
                    //func
                    dataInit();
                    Toast.makeText(getApplicationContext(),"1",Toast.LENGTH_SHORT).show();
                    handler.sendEmptyMessage(2);
                    break;
                case MSG_2:
                    //func
                    getLocations();
                    Toast.makeText(getApplicationContext(),"2",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    Handler handler = mHandler;

    public void run(){
        while(true){
            Message message = mHandler.obtainMessage();

            message.what=MSG_1;
            message.arg1=MSG_2;

            handler.sendMessage(message);
        }
    }



    static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

}
