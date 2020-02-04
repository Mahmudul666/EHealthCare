package com.example.ehealthcare;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ehealthcare.models.ModelBmi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class BMI_Calculator extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef,BMIDbRef,BMIDbRefview;
    String result,email,uid;
    private EditText bheight;
    private EditText bweight;
    private TextView bresult,brecord;
    private Button bcalculate,bmidata,bview;
    ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        BMIDbRef = FirebaseDatabase.getInstance().getReference("BMI");

        checkUserStatus();
        setContentView(R.layout.activity_bmi__calculator);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        bheight = (EditText)findViewById(R.id.txt_bmi_height);
        bweight = (EditText)findViewById(R.id.txt_bmi_weight);
        bresult= (TextView)findViewById(R.id.txt_bmi_result);
        brecord = (TextView)findViewById(R.id.txt_bmi_record);
        bmidata = (Button)findViewById(R.id.btn_bmi_data_save);

        bcalculate = (Button)findViewById(R.id.btn_bmi_cal);
        //record view
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        BMIDbRefview = FirebaseDatabase.getInstance().getReference("BMI").child(firebaseUser.getUid());
        BMIDbRefview.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String result = dataSnapshot.child("bResult").getValue().toString();
               brecord.setText("Your Last Time Result:"+"\n"+result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        bcalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String heightStr = bheight.getText().toString();
                String weightStr = bweight.getText().toString();
                if (heightStr != null && !"".equals(heightStr)
                        && weightStr != null && !"".equals(weightStr)

                ){
                    float heightvalue = Float.parseFloat(heightStr)/100;
                    float weightvalue = Float.parseFloat(weightStr);
                    float bmi = weightvalue/(heightvalue*heightvalue);
                    displayBMI(bmi);
                    bmidata.setVisibility(View.VISIBLE);

                }
            }
        });

        bmidata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addResult();
                bmidata.setVisibility(View.INVISIBLE);
            }
        });




    }


    private void addResult(){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String bResult = bresult.getText().toString().trim();
        String bTime = String.valueOf(System.currentTimeMillis());
        if(!TextUtils.isEmpty(bResult)){
            String id = firebaseUser.getUid();
            ModelBmi bmi = new ModelBmi(id,bResult,bTime);
            BMIDbRef.child(id).setValue(bmi);

        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            email = user.getEmail();
            uid = user.getUid();
//mProfileTv.setText(user.getEmail());
        }else{
            startActivity(new Intent( this,MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_reminder).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_clculate_bmi).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }


    private void displayBMI(float bmi) {
        String bmiLable = "";
        if (Float.compare(bmi,15f) <= 0){
            bmiLable = getString(R.string.very_severly_underweight);
        }else if(Float.compare(bmi, 15f) > 0 && Float.compare(bmi,16f) <= 0){
            bmiLable = getString(R.string.severly_underweight);
        }else if(Float.compare(bmi, 16f) > 0 && Float.compare(bmi,18.5f) <= 0){
            bmiLable = getString(R.string.underweight);
        }else if(Float.compare(bmi, 18.5f) > 0 && Float.compare(bmi,25f) <= 0){
            bmiLable = getString(R.string.normal);
        }else if(Float.compare(bmi, 25f) > 0 && Float.compare(bmi,30f) <= 0){
            bmiLable = getString(R.string.OverWight);
        }else if(Float.compare(bmi, 30f) > 0 && Float.compare(bmi,35f) <= 0){
            bmiLable = getString(R.string.Obese_class_i);
        }else if(Float.compare(bmi, 35f) > 0 && Float.compare(bmi,40f) <= 0){
            bmiLable = getString(R.string.Obese_class_ii);
        }else{
            bmiLable = getString(R.string.Obese_class_iii);
        }
        bmiLable = "\n"+bmi + "\n" + bmiLable;
        bresult.setText(bmiLable);
    }
}
