package com.example.baidumap;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

public class IndoorActivity extends AppCompatActivity {
    private ListView floorListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor);
        floorListView = findViewById(R.id.lv_floors);
        floorListView.setAdapter(new IndoorListAdapter(IndoorActivity.this,null));
    }
}
