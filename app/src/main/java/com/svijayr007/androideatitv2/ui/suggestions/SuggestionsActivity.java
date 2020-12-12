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

package com.svijayr007.androideatitv2.ui.suggestions;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.svijayr007.androideatitv2.Common.Common;
import com.svijayr007.androideatitv2.Model.OrderModel;
import com.svijayr007.androideatitv2.Model.SuggestionsModel;
import com.svijayr007.androideatitv2.R;
import com.svijayr007.androideatitv2.callback.ILoadTimeFromFirebaseListener;
import com.svijayr007.androideatitv2.ui.no_internet.NoInternetActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

import spencerstudios.com.bungeelib.Bungee;

public class SuggestionsActivity extends AppCompatActivity implements ILoadTimeFromFirebaseListener {

   EditText et_suggestions;
   TextView text1TV, buttonTV;
   LottieAnimationView lottie;
   CardView submitCV;
   ImageView backIV;
    private FirebaseAnalytics mFirebaseAnalytics;

    ILoadTimeFromFirebaseListener listener;
    private KProgressHUD hud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        listener = this;

        //hud
        hud = KProgressHUD.create(SuggestionsActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setCancellable(false)
                .setLabel("Please Wait")
                .setDetailsLabel("Sending data")
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);


        et_suggestions = findViewById(R.id.et_suggestions);
        submitCV = findViewById(R.id.submitCV);
        lottie = findViewById(R.id.lottie);
        text1TV = findViewById(R.id.text1TV);
        buttonTV = findViewById(R.id.buttonTV);
        backIV = findViewById(R.id.backIV);

        //back IMAGE VIEW
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //submit card view
        submitCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Common.isInternetAvailable(getApplicationContext())){
                    if(buttonTV.getText().equals("Submit")){
                        String et_text  = et_suggestions.getEditableText().toString().trim();
                        if(et_text.length() > 1){
                            //All ok send data to firebase
                            hud.show();
                            SuggestionsModel suggestionsModel = new SuggestionsModel();
                            suggestionsModel.setUid(Common.currentUser.getUid());
                            suggestionsModel.setName(Common.currentUser.getName());
                            suggestionsModel.setEmail(Common.currentUser.getEmail());
                            suggestionsModel.setPhone(Common.currentUser.getPhone());
                            suggestionsModel.setSuggestion(et_text);

                            //
                            syncLocalTimeWithGlobaltime(suggestionsModel);

                        }else {
                            Toast toast = Toast.makeText(SuggestionsActivity.this,"Please write something",Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER,0,0);
                            toast.show();
                        }
                    }else
                        onBackPressed();

                }else {
                    Intent intent = new Intent(getApplicationContext(), NoInternetActivity.class);
                    startActivity(intent);
                    Bungee.fade(SuggestionsActivity.this);
                }

            }
        });


    }

    private void syncLocalTimeWithGlobaltime(SuggestionsModel suggestionsModel) {
        final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long offset = snapshot.getValue(Long.class);
                long estimatedServerTimeMs = System.currentTimeMillis() + offset ; //offset is missing time b/w local time and server time
                SimpleDateFormat sdf = new SimpleDateFormat("MM dd,yyyy hh:mm a");
                Date resultDate = new Date(estimatedServerTimeMs);
                Log.d("TEST DATE",""+sdf.format(resultDate));
                listener.onLoadTimeSuccess(suggestionsModel,estimatedServerTimeMs);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onLoadTimeFailed(error.getMessage());

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Bungee.slideRight(SuggestionsActivity.this);
    }

    @Override
    public void onLoadTimeSuccess(OrderModel orderModel, long estimateTimeInMs) {

    }


    @Override
    public void onLoadTimeSuccess(SuggestionsModel suggestionsModel, long estimateTimeInMs) {
        suggestionsModel.setCreateDate(estimateTimeInMs);
        SimpleDateFormat sdf = new SimpleDateFormat("MM dd,yyyy hh:mm a");
        Date resultDate = new Date(estimateTimeInMs);
        suggestionsModel.setDate(sdf.format(resultDate));
        writeSuggestionToFirebase(suggestionsModel);

    }

    private void writeSuggestionToFirebase(SuggestionsModel suggestionsModel) {
        //write to firebase
        FirebaseDatabase.getInstance(Common.suggestionsDB)
                .getReference(Common.SUGGESTIONS_REF)
                .push()
                .setValue(suggestionsModel)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hud.dismiss();
                        Toast toast = Toast.makeText(SuggestionsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();

                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Suggestion updated update UI
                hud.dismiss();
                lottie.setVisibility(View.VISIBLE);
                text1TV.setVisibility(View.VISIBLE);
                et_suggestions.setVisibility(View.INVISIBLE);
                buttonTV.setText("Continue");
                Common.hideKeyboard(SuggestionsActivity.this);
            }
        });
    }

    @Override
    public void onLoadTimeFailed(String Message) {
        hud.dismiss();
        Toast toast = Toast.makeText(SuggestionsActivity.this,""+Message,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();

    }
}