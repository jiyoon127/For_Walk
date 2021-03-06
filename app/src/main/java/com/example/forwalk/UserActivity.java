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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class UserActivity extends AppCompatActivity implements LocationListener {

    Button btn_register, btn_connect, btn_usr_del_login1;
    TextView tView10, tView11, tView12, tView14, tView15, tView16;

    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseFunctions mFuncs;

    private BluetoothSPP bt;

    final static String LOC_INFO = "location_information";
    final static String TAG = "MSP03";
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    static String id = "", con_id = "", cur_log = "", cur_time = "", receiverUserID = "";
    static int num1 = 0, trf_size = 0, min_j = 0;

    private BackPressCloseHandler back;
    private final int MSG_1 = 1;
    private final int MSG_2 = 2;
    LocationManager lm;
    static Traffic[] trf = new Traffic[100];
    static double cur_lat, cur_lng;
    static double[] d = new double[100];
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_user);
        mFuncs = FirebaseFunctions.getInstance();

        mAuth = FirebaseAuth.getInstance();
        id = encodeUserEmail(mAuth.getCurrentUser().getEmail());

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        bt = new BluetoothSPP(this);//initializing

        back = new BackPressCloseHandler(this);

        tView10 = (TextView) findViewById(R.id.tView10);
        tView11 = (TextView) findViewById(R.id.tView11);
        tView12 = (TextView) findViewById(R.id.tView12);
        tView14 = (TextView) findViewById(R.id.tView14);
        tView15 = (TextView) findViewById(R.id.tView15);
        tView16 = (TextView) findViewById(R.id.tView16);
        btn_register = (Button) findViewById(R.id.btn_register);
        btn_connect = (Button) findViewById(R.id.btn_usr_del_login1);
        btn_usr_del_login1 = (Button) findViewById(R.id.btn_usr_del_login1);
        String usr = mAuth.getCurrentUser().getEmail();


        final DatabaseReference ref = database.getReference("app").child(encodeUserEmail(id));
        final DatabaseReference trafficRef = database.getReference("traffic");
        final DatabaseReference myRef = database.getReference("app").child(encodeUserEmail(usr)).child("gps");
        final DatabaseReference NotificationRef = database.getReference("app").child("Notifications");

        myRef.child("history").removeValue();

        ref.child("receiver_token").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                receiverUserID = value;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        for (int i = 1; i <= 5; i++) {
            myRef.child("history").child(Integer.toString(i)).child("loc").setValue("null");
            myRef.child("history").child(Integer.toString(i)).child("time").setValue("null");
        }

        btn_usr_del_login1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedReference.clearUserName(UserActivity.this);
            }
        });

        final Timer m_Timer = new Timer();
        TimerTask m_Task = new TimerTask() {
            Query last = myRef.child("history").orderByKey().limitToLast(1);
            String num = last.getRef().getKey();

            @Override
            public void run() {
                Log.d(TAG, num);
                HashMap<String, Object> locaitonChangeMap = new HashMap<>();
                if (!cur_log.equals("")) {
                    if (num.equals("history")) { //zero nodes
                        locaitonChangeMap.put("1/loc", cur_log);
                        locaitonChangeMap.put("1/time", cur_time);
                        myRef.child("history").updateChildren(locaitonChangeMap);
                        num = "1";
                        num1 = Integer.parseInt(num) + 1;
                    } else {
                        locaitonChangeMap.put(num1+"/loc", cur_log);
                        locaitonChangeMap.put(num1+"/time", cur_time);
                        myRef.child("history").updateChildren(locaitonChangeMap);
                        num1++;
                    }
                }

            }
        };
        m_Timer.schedule(m_Task, 15000, 5000);

        ref.child("name").addValueEventListener(new ValueEventListener() {//read user's name
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

                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }

            }
        });

