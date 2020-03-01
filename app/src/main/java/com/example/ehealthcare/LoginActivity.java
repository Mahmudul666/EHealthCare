package com.example.ehealthcare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity<LradioGroup> extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100 ;
    GoogleSignInClient mGoogleSignInClient;
    EditText mEmailEt,mPasswordEt;
    Button mLoginBtn;
    private RadioButton LradioButtonAdmin,LradioButtonDoctor, LradioButtonUser;
    private RadioGroup LradioGroup;

    TextView mnothaveAcc, mRecoverpass;
    //SignInButton mGoogleLoginBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference dDatabase, pDatabase,aDatabase;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(LoginActivity.this.getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

 mGoogleSignInClient = GoogleSignIn.getClient(this,gso);



        mAuth = FirebaseAuth.getInstance();
        dDatabase = FirebaseDatabase.getInstance().getReference().child("Doctors");
        pDatabase = FirebaseDatabase.getInstance().getReference().child("Patients");
        aDatabase = FirebaseDatabase.getInstance().getReference().child("Admin");
        LradioGroup = (RadioGroup)findViewById(R.id.login_radio_group);
        LradioButtonAdmin = findViewById(R.id.radio_btn_Admin);
        LradioButtonDoctor = findViewById(R.id.radio_btn_doctor);
        LradioButtonUser = findViewById(R.id.radio_btn_User);
        mEmailEt = findViewById(R.id.emailE);
        mPasswordEt = findViewById(R.id.passwordE);
        mnothaveAcc = findViewById(R.id.nothave_accountTv);
        mRecoverpass = findViewById(R.id.recoveryPassTv);
//        mGoogleLoginBtn = findViewById(R.id.googleLoginBtn);
        mLoginBtn = findViewById(R.id.login_Btn);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                
                else{
                    loginUser(email,password);
                }
            }
        });
        mnothaveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent( LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });
        mRecoverpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDialog();
            }
        });

//        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
////                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
////                    startActivityForResult(signInIntent, RC_SIGN_IN);
//                Toast.makeText(LoginActivity.this, "Will be Implemented Soon ", Toast.LENGTH_LONG).show();
//
//
//            }
//        });
        pd = new ProgressDialog(this);

    }

    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(16);
        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });
        builder.setNegativeButton("Cencle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginRecovery(String email) {
        pd.setMessage("Sending Email.....");
        pd.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(LoginActivity.this,"Email Sent..",Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(LoginActivity.this," Failed",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String password) {
        pd.setMessage("Loging In.....");
        pd.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            pd.dismiss();

                            if (LradioButtonDoctor.isChecked()){
                                CheckDoctorExist();
                            }else if(LradioButtonUser.isChecked()){
                                CheckPatientExist();
                            } else if(LradioButtonAdmin.isChecked()){
                                CheckAdminExist();
                            }

                            else{
                                Toast.makeText(LoginActivity.this, "User Does Not Exist In The Database ",
                                        Toast.LENGTH_LONG).show();
                            }
                            //finish code
                        }else{
                            pd.dismiss();
                            Toast.makeText(LoginActivity.this,"Authentication failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void CheckAdminExist() {
        final String uid = mAuth.getCurrentUser().getUid();
        aDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(uid)){
                    Toast.makeText(LoginActivity.this,"Login sucessful as Admin",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent( LoginActivity.this, AdminDashboardActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void CheckPatientExist() {
        final String uid = mAuth.getCurrentUser().getUid();
        pDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(uid)){
                    Toast.makeText(LoginActivity.this,"Login sucessful as Patient",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent( LoginActivity.this, PatientDashboardActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void CheckDoctorExist() {

        final String uid = mAuth.getCurrentUser().getUid();
        dDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(uid)){
                    Toast.makeText(LoginActivity.this,"Login Sucessful as Doctor",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent( LoginActivity.this, DashboardActivity.class));
                   finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();


            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();

                            if(task.getResult().getAdditionalUserInfo().isNewUser()){
                                String email = user.getEmail();
                                String uid = user.getUid();
                                HashMap<Object,String> hashMap = new HashMap<>();
                                hashMap.put("email" , email);
                                hashMap.put("uid" , uid);
                                hashMap.put("name" , "");
                                hashMap.put("onlineStatus" , "online");
                                hashMap.put("typingTo" , "noOne");
                                hashMap.put("phone" , "");
                                hashMap.put("image" , "");
                                hashMap.put("cover" , "");

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference reference = database.getReference("Users");
                                reference.child(uid).setValue(hashMap);
                            }


//                            Toast.makeText(LoginActivity.this,""+user.getEmail(),Toast.LENGTH_SHORT).show();
//                            startActivity(new Intent( LoginActivity.this, DashboardActivity.class));
//                            finish();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this,"Login Failed",Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
