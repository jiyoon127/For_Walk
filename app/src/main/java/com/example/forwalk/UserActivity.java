package com.example.forwalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class UserActivity extends AppCompatActivity implements LocationListener {

    Button btn_register, btn_connect;
    TextView tView10, tView11, tView12, tView14, tView15, tView16;

    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    private BluetoothSPP bt;

    final static String LOC_INFO = "location_information";
    final static String TAG = "MSP03";
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    static String id = "", con_id = "", cur_log="",cur_time="",traf="";
    static ArrayQueue queue = new ArrayQueue(5);
    static int i=1;

    private final int MSG_1 = 1;
    private final int MSG_2 = 2;

    LocationManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mAuth = FirebaseAuth.getInstance();
        id = encodeUserEmail(mAuth.getCurrentUser().getEmail());

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        bt = new BluetoothSPP(this);//initializing

        tView10 = (TextView) findViewById(R.id.tView10);
        tView11 = (TextView) findViewById(R.id.tView11);
        tView12 = (TextView) findViewById(R.id.tView12);
        //tView13 = (TextView) findViewById(R.id.tView13);
        tView14 = (TextView) findViewById(R.id.tView14);
        tView15 = (TextView) findViewById(R.id.tView15);
        tView16 = (TextView) findViewById(R.id.tView16);
        btn_register = (Button) findViewById(R.id.btn_register);
        btn_connect = (Button) findViewById(R.id.btn_con);


        DatabaseReference ref = database.getReference("app").child(encodeUserEmail(id));
        final DatabaseReference traffic = database.getReference("traffic").child("tra_id1").child("light");


        ref.child("name").addListenerForSingleValueEvent(new ValueEventListener() {//read user's name
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                tView10.setText(value + "님");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //fail to retrieve data
                Log.d(TAG, "fail to read name");
            }
        });
        handler.sendEmptyMessage(1);

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedReference.clearUserName(UserActivity.this);
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //RegisterDialog reg = new RegisterDialog(UserActivity.this);
                //reg.callFunction();

                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }

            }
        });
