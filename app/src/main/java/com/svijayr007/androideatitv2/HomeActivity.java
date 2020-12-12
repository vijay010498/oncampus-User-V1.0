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

package com.svijayr007.androideatitv2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.svijayr007.androideatitv2.Common.Common;
import com.svijayr007.androideatitv2.Database.CartDataSource;
import com.svijayr007.androideatitv2.Database.CartDatabase;
import com.svijayr007.androideatitv2.Database.LocalCartDataSource;
import com.svijayr007.androideatitv2.EventBus.CounterCartEvent;
import com.svijayr007.androideatitv2.EventBus.HideBottomNavigation;
import com.svijayr007.androideatitv2.EventBus.NavigateExploreEvent;
import com.svijayr007.androideatitv2.ui.block_screen.BlockScreenActivity;
import com.svijayr007.androideatitv2.ui.cart.CartFragment;
import com.svijayr007.androideatitv2.ui.no_internet.NoInternetActivity;
import com.svijayr007.androideatitv2.ui.restaurant.RestaurantFragment;
import com.svijayr007.androideatitv2.ui.settings.SettingsActivity;
import com.svijayr007.androideatitv2.ui.view_orders.ViewOrdersFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import io.customerly.Callback;
import io.customerly.Customerly;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import spencerstudios.com.bungeelib.Bungee;

public class HomeActivity extends AppCompatActivity  {

    private   BottomNavigationView bottomNavigationView;
    private  BadgeDrawable badge;

    private CartDataSource cartDataSource;
    private KProgressHUD hud;
    private boolean isNavigateViewOrder = false;
    private boolean isNavigateCartFragment = false;
    private FirebaseAnalytics mFirebaseAnalytics;




    @Override
    protected void onResume() {
        super.onResume();
        //countCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            if (Common.blockScreenModel!=null) {
                if(Common.blockScreenModel.isOverall()) {
                    startActivity(new Intent(getApplicationContext(), BlockScreenActivity.class));
                    Bungee.fade(HomeActivity.this);
                }
            }
                setContentView(R.layout.activity_home);
                loginCustomerly();
                bottomNavigationView = findViewById(R.id.bottom_navigation);
                //Badge
                Menu menu = bottomNavigationView.getMenu();
                MenuItem item = menu.findItem(R.id.nav_cart);
                badge = bottomNavigationView.getOrCreateBadge(item.getItemId());

                getArgs();


                // Obtain the FirebaseAnalytics instance.
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

                //Bottom navigation default restaurant
                loadFragment(new RestaurantFragment());


                bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment fragment = null;
                        switch (item.getItemId()){
                            case R.id.nav_view_orders:
                                if(bottomNavigationView.getSelectedItemId()!= R.id.nav_view_orders)
                                    fragment = new ViewOrdersFragment();
                                break;

                            case R.id.nav_settings:
                                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                                Bungee.fade(HomeActivity.this);
                                break;


                            case R.id.nav_restaurant: //explore
                                if(bottomNavigationView.getSelectedItemId()!= R.id.nav_restaurant)
                                    fragment = new RestaurantFragment();
                                break;

                            case R.id.nav_cart:
                                if(bottomNavigationView.getSelectedItemId()!= R.id.nav_cart)
                                    fragment = new CartFragment();
                                break;

                        }
                       return loadFragment(fragment);
                    }
                });

                hud = KProgressHUD.create(HomeActivity.this)
                        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                        .setCancellable(true)
                        .setLabel("Loading")
                        .setAnimationSpeed(2)
                        .setDimAmount(0.5f);

                ButterKnife.bind(this);
                cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

                //FCM TOPICS
                subscribeToGlobalTopic(Common.globalTopic);
                subscribeToGlobalClientTopic(Common.globalClientTopic);
                sbscribeToClient_campusTopic(new StringBuilder()
                .append("client")
                .append("_")
                .append(Common.currentUser.getCampusId()).toString());


                countCartItem();
                //Check is view Order navigation
                if(isNavigateViewOrder){
                   loadFragment(new ViewOrdersFragment());
                   bottomNavigationView.setSelectedItemId(R.id.nav_view_orders);
                }
                //Check if cart navigation
                if(isNavigateCartFragment){
                    loadFragment(new CartFragment());
                    bottomNavigationView.setSelectedItemId(R.id.nav_cart);
                }



    }

    private void loginCustomerly() {
        Customerly.registerUser(
                Common.currentUser.getEmail(),
                Common.currentUser.getUid(),
                Common.currentUser.getName(), null, null,
                new Callback() {
                    @Override
                    public Unit invoke() {
                        //task completed successfully
                        Log.i("Customerly","User Registered");
                        return null;
                    }
                },
                new Callback() {
                    @Override
                    public Unit invoke() {
                        Log.i("Customerly","User Register Failed");
                        return null;
                    }
                }
        );

    }


    private boolean loadFragment(Fragment fragment) {
        if(fragment != null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            return  true;
        }
        return false;

    }


    private void getArgs() {
        isNavigateViewOrder = getIntent().getBooleanExtra("isNavigateViewOrder",false);
        isNavigateCartFragment = getIntent().getBooleanExtra("isNavigateCartFragment",false);
    }

    //FCM
    /*****************************************************************/

    private void subscribeToGlobalClientTopic(String globalClientTopic) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(globalClientTopic)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("FCM","Subscribed to global client topic");


            }
        });
    }

    private void subscribeToGlobalTopic  (String globalTopic) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(globalTopic)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("FCM","Subscribed to global  topic");
            }
        });

    }
    private void sbscribeToClient_campusTopic(String campusTopic) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(campusTopic)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, "Campus Topic"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("FCM ","Subscriber to Client Campus Topic");
            }
        });
    }



    //FCM
    /*****************************************************************/



    //EventBuses
    @Override
    protected void onStart() {
        if(!Common.isInternetAvailable(getApplicationContext())) {
            super.onStart();
            EventBus.getDefault().register(this);
        }
        else {
            startActivity(new Intent(getApplicationContext(), NoInternetActivity.class));
            finish();
            Bungee.fade(getApplicationContext());
        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();

    }




    /********************************************************************************************/
    //Count cart EVENT
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEvent event)
    {
        if(event.isSuccess()){
            countCartItem();
        }
    }

    /********************************************************************************************/

    //Explore navigate EVENT
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExploreEvent(NavigateExploreEvent event){
        if(event.isNavigateExplore()){
            bottomNavigationView.setSelectedItemId(R.id.nav_restaurant);
            loadFragment(new RestaurantFragment());
        }
    }

    /********************************************************************************************/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHideBottomNavigation(HideBottomNavigation event){
        if(event.isHide()){
            bottomNavigationView.setVisibility(View.INVISIBLE);
        }
        else if(!event.isHide()){
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
        else
            bottomNavigationView.setVisibility(View.VISIBLE);
    }


    /********************************************************************************************/

    //Count cart function
    private void countCartItem() {
        if(Common.currentUser!=null) {
            cartDataSource.countItemInCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            badge.setVisible(true);
                            badge.setNumber(integer);

                        }

                        @Override
                        public void onError(Throwable e) {
                            if (!e.getMessage().contains("Query returned empty")) {
                                //Toast.makeText(HomeActivity.this, "[COUNT CART]"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                badge.setNumber(0);
                                badge.setVisible(false);
                            }


                        }
                    });
        }
    }

    /********************************************************************************************/

    //Back Pressed

    @Override
    public void onBackPressed() {
        MenuItem homeItem = bottomNavigationView.getMenu().getItem(0);
        if(bottomNavigationView.getSelectedItemId() != homeItem.getItemId()){
            loadFragment(new RestaurantFragment());

            bottomNavigationView.setSelectedItemId(homeItem.getItemId());
        }
        else {
            super.onBackPressed();
        }
    }
}
