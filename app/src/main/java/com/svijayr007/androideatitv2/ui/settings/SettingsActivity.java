
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

package com.svijayr007.androideatitv2.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.svijayr007.androideatitv2.Adapter.MyCampusAdapter;
import com.svijayr007.androideatitv2.Common.Common;
import com.svijayr007.androideatitv2.Common.SpacesItemDecoration;
import com.svijayr007.androideatitv2.Database.CartDataSource;
import com.svijayr007.androideatitv2.Database.CartDatabase;
import com.svijayr007.androideatitv2.Database.LocalCartDataSource;
import com.svijayr007.androideatitv2.EventBus.CampusSelectedEvent;
import com.svijayr007.androideatitv2.MainActivity;
import com.svijayr007.androideatitv2.Model.CampusModel;
import com.svijayr007.androideatitv2.R;
import com.svijayr007.androideatitv2.sharedPreference.SharedPref;
import com.svijayr007.androideatitv2.ui.no_internet.NoInternetActivity;
import com.svijayr007.androideatitv2.ui.signup.CampusViewModel;
import com.svijayr007.androideatitv2.ui.suggestions.SuggestionsActivity;
import com.thefinestartist.finestwebview.FinestWebView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.customerly.Customerly;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import spencerstudios.com.bungeelib.Bungee;

public class SettingsActivity extends AppCompatActivity {


    @BindView(R.id.user_name)
    TextView user_name;
    @BindView(R.id.edit_name_email)
    TextView edit_name_email;
    @BindView(R.id.user_phone)
    TextView user_phone;
    @BindView(R.id.user_email)
    TextView user_email;

    TextView  inviteTV, ratingTV, privacyTV, logoutTV,suggestionsTV,changeCampusTV,mailUsTV,websiteTV,supportTV,helpCenterTV;

    ImageView backIV;
    KProgressHUD hud1;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CartDataSource cartDataSource;

    //Campus
    private CampusViewModel campusViewModel;
    private  AlertDialog dialogCampus;

    private FirebaseAnalytics mFirebaseAnalytics;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        ButterKnife.bind(this);
        campusViewModel = ViewModelProviders.of(this).get(CampusViewModel.class);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