//----------------bluetooth--------------------//애뮬레이터에서 실행할 경우 블루투스 기능을 제공하지 않기때문에 블루투스 기능을 주석처리하거나 실제 기기로 테스트 해야함

        if(!bt.isBluetoothAvailable())
        {
            Toast.makeText(this,"블루투스를 사용할 수 없습니다. 어플을 종료합니다.",Toast.LENGTH_LONG).show();
        }
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {//데이터 수신
                //Toast.makeText(UserActivity.this,message,Toast.LENGTH_SHORT).show();
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
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:01051335482"));
                        startActivity(intent);
                    }

                    HashMap<String, String> chatNotificationMap = new HashMap<>();
                    chatNotificationMap.put("from", FirebaseInstanceId.getInstance().getToken());
                    chatNotificationMap.put("type", "request");
                    while (true) {
                        if (!receiverUserID.equals("") && !con_id.equals("")) {
                            NotificationRef.child(con_id).push()
                                    .setValue(chatNotificationMap);
                            break;
                        }
                    }
                }
                else if(message.equals("123")){
                    trafficRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            trf_size = 0;
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                long light = postSnapshot.child("light").getValue(long.class);
                                String loc = postSnapshot.child("loc").getValue(String.class);
                                Log.d(TAG, trf_size + "번 light: " + light + ", loc: " + loc);
                                trf[trf_size] = new Traffic(light, loc);
                                trf_size++;
                            }
                            for (int j = 0; j < trf_size; j++) {
                                d[j] = Math.sqrt(Math.pow(cur_lat - trf[j].get_lat(), 2) + Math.pow(cur_lng - trf[j].get_lng(), 2));
                                Log.d(TAG, j + "번 d = " + d[j]);
                            }

                            double min = d[0];
                            for (int j = 1; j < trf_size; j++) {
                                if (d[j] < min) {
                                    min = d[j];
                                    min_j = j;
                                } else
                                    min_j = 0;
                            }
                            if(trf[min_j].get_light()==0)
                                Toast.makeText(getApplicationContext(), "빨간불이 인식되었습니다.",Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(getApplicationContext(), "초록불이 인식되었습니다.",Toast.LENGTH_LONG).show();
                            bt.send(Long.toString(trf[min_j].get_light()),true);
                            //Toast.makeText(getApplicationContext(), "cur_trf: " + trf[min_j].get_light(), Toast.LENGTH_LONG).show();
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
            public void onDeviceConnected(String name, String address) {//when connected
                Toast.makeText(getApplicationContext(),"Connected to "+name+"\n"+address,Toast.LENGTH_SHORT).show();
                tView11.setText("\""+name+"\"");
                tView12.setText("연결상태: 연결됨");
                btn_register.setText("연결해제");
                ref.child("blu_id").setValue(name);
            }

            @Override
            public void onDeviceDisconnected() {//disconnect
                Toast.makeText(getApplicationContext(),"Connection lost",Toast.LENGTH_SHORT).show();
                tView12.setText("연결상태: 해제됨");
                btn_register.setText("등록");
            }

            @Override
            public void onDeviceConnectionFailed() {//fail to connect
                Toast.makeText(getApplicationContext(),"연결에 실패하였습니다. 다시 시도해 주세요.",Toast.LENGTH_SHORT).show();
            }
        });

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
            if(resultCode== Activity.RESULT_OK){
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            }else{
                Toast.makeText(getApplicationContext(),"Bluetooth was not enabled",Toast.LENGTH_SHORT).show();
                finish();
            }
        }

//-------------------------bluetooth-end----------------------------
    }

    //-----------------------gps---------------------------
    protected void onResume() {
        super.onResume();
        Log.d("resume", "resume-working");
        String usr = mAuth.getCurrentUser().getEmail();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("app").child(encodeUserEmail(usr)).child("gps");


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {//권한 허가 상태가 아닐경우

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //허가 필요성 설명

            } else {

                //permission required, if explanation is not needed
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }

        } else {
            // Has ACCESS_FINE_LOCATION permission
            // can request location updates

            // use GPS provider
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    public void onRequestPermissionResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {//already have permission ACCESS_FINE_LOCATION

                    try {
                        String usr = mAuth.getCurrentUser().getEmail();
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final DatabaseReference myRef = database.getReference("app").child(encodeUserEmail(usr)).child("gps");
                    } catch (SecurityException e) {
                        Log.d(TAG, "SecurityException: permission required");
                    }

                } else {
                    //can't get permission and require user's location
                }
                return;
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    // LocationListener 구현을 위한 메소드
    // onLocationChanged, onStatusChanged, onProviderEnabled, onProviderDisabled

    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        final Date date = new Date();
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss");
        String usr = mAuth.getCurrentUser().getEmail();

        Log.d(LOC_INFO, "latitiude:" + lat + ", longnitude: " + lng);
        //Toast.makeText(this, "latitude: " + lat + ", longitude: " + lng, Toast.LENGTH_SHORT).show();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("app").child(encodeUserEmail(usr)).child("gps");
        cur_log = lat + "," + lng;
        cur_lat = Math.floor(lat*1000000)/1000000.0;
        cur_lng = Math.floor(lng*1000000)/1000000.0;
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

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_1:
                    //func
                    getUser();
                    //Toast.makeText(getApplicationContext(), "1", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_2:
                    getProtector();
                    //Toast.makeText(getApplicationContext(), "2", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    Handler handler = mHandler;

    public void run() {
        while (true) {
            Message message = mHandler.obtainMessage();

            message.what = MSG_1;
            message.arg1 = MSG_2;

            handler.sendMessage(message);
        }
    }

    public void getUser() {

        DatabaseReference ref = database.getReference("app").child(id);

        ref.child("con_id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if (value != null) {
                    if (value.equals("null")) {
                        tView14.setVisibility(View.INVISIBLE);
                        tView15.setVisibility(View.INVISIBLE);
                        tView16.setText(R.string.pro_info);
                    } else {
                        tView14.setVisibility(View.VISIBLE);
                        tView15.setVisibility(View.VISIBLE);
                        con_id = value;
                        //Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();
                        handler.sendEmptyMessage(2);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getProtector() {

        DatabaseReference ref = database.getReference("app").child(con_id);

        ref.child("name").addValueEventListener(new ValueEventListener() {//read protector's name
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                tView14.setText(value + "님");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "fail to read protector's name");
            }
        });

        ref.child("phone").addValueEventListener(new ValueEventListener() {//read protector's phone
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                tView16.setText("전화번호: " + value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "fail to read protector's phone number");
            }
        });

        ref.child("relationship").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                tView15.setText("관계: " + value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        bt.stopService();//블루투스 중지
        back.onBackPressed();
        lm.removeUpdates(this);
        mAuth.signOut();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.signOut();
        if (Build.VERSION.SDK_INT >= 21)
            finishAndRemoveTask();
        else
            finish();
        System.exit(0);
    }

    static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }
}