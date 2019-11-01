package com.example.forwalk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoadingActivity extends Activity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.loading);
        mAuth = FirebaseAuth.getInstance();
        String email, pw;
        final int type;
        email = SharedReference.getUserName(LoadingActivity.this);
        pw = SharedReference.getUserPw(LoadingActivity.this);
        type = SharedReference.getUserType(LoadingActivity.this);
        SharedReference.clearUserName(LoadingActivity.this);

        if (SharedReference.getUserName(LoadingActivity.this).length() == 0) {
            startLoading(MainActivity.class);
        }//not logged in : call mainActivity
        else {
            mAuth.signInWithEmailAndPassword(email, pw)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if (type == 0) {
                                    startLoading(UserActivity.class);//if it is user open UserActivity
                                } else {
                                    startLoading(ProtectorActivity.class);//if it is protector open ProtectorActivity
                                }
                            } else {//failed to log in
                                Toast.makeText(getApplicationContext(), "로그인에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                                startLoading(MainActivity.class);//open mainActivity
                            }
                        }
                    });
        }
    }
    private void startLoading (final Class context)
    {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getBaseContext(), context);
                startActivity(intent);
                finish(); }
            }, 2000);
        }
    }
