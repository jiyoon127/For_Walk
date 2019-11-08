package com.example.forwalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProtectorActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private BackPressCloseHandler back;
    String usr = encodeUserEmail(mAuth.getCurrentUser().getEmail());
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("app");

    static String loc, con, emer;
    static String[] locs;
    TextView tView5;
    Button btn_map, btn_usr_del_login2;
    final String TAG = "VALUE";
    static String usr_id="u";
    private final int MSG_1 = 1;
    private final int MSG_2 = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protector);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ref.child(usr).child("con_id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                usr_id=value;
                Log.d(TAG,"user id value"+value);
                con = value;
                handler.sendEmptyMessage(1);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Toast.makeText(this,usr_id,Toast.LENGTH_LONG).show();


        tView5 = (TextView) findViewById(R.id.tView5);
        btn_map = (Button) findViewById(R.id.btn_map);
        btn_usr_del_login2 = (Button) findViewById(R.id.btn_usr_del_login2);
        back = new BackPressCloseHandler(this);

        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!usr_id.isEmpty()) {
                    Intent intent = new Intent(ProtectorActivity.this, MapsActivity.class);
                    intent.putExtra("connected_id", usr_id);
                    startActivity(intent);
                    Log.d("con val success = ",usr_id);
                    //no need to close
                }
                else{
                    Log.d("con val fail = ",usr_id);
                }
            }
        });

        btn_usr_del_login2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedReference.clearUserName(ProtectorActivity.this);
            }
        });

    }

    public void getLocation3(){
        LatLng current = new LatLng(Float.parseFloat(locs[0]),Float.parseFloat(locs[1]));
        mMap.addMarker(new MarkerOptions().position(current));
        mMap.setMinZoomPreference(10);

    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //----------read current location------------
        //MarkerOptions mOptions = new MarkerOptions();
        //mOptions.position(new LatLng(Integer.parseInt(locs[0]),Integer.parseInt(locs[1])));
        //googleMap.addMarker(mOptions);
        //LatLng current = new LatLng(Float.parseFloat(locs[0]),Float.parseFloat(locs[1]));
        //
        // mMap.addMarker(new MarkerOptions().position(current));
    }
//------handler------
    private final Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case MSG_1:
                    //func
                    getLocation();
                    Toast.makeText(getApplicationContext(),"1",Toast.LENGTH_SHORT).show();
                    break;
                case MSG_2:
                    getLocation2();
                    getLocation3();
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

    public void getLocation(){

        ref.child(usr_id).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value= dataSnapshot.getValue(String.class);
                tView5.setText(value+"님의 현위치");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ref.child(usr_id).child("gps").child("current").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value  = dataSnapshot.getValue(String.class);
                loc=value;
                Toast.makeText(getApplicationContext(),loc,Toast.LENGTH_LONG).show();
                if(value!=null)
                    handler.sendEmptyMessage(2);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getLocation2(){
        locs = loc.split(",");
        mMap.clear();
        LatLng current = new LatLng(Float.parseFloat(locs[0]),Float.parseFloat(locs[1]));
        mMap.addMarker(new MarkerOptions().position(current));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));



        Toast.makeText(getApplicationContext(),"2"+loc,Toast.LENGTH_LONG).show();
    }

    @Override public void onBackPressed() {
        //super.onBackPressed();
        back.onBackPressed();
        mAuth.signOut();
    }

    static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }
}
