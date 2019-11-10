package com.example.forwalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Sampler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String email, pw, id;
    static int type=-1;
    final static String TAG ="tag";
    final int MSG_1=1, MSG_2=2;

    TextView tView1, tView2, tView3, tView4;
    EditText eText1, eText2;
    Button btn_login;

    FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tView1 = (TextView) findViewById(R.id.tView1);
        tView2 = (TextView) findViewById(R.id.tView2);
        tView3 = (TextView) findViewById(R.id.tView3);
        tView4 = (TextView) findViewById(R.id.tView4);
        eText1 = (EditText) findViewById(R.id.eText1);
        eText2 = (EditText) findViewById(R.id.eText2);
        btn_login = (Button) findViewById(R.id.btn_login);

        mAuth = FirebaseAuth.getInstance();

        btn_login.setOnClickListener(this);

        tView4.setOnClickListener(this);
    }
    public void onClick(View v) {

        email = eText1.getText().toString();
        pw = eText2.getText().toString();
        id= encodeUserEmail(email);

        final DatabaseReference usrRef = database.getReference("app").child(id);

        switch(v.getId()){
            case R.id.btn_login :
                usrRef.child("type").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue(String.class);
                        if(value.equals("user")) {
                            startLoading(UserActivity.class);
                            SharedReference.setUserName(MainActivity.this,email,pw,0);
                        }
                        else{
                            startLoading(ProtectorActivity.class);
                            SharedReference.setUserName(MainActivity.this,email,pw,1);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //Toast.makeText(getApplicationContext(),"cannot read type val",Toast.LENGTH_LONG).show();
                    }
                });

                if(eText1.getText().toString().isEmpty()||eText2.getText().toString().isEmpty()){}
                else {
                    mAuth.signInWithEmailAndPassword(email, pw).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                    usrRef.child("device_token").setValue(deviceToken);
                            } else {//failed to log in
                                Toast.makeText(getApplicationContext(), "로그인에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                break;
            case R.id.tView4 :
                Intent intent = new Intent(getBaseContext(), SignUpActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    private final Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case MSG_1:
                    //func
                    //Toast.makeText(getApplicationContext(),"1",Toast.LENGTH_SHORT).show();
                    break;
                case MSG_2:
                    //Toast.makeText(getApplicationContext(),"2",Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void startLoading(final Class context){
        Intent intent = new Intent(getBaseContext(), context);
        startActivity(intent);
        finish();
    }
    static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }
}