//----------------bluetooth--------------------

        if(!bt.isBluetoothAvailable())
        {
            Toast.makeText(this,"블루투스를 사용할 수 없습니다. 어플을 종료합니다.",Toast.LENGTH_LONG).show();
        }
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {//데이터 수신
                Toast.makeText(UserActivity.this,message,Toast.LENGTH_SHORT).show();
                if(message.equals("12142")){
                    int permission = checkSelfPermission(Manifest.permission.CALL_PHONE);
                    if(permission==PackageManager.PERMISSION_DENIED){
                        if(shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)){
                            AlertDialog.Builder dialog = new AlertDialog.Builder(UserActivity.this);
                            dialog.setTitle("권한이 필요합니다.").setMessage("이 기능을 사용하기 위해서는 단말기의 \"전화걸기\" 권한이 필요합니다. 계속 하시겠습니까?")
                                    .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1000);
                                            }
                                        }
                                    }).setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(UserActivity.this,"기능을 취소했습니다",Toast.LENGTH_SHORT).show();
                                }
                            }).create().show();
                        }
                        else{
                            requestPermissions(new String[]{Manifest.permission.CALL_PHONE},1000);
                        }
                    }
                    else{
                        Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:01051335482"));
                        startActivity(intent);
                    }
                }
                else if(message.equals("123")){
                    traffic.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                 long value = dataSnapshot.getValue(long.class);
                                         traf = Long.toString(value);
                                         bt.send(traf,true);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            @Override
            public void onDeviceConnected(String name, String address) {//연결됐을 때
                Toast.makeText(getApplicationContext(),"Connected to "+name+"\n"+address,Toast.LENGTH_SHORT).show();
                tView12.setText("연결상태: 연결됨");
                btn_register.setText("연결해제");
            }

            @Override
            public void onDeviceDisconnected() {//연결 해제
                Toast.makeText(getApplicationContext(),"Connection lost",Toast.LENGTH_SHORT).show();
                tView12.setText("연결상태: 해제됨");
                btn_register.setText("등록");
            }

            @Override
            public void onDeviceConnectionFailed() {//연결 실패
                Toast.makeText(getApplicationContext(),"연결에 살패하였습니다. 다시 시도해 주세요.",Toast.LENGTH_SHORT).show();
            }
        });


        //tView11 cane info
        //tView12 bluetooth connect status
        //tView14 pro name
        //tView15 rel with pro
        //tView16 pro phone

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bt.stopService();//블루투스 중지
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!bt.isBluetoothEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }else{
            if(!bt.isServiceAvailable()){
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            }
        }
    }

    public void setup(){//데이터 전송
        TextView tView13 = findViewById(R.id.tView13);
        tView13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt.send("Text",true);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==BluetoothState.REQUEST_CONNECT_DEVICE){
            if(resultCode== Activity.RESULT_OK)
                bt.connect(data);
        }else if(requestCode==BluetoothState.REQUEST_ENABLE_BT){
            if(resultCode==Activity.RESULT_OK){
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            }else{
                Toast.makeText(getApplicationContext(),"Bluetooth was not enabled",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
//-----------------------------------------------------
    }

//-----------------------gps---------------------------
protected void onResume() {
        super.onResume();

    String usr = mAuth.getCurrentUser().getEmail();

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference myRef = database.getReference("app").child(encodeUserEmail(usr)).child("gps");
    //final Date date = new Date();
    //final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    final Timer m_Timer = new Timer();
    TimerTask m_Task = new TimerTask() {

        @Override
        public void run() {
                if(i<=5) {
                    myRef.child("history").child(Integer.toString(i)).child("loc").setValue(cur_log);
                    myRef.child("history").child(Integer.toString(i)).child("time").setValue(cur_time);
                    i++;
                }
                else{
                    m_Timer.cancel();
                }
        }
    };
        m_Timer.schedule(m_Task,15000,2000);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {//권한 허가 상태가 아닐경우

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //허가 필요성 설명
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                //설명 필요없는 경우 권한 허가 요청

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }

        } else {
            // ACCESS_FINE_LOCATION 권한이 있는 것이므로
            // location updates 요청을 할 수 있다.

            // GPS provider를 이용
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    public void onRequestPermissionResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {// ACCESS_FINE_LOCATION 권한을 얻었으므로 관련 작업을 수행할 수 있다

                    try {
                        String usr = mAuth.getCurrentUser().getEmail();
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);//10분 간격으로 위치 전송
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final DatabaseReference myRef = database.getReference("app").child(encodeUserEmail(usr)).child("gps");
                    } catch (SecurityException e) {
                        Log.d(TAG, "SecurityException: permission required");
                    }

                } else {
                    // 권한을 얻지 못 하였으므로 location 요청 작업을 수행할 수 없다
                }
                return;
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        lm.removeUpdates(this);
    }

    // LocationListener 구현을 위한 메소드
    // onLocationChanged, onStatusChanged, onProviderEnabled, onProviderDisabled

    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        long time = location.getTime();
        final Date date = new Date();
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String usr = mAuth.getCurrentUser().getEmail();

        Log.d(LOC_INFO, "latitiude:" + lat + ", longnitude: " + lng);
        //Toast.makeText(this, "latitude: "+ lat +", longitude: "+ lng, Toast.LENGTH_SHORT).show();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("app").child(encodeUserEmail(usr)).child("gps");
        cur_log = lat+","+lng;
        myRef.child("current").setValue(cur_log);
        cur_time = formatter.format(date);

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public void onProviderEnabled(String provider) {

    }

    public void onProviderDisabled(String provider) {

    }


 //------------Thread----------------------

    private final Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case MSG_1:
                    //func
                    getUser();
                    Toast.makeText(getApplicationContext(),"1",Toast.LENGTH_SHORT).show();
                    break;
                case MSG_2:
                    getProtector();
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

    public void getUser(){

        DatabaseReference ref = database.getReference("app").child(id);

        ref.child("con_id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if(value!=null) {
                    con_id = value;
                    Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();
                    handler.sendEmptyMessage(2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getProtector(){

        DatabaseReference ref = database.getReference("app").child(con_id);

        ref.child("name").addListenerForSingleValueEvent(new ValueEventListener() {//read protector's name
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                tView14.setText(value+"님");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG,"fail to read protector's name");
            }
        });

        ref.child("phone").addValueEventListener(new ValueEventListener() {//read protector's phone
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                tView16.setText("전화번호: "+value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG,"fail to read protector's phone number");
            }
        });
    }

    static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }
}

