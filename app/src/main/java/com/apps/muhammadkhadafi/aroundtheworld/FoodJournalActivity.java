package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

/**
 * Created by muhammadkhadafi on 12/5/14.
 */
public class FoodJournalActivity extends Activity {
    Button btnBack;
    Button btnGetFood;
    int calMonth;
    int calDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_journal);

        btnBack = (Button) findViewById(R.id.btn_back);
        btnGetFood = (Button) findViewById(R.id.btn_getfood);
        final CalendarView calendarView = (CalendarView) findViewById(R.id.cal_foodcalendar);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
//                Toast.makeText(getApplicationContext(), "" + dayOfMonth, Toast.LENGTH_SHORT).show();// TODO Auto-generated method stub
                calDay = dayOfMonth;
                calMonth = month;
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
        });
        btnGetFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DailyJournalActivity.class);
                Bundle b = new Bundle();
                b.putInt("day", calDay); //Your id
                b.putInt("month", calMonth);
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
                finish();
            }
        });



    }
}