        hud1 = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.75f);
        initUi();
        setListener();






    }

    private void setListener() {

        //Customerly support
        supportTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Customerly.openSupport(SettingsActivity.this);
            }
        });
        //Back Image View
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //logout Handle
        logoutTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //logout customerly
                Customerly.logoutUser();
                Common.currentRestaurant = null;
                Common.currentUser = null;
                FirebaseAuth.getInstance().signOut();
                SharedPref.removeAll(getApplicationContext());


                //Intent to main activity
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        });

        //Privacy Documentation
        privacyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Common.isInternetAvailable(getApplicationContext())){
                    //Common.openCustomBrowser(getApplicationContext(), Common.onCampusPrivacy);
                    try {
                        new FinestWebView.Builder(SettingsActivity.this)
                                .webViewAppCacheEnabled(true)
                                .showProgressBar(true)
                                .toolbarColor(getResources().getColor(R.color.colorPrimaryDark))

                                .swipeRefreshColor(getResources().getColor(R.color.colorPrimaryDark))
                                .webViewSupportZoom(true)
                                .webViewUserAgentString("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0")
                                .show(Common.onCampusPrivacy);

                    }catch (Exception e){
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), NoInternetActivity.class);
                    startActivity(intent);
                    Bungee.fade(SettingsActivity.this);
                }

            }
        });
        //website
        websiteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Common.isInternetAvailable(getApplicationContext())){
                    //Common.openCustomBrowser(getApplicationContext(), Common.onCampusWebsite);
                    try {
                        new FinestWebView.Builder(SettingsActivity.this)
                                .webViewAppCacheEnabled(true)
                                .showProgressBar(true)
                                .toolbarColor(getResources().getColor(R.color.colorPrimaryDark))
                                .swipeRefreshColor(getResources().getColor(R.color.colorPrimaryDark))
                                .webViewSupportZoom(true)
                                .webViewUserAgentString("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0")
                                .show(Common.onCampusWebsite);
                    }catch (Exception e){
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    Intent intent = new Intent(getApplicationContext(), NoInternetActivity.class);
                    startActivity(intent);
                    Bungee.fade(SettingsActivity.this);
                }



            }
        });

        //Help Center
        helpCenterTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Common.isInternetAvailable(getApplicationContext())){
                    //Common.openCustomBrowser(getApplicationContext(), Common.onCampusWebsite);
                    try {
                        new FinestWebView.Builder(SettingsActivity.this)
                                .webViewAppCacheEnabled(true)
                                .showProgressBar(true)
                                .toolbarColor(getResources().getColor(R.color.colorPrimaryDark))
                                .swipeRefreshColor(getResources().getColor(R.color.colorPrimaryDark))
                                .webViewSupportZoom(true)
                                .webViewUserAgentString("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0")
                                .show(Common.onCampusHelpCenter);
                    }catch (Exception e){
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    Intent intent = new Intent(getApplicationContext(), NoInternetActivity.class);
                    startActivity(intent);
                    Bungee.fade(SettingsActivity.this);
                }
            }
        });

        //Rate our app
        ratingTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Common.isInternetAvailable(getApplicationContext())) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), NoInternetActivity.class);
                    startActivity(intent);
                    Bungee.fade(SettingsActivity.this);
                }


            }
        });

        //invite friends
        inviteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Hi all! Get your food delivered to your table or to your hostel room from your campus cafe's! Click here to download and order food: https://play.google.com/store/apps/details?id=" + SettingsActivity.this.getPackageName();
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent,"Share via"));

            }
        });


        //suggestions
        suggestionsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SuggestionsActivity.class));
                Bungee.slideLeft(SettingsActivity.this);
            }
        });

        changeCampusTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this,android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
                builder.setTitle("Change Your Campus!");
                View v = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.layout_campus,null);
                RecyclerView recycler_campus = v.findViewById(R.id.recycler_campus);
                campusViewModel.getMessageError().observe(SettingsActivity.this, new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        Toast.makeText(SettingsActivity.this, ""+s, Toast.LENGTH_SHORT).show();
                    }
                });
                campusViewModel.getCampusListMutable().observe(SettingsActivity.this, new Observer<List<CampusModel>>() {
                    @Override
                    public void onChanged(List<CampusModel> campusModelList) {
                        MyCampusAdapter campusAdapter = new MyCampusAdapter(SettingsActivity.this,campusModelList);
                        recycler_campus.setAdapter(campusAdapter);
                        recycler_campus.setLayoutManager(new LinearLayoutManager(SettingsActivity.this));
                        recycler_campus.setVerticalScrollBarEnabled(true);
                        recycler_campus.addItemDecoration(new SpacesItemDecoration(5));
                    }
                });
                builder.setView(v);
                dialogCampus = builder.create();
                dialogCampus.show();

            }
        });

        //Mail
        mailUsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
               emailIntent.setData(Uri.parse("mailto:contact@oncampus.in"));
               try{
                   startActivity(emailIntent);

               }catch (Exception e){
                   e.printStackTrace();
               }
            }
        });



        //Edit name and email
        edit_name_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this,android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
                builder.setTitle("Edit Account!");
                View v = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.layout_edit_name_email,null);
                EditText text_user_name = v.findViewById(R.id.userName);
                TextView text_update = v.findViewById(R.id.text_update);
                text_user_name.setText(new StringBuilder()
                .append(Common.currentUser.getName()));

                builder.setView(v);
                dialogCampus = builder.create();
                dialogCampus.show();
                text_update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(text_user_name.getText().toString().isEmpty()){
                            text_user_name.setError("Name Required");
                            return;
                        }
                        else {
                            if(!TextUtils.equals(text_user_name.getText().toString(),Common.currentUser.getName())){
                                // only Name Update
                                hud1.show();
                                Map<String , Object> nameUpdate = new HashMap<>();
                                nameUpdate.put("name",text_user_name.getText().toString().trim());
                                FirebaseDatabase.getInstance(Common.usersDB)
                                        .getReference(Common.USER_REFERENCES)
                                        .child(Common.currentUser.getUid())
                                        .updateChildren(nameUpdate)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                hud1.dismiss();
                                                dialogCampus.dismiss();

                                            }
                                        }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            finishAffinity();
                                            startActivity(mainIntent);
                                            hud1.dismiss();
                                            dialogCampus.dismiss();
                                        }

                                    }
                                });

                            }
                            else {
                                dialogCampus.dismiss();
                            }
                        }
                    }
                });

            }
        });
    }

    private void initUi() {
        hud1.show();
        suggestionsTV = findViewById(R.id.suggestionsTV);
        inviteTV = findViewById(R.id.inviteTV);
        ratingTV = findViewById(R.id.rateTV);
        privacyTV = findViewById(R.id.privacyTV);
        logoutTV = findViewById(R.id.logoutTV);
        changeCampusTV = findViewById(R.id.text_change_campus_name);
        backIV = findViewById(R.id.backIV);
        mailUsTV = findViewById(R.id.mailUsTV);
        websiteTV = findViewById(R.id.websiteTV);
        supportTV = findViewById(R.id.supportTV);
        helpCenterTV = findViewById(R.id.helpCenterTV);

        //Current user Details
        user_name.setText(new StringBuilder()
        .append(Common.currentUser.getName()));
        user_phone.setText(new StringBuilder()
        .append(Common.currentUser.getPhone().substring(3)));
        user_email.setText(new StringBuilder()
        .append(Common.currentUser.getEmail()));



        //Current campus
        FirebaseDatabase.getInstance(Common.serverValues)
                .getReference(Common.CAMPUS_REF)
                .child(Common.currentUser.getCampusId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            CampusModel campusModel = snapshot.getValue(CampusModel.class);
                            changeCampusTV.setText(campusModel.getName());
                            hud1.dismiss();

                        }else {
                            hud1.dismiss();
                            Toast.makeText(SettingsActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        hud1.dismiss();
                        Toast.makeText(SettingsActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();

                    }
                });


    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCampusClicked(CampusSelectedEvent event){
        if(!TextUtils.equals(event.getSelectedCampus().getCampusId(),Common.currentUser.getCampusId())) {
            //Update user current campus
            hud1.show();

            //Unsubscribe to old campus topic
            FirebaseMessaging.getInstance()
                    .unsubscribeFromTopic(new StringBuilder()
                    .append("client")
                    .append("_")
                    .append(Common.currentUser.getCampusId()).toString())
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("campusId", event.getSelectedCampus().getCampusId());

                            FirebaseDatabase.getInstance(Common.usersDB)
                                    .getReference(Common.USER_REFERENCES)
                                    .child(Common.currentUser.getUid())
                                    .updateChildren(updateData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //Clear cart first
                                                compositeDisposable.add(cartDataSource.countItemInCart(Common.currentUser.getUid())
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(integer -> {
                                                            //Cart not empty
                                                            //clean cart
                                                            compositeDisposable.add(cartDataSource.cleanCart(Common.currentUser.getUid())
                                                                    .subscribeOn(Schedulers.io())
                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                    .subscribe(integer1 -> {
                                                                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                        finishAffinity();
                                                                        startActivity(mainIntent);
                                                                        hud1.dismiss();
                                                                        dialogCampus.dismiss();

                                                                    }, throwable -> {
                                                                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                        finishAffinity();
                                                                        startActivity(mainIntent);
                                                                        hud1.dismiss();
                                                                        dialogCampus.dismiss();

                                                                    }));

                                                        }, throwable -> {
                                                            if (throwable.getMessage().contains("empty")) {
                                                                //Cart empty
                                                                //Start as new app
                                                                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                finishAffinity();
                                                                startActivity(mainIntent);
                                                                hud1.dismiss();
                                                                dialogCampus.dismiss();
                                                            }

                                                        }));

                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    hud1.dismiss();
                                    Toast.makeText(SettingsActivity.this, "Something went wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    dialogCampus.dismiss();

                                }
                            });

                        }
                    }).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("campusId", event.getSelectedCampus().getCampusId());

                    FirebaseDatabase.getInstance(Common.usersDB)
                            .getReference(Common.USER_REFERENCES)
                            .child(Common.currentUser.getUid())
                            .updateChildren(updateData)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //Clear cart first
                                        compositeDisposable.add(cartDataSource.countItemInCart(Common.currentUser.getUid())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(integer -> {
                                                    //Cart not empty
                                                    //clean cart
                                                    compositeDisposable.add(cartDataSource.cleanCart(Common.currentUser.getUid())
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(integer1 -> {
                                                                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                finishAffinity();
                                                                startActivity(mainIntent);
                                                                hud1.dismiss();
                                                                dialogCampus.dismiss();

                                                            }, throwable -> {
                                                                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                finishAffinity();
                                                                startActivity(mainIntent);
                                                                hud1.dismiss();
                                                                dialogCampus.dismiss();

                                                            }));

                                                }, throwable -> {
                                                    if (throwable.getMessage().contains("empty")) {
                                                        //Cart empty
                                                        //Start as new app
                                                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        finishAffinity();
                                                        startActivity(mainIntent);
                                                        hud1.dismiss();
                                                        dialogCampus.dismiss();
                                                    }

                                                }));

                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hud1.dismiss();
                            Toast.makeText(SettingsActivity.this, "Something went wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            dialogCampus.dismiss();

                        }
                    });

                }
            });

        }
        else {
            dialogCampus.dismiss();
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Bungee.slideRight(SettingsActivity.this);
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
    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}