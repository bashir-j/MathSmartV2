package com.pythagorithm.mathsmartv2.UILayer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pythagorithm.mathsmartv2.AppLogic.Student;
import com.pythagorithm.mathsmartv2.R;
import com.pythagorithm.mathsmartv2.UIConnector.UIConnector;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Random;

public class reportStudent extends AppCompatActivity {

    private String[] topicSet;
    private String[] timeSet;
    private UIConnector uic;
    private Student student;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_student);
        student = (Student)getIntent().getParcelableExtra("student");
        uic=new UIConnector(this);
        student.getAvgTime();
        student.getAssignmentscompletedScores();

    }

    public void showPieChart(HashMap<String,Integer> vals){
        Random rnd=new Random();
        PieChart mPieChart = (PieChart) findViewById(R.id.piechart);
        ViewGroup myRoot1 = (ViewGroup) findViewById(R.id.legendLayout);
        float[] values=new float[vals.size()];
        int[]colors= new int[vals.size()];
        topicSet=vals.keySet().toArray(new String[vals.size()]);
        int sum=0;
        for(int i=0;i<vals.size();i++){
            values[i]=(int)vals.values().toArray()[i];
            Log.d("Hussu","Array at:"+i+" value:"+values[i]);
            sum+=values[i];
        }
        for(int i=0;i<vals.size();i++){
            values[i]=(float)values[i]/sum*100;
            Log.d("Hussu","Array at:"+i+" value:"+values[i]);
            colors[i]=Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            mPieChart.addPieSlice(new PieModel(topicSet[i],values[i],colors[i]));
            View inflatedLayout = LayoutInflater.from(this).inflate(R.layout.legend_item, null, false);
            ((TextView) inflatedLayout.findViewById(R.id.colorSquare)).setBackgroundColor(colors[i]);
            ((TextView) inflatedLayout.findViewById(R.id.itemText)).setText(topicSet[i] + "   " + round(values[i],2) + "%");
            inflatedLayout.setPadding(10,5,0,0);
            myRoot1.addView(inflatedLayout);
        }
        mPieChart.startAnimation();
    }

    public void showBarChart(HashMap<String,Float> vals){
        BarChart mBarChart = (BarChart) findViewById(R.id.barchart);
        ViewGroup myRoot1 = (ViewGroup) findViewById(R.id.legendBarLayout);
        int color;
        timeSet=vals.keySet().toArray(new String[vals.size()]);
        Random rnd = new Random();
        for (int i = 0; i<vals.size(); i++){
            float score = vals.get(timeSet[i]);
            color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            mBarChart.addBar(new BarModel(score, color));
            View inflatedLayout = LayoutInflater.from(this).inflate(R.layout.legend_item, null, false);
            ((TextView) inflatedLayout.findViewById(R.id.colorSquare)).setBackgroundColor(color);
            ((TextView) inflatedLayout.findViewById(R.id.itemText)).setText(timeSet[i]);
            inflatedLayout.setPadding(10,5,0,0);
            myRoot1.addView(inflatedLayout);
        }
        mBarChart.startAnimation();
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public void backBtnClicked(View v){
        Intent newIntent = new Intent(this, Assignments.class);
        newIntent.putExtra("student",student);
        startActivity(newIntent);
    }
}
