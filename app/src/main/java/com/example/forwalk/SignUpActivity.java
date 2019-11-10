package com.example.forwalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Iterator;

public class SignUpActivity extends AppCompatActivity implements
        View.OnClickListener{

    private static final String TAG = "SignUpActivity";

    String id, pw, connect;

    EditText eText3, eText4, eText5, eText6, eText7, eRelText;
    TextView tView5, tView6,tView7, tView8, tView9, tRelView;
    Button btn_sign_up;
    RadioGroup rGroup2;
    RadioButton rBtn3, rBtn4;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference taken = database.getReference("app").child("taken");

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_sign_up);

        eText3 = (EditText) findViewById(R.id.eText3);
        eText4 = (EditText) findViewById(R.id.eText4);
        eText5 = (EditText) findViewById(R.id.eText5);
        eText6 = (EditText) findViewById(R.id.eText6);
        eText7 = (EditText) findViewById(R.id.eText7);
        eRelText = (EditText) findViewById(R.id.eRelText);
        btn_sign_up = (Button) findViewById(R.id.btn_sign_up);
        rGroup2 = (RadioGroup) findViewById(R.id.rGroup2);
        rBtn3 = (RadioButton) findViewById(R.id.rBtn3);
        rBtn4 = (RadioButton) findViewById(R.id.rBtn4);
        tView5 = (TextView) findViewById(R.id.tView5);
        tView6 = (TextView) findViewById(R.id.tView6);
        tView7 = (TextView) findViewById(R.id.tView7);
        tView8 = (TextView) findViewById(R.id.tView8);
        tView9 = (TextView) findViewById(R.id.tView9);
        tRelView = (TextView) findViewById(R.id.tRelView);


        mAuth = FirebaseAuth.getInstance();

        rBtn3.setChecked(true);
        btn_sign_up.setOnClickListener(this);

        rBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tView9.setVisibility(View.GONE);
                tRelView.setVisibility(View.GONE);
                eText5.setVisibility(View.GONE);
                eRelText.setVisibility(View.GONE);
            }
        });

        rBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tView9.setVisibility(View.VISIBLE);
                tRelView.setVisibility(View.VISIBLE);
                eText5.setVisibility(View.VISIBLE);
                eRelText.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void createAccount(final String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        final DatabaseReference ref = database.getReference("app");

        mAuth.createUserWithEmailAndPassword(email, password)//이메일 형식으로 계정을 생성
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {//계정 생성 성공시
                            Log.d(TAG, "createUserWithEmail:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                            final String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            ref.child(encodeUserEmail(id)).child("device_token").setValue(deviceToken);

                            Toast.makeText(SignUpActivity.this, R.string.sign_up_success,
                                    Toast.LENGTH_SHORT).show();

                            if(rBtn3.isChecked()) {//사용자로 회원가입을 한 경우

                                Intent intent = new Intent(getBaseContext(), UserActivity.class);
                                startActivity(intent);
                                finish();
                            }

                            else{//보호자로 회원가입을 한 경우

                                   taken.addListenerForSingleValueEvent(new ValueEventListener() {//check if user exists and set this object bottom of the user's object
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            Iterator<DataSnapshot> child = dataSnapshot.getChildren().iterator();
                                            //users의 모든 자식들의 key값과 value 값들을 iterator로 참조

                                            while(child.hasNext())
                                            {
                                                //찾고자 하는 ID값은 key로 존재하는 값
                                                if(child.next().getKey().equals(encodeUserEmail(eText5.getText().toString())))
                                                {
                                                    //change con id val of user db
                                                    DatabaseReference proRef = database.getReference("app").child(encodeUserEmail(eText5.getText().toString()));
                                                    proRef.child("con_id").setValue(encodeUserEmail(id));
                                                    Toast.makeText(getApplicationContext(),"사용자 아이디가 존재합니다",Toast.LENGTH_LONG).show();
                                                    proRef.child("receiver_token").setValue(deviceToken);
                                                    return;
                                                }
                                            }
                                            Toast.makeText(getApplicationContext(),"존재하지 않는 사용자 아이디입니다.",Toast.LENGTH_LONG).show();
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                if(rBtn3.isChecked()) {
                                    SharedReference.setUserName(SignUpActivity.this, id, pw, 0);//when successed to sign up, set info on sharedReference

                                    final DatabaseReference myRef = database.getReference("app").child(encodeUserEmail(id));
                                    taken.child(encodeUserEmail(id)).setValue(true);

                                    myRef.child("blu_id").setValue("null");
                                    myRef.child("con_id").setValue("null");
                                    myRef.child("relationship").setValue(eRelText.getText().toString());
                                    myRef.child("gps").child("current").setValue("null");
                                    for(int i=1;i<=5;i++) {
                                        myRef.child("gps").child("history").child(Integer.toString(i)).child("loc").setValue("null");
                                        myRef.child("gps").child("history").child(Integer.toString(i)).child("time").setValue("null");
                                    }
                                    myRef.child("name").setValue(eText6.getText().toString());
                                    myRef.child("phone").setValue(eText7.getText().toString());
                                    myRef.child("type").setValue("user");

                                }
                                else {
                                    SharedReference.setUserName(SignUpActivity.this, id, pw, 1);
                                    final DatabaseReference proRef = database.getReference("app").child(encodeUserEmail(id));

                                    proRef.child("con_id").setValue(encodeUserEmail(eText5.getText().toString()));//user의 하위객체
                                    proRef.child("name").setValue(eText6.getText().toString());
                                    proRef.child("phone").setValue(eText7.getText().toString());
                                    proRef.child("type").setValue("protector");
                                }

                                Intent intent = new Intent(getBaseContext(), ProtectorActivity.class);
                                startActivity(intent);
                                finish();
                            }

                        } else {//회원가입에 실패한 경우(기존에 존재하던 아이디 또는 이메일 형식이 아님)
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, R.string.sign_up_fail,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_sign_up) {

            id = eText3.getText().toString();
            pw = eText4.getText().toString();
            connect = eText5.getText().toString();

            if(id.isEmpty()==false && pw.isEmpty()==false)
                createAccount(id, pw);
            else
                Toast.makeText(SignUpActivity.this, "아이디 또는 패스워드를 입력해주세요", Toast.LENGTH_SHORT).show();
        }
    }
}
