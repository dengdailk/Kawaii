package com.lk.kawaii.kawaii;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.snow.lk.kawaii.KawaiiView;



public class MainActivity extends AppCompatActivity {

    private KawaiiView KawaiiView;
    private Button buttonStrart;
    private Button buttonFinish;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        KawaiiView = (KawaiiView)findViewById(R.id.KawaiiView);

        buttonStrart = (Button) findViewById(R.id.start);
        buttonFinish = (Button) findViewById(R.id.finish);


        buttonStrart.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View v) {

                KawaiiView.startMoving();
            }
        });

        buttonFinish.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                KawaiiView.stopMoving();
            }
        });
    }
}
