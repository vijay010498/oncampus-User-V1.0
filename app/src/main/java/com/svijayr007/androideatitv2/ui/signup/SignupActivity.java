/*
 * New BSD License
 *
 * Copyright © 2020 Vijayaraghavan (https://www.oncampus.in) All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 *  Neither the name of “onCampus Private Limited” nor the names of its contributors may be used to
 *   endorse or promote products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.svijayr007.androideatitv2.ui.signup;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.svijayr007.androideatitv2.Adapter.MyCampusAdapter;
import com.svijayr007.androideatitv2.Common.Common;
import com.svijayr007.androideatitv2.Common.SpacesItemDecoration;
import com.svijayr007.androideatitv2.EventBus.CampusSelectedEvent;
import com.svijayr007.androideatitv2.MainActivity;
import com.svijayr007.androideatitv2.Model.CampusModel;
import com.svijayr007.androideatitv2.Model.UserModel;
import com.svijayr007.androideatitv2.R;
import com.svijayr007.androideatitv2.sharedPreference.SharedPref;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class SignupActivity extends AppCompatActivity {
    private EditText edtName;
    private TextView gmail;
    private TextView textCampusName;
    private ImageView closeImage;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private MaterialButton buttonRegister;
    private DatabaseReference userRef;
    private static  CampusModel selectedCampus;

    //Campus
    private CampusViewModel campusViewModel;
    private  AlertDialog dialogCampus;
    private KProgressHUD hud;

    //Google SIgn in
    private static  final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mgoogleSignInClient;
    private static String googleIdToken ;

    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        campusViewModel = ViewModelProviders.of(this).get(CampusViewModel.class);
        init();
        setListener();

    }

    private void init() {
        hud = KProgressHUD.create(SignupActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setCancellable(true)
                .setLabel("Creating Account")
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);


        edtName = findViewById(R.id.edit_name);
        gmail = findViewById(R.id.gmail);
        textCampusName = findViewById(R.id.text_campus_name);
        closeImage = findViewById(R.id.image_close);
        mAuth  = FirebaseAuth.getInstance();
        user  = mAuth.getCurrentUser();
        buttonRegister = findViewById(R.id.button_register);
        userRef = FirebaseDatabase.getInstance(Common.usersDB).getReference(Common.USER_REFERENCES);

        //Google signin
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                    .requestIdToken(getString(R.string.default_web_client_id))
                                                    .requestEmail()
                                                    .requestProfile()
                                                    .build();
        mgoogleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try{
                GoogleSignInAccount account = task.getResult();
                googleIdToken = account.getIdToken();
                gmail.setText(new StringBuilder()
                .append(account.getEmail()));
            }catch (Exception e){
                if(e.getMessage().contains("12501")){
                    Toast.makeText(this, "No Email selected", Toast.LENGTH_SHORT).show();
                    gmail.setHint("Link Email!");
                }
                else
                    Toast.makeText(this, "Gmail Error"+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }
    }



    private void setListener() {
        //for close image
        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //Implement later for choose campus
        textCampusName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this,android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
                builder.setTitle("Select Your Campus!");
                View v = LayoutInflater.from(SignupActivity.this).inflate(R.layout.layout_campus,null);
                RecyclerView recycler_campus = v.findViewById(R.id.recycler_campus);
                campusViewModel.getMessageError().observe(SignupActivity.this, new Observer<String>() {
                    @Override
                    public void onChanged(String message) {
                        Toast.makeText(SignupActivity.this, ""+message, Toast.LENGTH_SHORT).show();

                    }
                });
                campusViewModel.getCampusListMutable().observe(SignupActivity.this, new Observer<List<CampusModel>>() {
                    @Override
                    public void onChanged(List<CampusModel> campusModelList) {
                        Log.i("CAMPUS CHECk-2",campusModelList.get(0).getName());
                        MyCampusAdapter campusAdapter = new MyCampusAdapter(SignupActivity.this,campusModelList);
                        Log.i("CAMPUS CHECK-3",String.valueOf(campusAdapter.getItemCount()));
                        recycler_campus.setAdapter(campusAdapter);
                        recycler_campus.setLayoutManager(new LinearLayoutManager(SignupActivity.this));
                        recycler_campus.setVerticalScrollBarEnabled(true);
                        recycler_campus.addItemDecoration(new SpacesItemDecoration(5));
                    }
                });
                builder.setView(v);
                dialogCampus = builder.create();
                dialogCampus.show();
            }
        });



        //Google signin
        gmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mgoogleSignInClient != null){
                    mgoogleSignInClient.signOut();
                    gmail.setText("");
                }
                Intent signInIntent = mgoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });




        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(edtName.getText().toString())){
                    edtName.setError("Name Required");
                    return;
                }
                else if(TextUtils.isEmpty(gmail.getText().toString()) ||
                        !isValidMail(gmail.getText().toString())){
                    gmail.setError("Email Empty or not valid");
                    return;
                }
               else if(TextUtils.isEmpty(textCampusName.getText().toString())){
                    textCampusName.setError("Please select a campus");
                    return;
                }
               hud.show();
                AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken,null);
                // For Unlink
                // mAuth.getCurrentUser().unlink(credential.getProvider());
                mAuth.getCurrentUser().linkWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    UserModel userModel = new UserModel();
                                    userModel.setUid(user.getUid());
                                    userModel.setName(edtName.getText().toString().trim());
                                    userModel.setPhone(user.getPhoneNumber());
                                    userModel.setEmail(gmail.getText().toString().trim());
                                    userModel.setCampusId(selectedCampus.getCampusId());
                                    userModel.setLastVisited(System.currentTimeMillis());

                                    userRef.child(user.getUid()).setValue(userModel)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        FirebaseAuth.getInstance().getCurrentUser()
                                                                .getIdToken(true)
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        hud.dismiss();
                                                                        Toast.makeText(SignupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                                                    }
                                                                }).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                                                hud.dismiss();
                                                                Common.authorizeKey = task.getResult().getToken();
                                                                //Veg only shared pref default veg_only is false
                                                                SharedPref.putBoolean(SignupActivity.this,"isVegOnly",false);
                                                                Toast.makeText(SignupActivity.this, "Congratulations ! Register Successful", Toast.LENGTH_SHORT).show();
                                                                goToMainActivity(userModel);
                                                            }
                                                        });


                                                    }

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            hud.dismiss();
                                            Toast.makeText(SignupActivity.this, "Creating Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hud.dismiss();
                        if(e.getMessage().contains("already associated with a different")){
                            Toast.makeText(SignupActivity.this, "Email Account Already Added with different account", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(SignupActivity.this, "Linking Error"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });



            }
        });




    }
    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void goToMainActivity(UserModel userModel) {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        Common.currentUser = userModel;
                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                        finish();
                    }
                }).addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                Common.currentUser = userModel;
                Common.createToken(SignupActivity.this,task.getResult().getToken());
                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                finish();

            }
        });
    }

    @Override
    public void onBackPressed() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(SignupActivity.this)
                .setTitle("Cancel Process?")
                .setMessage("Are you sure want to cancel the registration process?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       SignupActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCampusClicked(CampusSelectedEvent event){
        selectedCampus = event.getSelectedCampus();
        textCampusName.setText(new StringBuilder()
        .append(event.getSelectedCampus().getName()));
        dialogCampus.dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}