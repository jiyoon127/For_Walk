package com.example.forwalk;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity {

    private RecyclerView rView1;
    private GpsAdapter mGpsAdapter;
    private List<GpsItem> mGpsArray;
    private LinearLayoutManager layoutManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        init();
        dataInit();
    }

    private void init(){
        rView1 = (RecyclerView) findViewById(R.id.rView1);
    }

    private void dataInit(){
        mGpsArray = new ArrayList<GpsItem>();

        for(int i=0;i<5;i++){
            GpsItem item = new GpsItem();
            item.setNum(i+1);
            item.setTime("0000년 00월 00일 00시 00분");

            mGpsArray.add(item);
        }

        layoutManager = new LinearLayoutManager(MapsActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rView1.setLayoutManager(layoutManager);
        mGpsAdapter = new GpsAdapter(mGpsArray);

        rView1.setAdapter(mGpsAdapter);
        rView1.setItemAnimator(new DefaultItemAnimator());
    }



}
