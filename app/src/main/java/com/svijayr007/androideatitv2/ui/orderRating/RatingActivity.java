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

package com.svijayr007.androideatitv2.ui.orderRating;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.svijayr007.androideatitv2.Common.Common;
import com.svijayr007.androideatitv2.HomeActivity;
import com.svijayr007.androideatitv2.Model.RestaurantModel;
import com.svijayr007.androideatitv2.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import spencerstudios.com.bungeelib.Bungee;

public class RatingActivity extends AppCompatActivity {

    @BindView(R.id.backIV)
    ImageView backIV;

    @BindView(R.id.order_rating_restaurant_name)
    TextView order_rating_restaurant_name;

    @BindView(R.id.rating_restaurant_name)
    TextView rating_restaurant_name;

    @BindView(R.id.rating_value)
    TextView rating_value;

    @BindView(R.id.order_rating_bar)
    RatingBar order_rating_bar;

    @BindView(R.id.order_submit_rating)
    TextView order_submit_rating;

    private KProgressHUD hud;
    private FirebaseAnalytics mFirebaseAnalytics;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        ButterKnife.bind(this);
        setData();
        listener();

    }

    private void setData() {
        order_rating_restaurant_name.setText(Common.currentRatingOrder.getRestaurantName());
        rating_restaurant_name.setText(Common.currentRatingOrder.getRestaurantName());

        hud = KProgressHUD.create(RatingActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setCancellable(false)
                .setLabel("Please Wait")
                .setAnimationSpeed(1)
                .setDimAmount(0.5f);


    }

    private void listener() {

        //Back image Clicked
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //update rating bar value
        order_rating_bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rating_value.setText(new StringBuilder(String.valueOf(rating)));

            }
        });

        //Submit rating button clicked
        order_submit_rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hud.show();
                //Run rating algorithm and submit to firebase
                Map<String, Object> ratingUpdate = new HashMap<>();
                ratingUpdate.put("ratingValue",order_rating_bar.getRating());
                //First submit rating to order database
                FirebaseDatabase.getInstance(Common.ordersDB)
                        .getReference(Common.ORDER_REF)
                        .child(Common.currentRatingOrder.getOrderNumber())
                        .updateChildren(ratingUpdate)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                   //Only order rating
                                    restaurantRatingAlgorithm();

                                }
                                else {
                                    hud.dismiss();
                                    Toast.makeText(RatingActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hud.dismiss();
                        Toast.makeText(RatingActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

    }

    private void restaurantRatingAlgorithm() {
        //Get current rating value
        FirebaseDatabase.getInstance(Common.restaurantDB)
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentRatingOrder.getRestaurantId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){

                            //Rating algorithm
                            RestaurantModel tempResModel = snapshot.getValue(RestaurantModel.class);
                            double oldRatingTotal = tempResModel.getRating() * tempResModel.getRatingCount();
                            long ratingCount = tempResModel.getRatingCount()+1;
                            double ratingResult = (oldRatingTotal + order_rating_bar.getRating())/ratingCount;
                            BigDecimal bigDecimal = new BigDecimal(ratingResult).setScale(1, RoundingMode.HALF_DOWN);
                            double ratingResultRound = bigDecimal.doubleValue();




                            //Submit rating to restaurant
                            Map<String, Object> resRatingUpdate = new HashMap<>();
                            resRatingUpdate.put("rating", ratingResultRound);
                            resRatingUpdate.put("ratingCount",ratingCount);

                            submitRestaurantRating(resRatingUpdate,snapshot);
                        }else {
                            hud.dismiss();
                            Toast.makeText(RatingActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        hud.dismiss();
                        Toast.makeText(RatingActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void submitRestaurantRating(Map<String, Object> resRatingUpdate, DataSnapshot snapshot) {
        snapshot.getRef()
                .updateChildren(resRatingUpdate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //All done update UI
                            hud.dismiss();
                            Common.currentRatingOrder = null;
                            Intent homeIntent = new Intent(RatingActivity.this, HomeActivity.class);
                            homeIntent.putExtra("isNavigateViewOrder",true);
                            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(homeIntent);
                            Bungee.slideRight(RatingActivity.this);
                            finish();

                        }
                        else{
                            hud.dismiss();
                            Toast.makeText(RatingActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hud.dismiss();
                Toast.makeText(RatingActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Bungee.slideRight(RatingActivity.this);
    }

}