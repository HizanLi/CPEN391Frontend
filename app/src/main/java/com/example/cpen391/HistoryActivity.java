package com.example.cpen391;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        BarChart chartTemp = findViewById(R.id.barcharttemp);
        BarChart chartHum = findViewById(R.id.barcharthum);

        ArrayList<BarEntry> NoOfEmp = new ArrayList<BarEntry>();

        NoOfEmp.add(new BarEntry(1, 10));
        NoOfEmp.add(new BarEntry(2, 11));
        NoOfEmp.add(new BarEntry(3, 12));
        NoOfEmp.add(new BarEntry(4, 13));
        NoOfEmp.add(new BarEntry(5, 14));
        NoOfEmp.add(new BarEntry(6, 15));
        NoOfEmp.add(new BarEntry(7, 16));
        NoOfEmp.add(new BarEntry(8, 17));
        NoOfEmp.add(new BarEntry(9, 18));
        NoOfEmp.add(new BarEntry(10, 20));

        ArrayList<String> year = new ArrayList<String>();

        year.add("2008");
        year.add("2009");
        year.add("2010");
        year.add("2011");
        year.add("2012");
        year.add("2013");
        year.add("2014");
        year.add("2015");
        year.add("2016");
        year.add("2017");

        BarDataSet bardataset = new BarDataSet(NoOfEmp, "Temperature");
        chartHum.animateY(10);
        chartTemp.animateY(10);
        BarData data = new BarData(bardataset, bardataset);
        bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
        chartHum.setData(data);
        chartTemp.setData(data);
    }
}


