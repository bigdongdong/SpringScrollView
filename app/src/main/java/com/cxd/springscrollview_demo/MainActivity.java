package com.cxd.springscrollview_demo;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cxd.springscrolllinearlayout.SpringScrollView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SpringScrollView ssll ;
    private RecyclerView recycler ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ssll = findViewById(R.id.ssll);
        recycler = findViewById(R.id.recycler);

//        final int count = 5;
//        for (int i = 0; i < count; i++) {
//            TextView tv = new TextView(this);
//            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, 200);
//            p.setMargins(50, 10, 50, 10);
//            tv.setLayoutParams(p);
//            tv.setBackgroundColor(Color.GRAY);
//            tv.setGravity(Gravity.CENTER);
//            tv.setTextSize(20);
//            tv.setText(i + "");
//            tv.setTextColor(Color.WHITE);
//            final int finalI = i ;
//            tv.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(MainActivity.this,""+finalI,Toast.LENGTH_SHORT).show();
//                }
//            });
//            ssll.addView(tv);
//        }


        recycler.setLayoutManager(new LinearLayoutManager(this));
//        {
//            @Override
//            public boolean canScrollVertically() {
//                return false;
//            }
//        });
        final QuickAdapter adapter = adapter();
        List list1 = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list1.add(null);
        }
        recycler.setAdapter(adapter);

        adapter.update(list1);

    }

    private QuickAdapter adapter(){
        return new QuickAdapter(this) {
            @Override
            protected Object getEmptyIdOrView() {
                return null;
            }

            @Override
            protected Object getItemViewOrId() {
                TextView tv = new TextView(context);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, 150);
                p.setMargins(50, 10, 50, 10);
                tv.setLayoutParams(p);
                tv.setBackgroundColor(Color.GRAY);
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(20);
                tv.setTextColor(Color.WHITE);
                return tv;
            }

            @Override
            protected void onBindViewHolder(@NotNull ViewHolder holder, Object o,final int position) {
                TextView tv = (TextView) holder.itemView;
                tv.setText("recycler:"+position);

                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context,"recycler:"+position,Toast.LENGTH_SHORT).show();
                    }
                });

            }
        };
    }

}