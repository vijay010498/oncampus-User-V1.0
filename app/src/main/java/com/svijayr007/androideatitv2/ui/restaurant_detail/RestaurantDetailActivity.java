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

package com.svijayr007.androideatitv2.ui.restaurant_detail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.robertlevonyan.views.customfloatingactionbutton.FloatingActionButton;
import com.svijayr007.androideatitv2.Adapter.MyCategoriesAdapter;
import com.svijayr007.androideatitv2.Adapter.MyFabMenuAdapter;
import com.svijayr007.androideatitv2.Common.Common;
import com.svijayr007.androideatitv2.Common.SpacesItemDecoration;
import com.svijayr007.androideatitv2.Database.CartDataSource;
import com.svijayr007.androideatitv2.Database.CartDatabase;
import com.svijayr007.androideatitv2.Database.LocalCartDataSource;
import com.svijayr007.androideatitv2.EventBus.ResMenuScrollEvent;
import com.svijayr007.androideatitv2.EventBus.ShowViewCartBottomSheet;
import com.svijayr007.androideatitv2.HomeActivity;
import com.svijayr007.androideatitv2.Model.CategoryModel;
import com.svijayr007.androideatitv2.Model.RestaurantModel;
import com.svijayr007.androideatitv2.R;
import com.svijayr007.androideatitv2.sharedPreference.SharedPref;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import spencerstudios.com.bungeelib.Bungee;

public class RestaurantDetailActivity extends AppCompatActivity {

    private KProgressHUD hud;
    private RestaurantModel currRestaurant = Common.currentRestaurant;
    private MenuViewModel menuViewModel;
    private  AlertDialog dialogMenuFab;
    private BottomSheetBehavior bottomSheetBehavior;

    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private FirebaseAnalytics mFirebaseAnalytics;


    //Views
    @BindView(R.id.image_close)
    ImageView image_close;
    @BindView(R.id.res_title_name)
    TextView res_title_name;
    @BindView(R.id.image_refresh)
    ImageView image_refresh;
    @BindView(R.id.image_call)
    ImageView image_call;
    @BindView(R.id.image_chat)
    ImageView image_chat;
    @BindView(R.id.restaurant_image)
    ImageView restaurant_image;
    @BindView(R.id.text_restaurant_name)
    TextView text_restaurant_name;
    @BindView(R.id.text_res_food_category)
    TextView text_res_food_category;
    @BindView(R.id.text_res_address)
    TextView text_res_address;
    @BindView(R.id.restaurant_rating)
    TextView restaurant_rating;
    @BindView(R.id.restaurant_prep_time)
    TextView restaurant_prep_time;
    @BindView(R.id.restaurant_price_for_2)
    TextView restaurant_price_for_2;
    @BindView(R.id.restaurant_rating_count)
    TextView restaurant_rating_count;

    @BindView(R.id.menu_fab)
    FloatingActionButton menu_fab;


    //Veg Switch
    @BindView(R.id.switch_veg)
    SwitchMaterial switch_veg;

    @BindView(R.id.bottom_sheet_layout_cart)
    LinearLayout bottom_sheet_layout_cart;
    @BindView(R.id.view_cart_layout)
    LinearLayout view_cart_layout;
    @BindView(R.id.cart_no_of_items_price)
    TextView cart_no_of_items_price;

    LayoutAnimationController layoutAnimationController;


    //Restaurant menu
    @BindView(R.id.recycler_menu_res)
    RecyclerView recycler_menu_res;
    MyCategoriesAdapter categoriesAdapter;

    @BindView(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //Hud before open
        hud = KProgressHUD.create(RestaurantDetailActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setCancellable(false)
                .setLabel("Loading")
                .setAnimationSpeed(1)
                .setDimAmount(0.5f).show();

        //init important
        ButterKnife.bind(this);
        menuViewModel =
                ViewModelProviders.of(this).get(MenuViewModel.class);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(RestaurantDetailActivity.this,R.anim.layout_item_fade_in);
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_layout_cart);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        setUIData();
        setListener();
        checkCartItems();

    }



    private void setUIData() {

        //Veg Switch
        switch_veg.setChecked(SharedPref.getBoolean(RestaurantDetailActivity.this,"isVegOnly"));
        Glide.with(getApplicationContext())
                .load(currRestaurant.getImageUrl())
                .placeholder(R.drawable.ic_restaurant)
                .into(restaurant_image);
        res_title_name.setText(new StringBuilder()
            .append(currRestaurant.getName())
            .append(" "));

        text_restaurant_name.setText(new StringBuilder()
            .append(currRestaurant.getName()));

        text_res_food_category.setText(new StringBuilder()
            .append(currRestaurant.getFoodCategories()));

        text_res_address.setText(new StringBuilder()
            .append(currRestaurant.getCampus()));

        restaurant_rating.setText(new StringBuilder()
            .append(currRestaurant.getRating()));

        restaurant_prep_time.setText(new StringBuilder()
            .append(currRestaurant.getPrepTime())
            .append(" ")
            .append("mins"));

        restaurant_price_for_2.setText(new StringBuilder("₹")
            .append(currRestaurant.getPriceForTwoPeople()));

        restaurant_rating_count.setText(new StringBuilder()
            .append(currRestaurant.getRatingCount())
            .append("+ ")
            .append("ratings"));
        recycler_menu_res.setHasFixedSize(true);
        recycler_menu_res.setNestedScrollingEnabled(true);
        recycler_menu_res.addItemDecoration(new SpacesItemDecoration(8));

        //Rating animation
        Animation animation = AnimationUtils.loadAnimation(RestaurantDetailActivity.this,R.anim.fade_in_repeat);
        animation.setDuration(2000);
        restaurant_rating_count.startAnimation(animation);


        //set restaurant menu
        menuViewModel.getCategoryListMutable(currRestaurant.getId(),SharedPref.getBoolean(RestaurantDetailActivity.this,"isVegOnly")).observe(this, categoryModels -> {
            //adapter
            categoriesAdapter = new MyCategoriesAdapter(RestaurantDetailActivity.this,categoryModels);
            recycler_menu_res.setAdapter(categoriesAdapter);
            recycler_menu_res.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        });
        hud.dismiss();

    }

    private void setListener() {

        //Veg Only switch
        switch_veg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    SharedPref.putBoolean(RestaurantDetailActivity.this,"isVegOnly",isChecked);
                    finish();
                    overridePendingTransition(0,0);
                    startActivity(getIntent());
                    overridePendingTransition(0,0);

            }
        });
        image_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        image_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0,0);
                startActivity(getIntent());
                overridePendingTransition(0,0);

            }
        });
        image_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(RestaurantDetailActivity.this)
                        .withPermission(Manifest.permission.CALL_PHONE)
                        .withListener(new PermissionListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                Intent intent = new Intent(Intent.ACTION_CALL);
                                intent.setData(Uri.parse(new StringBuilder("tel:").append(currRestaurant.getPhone()).toString()));
                                startActivity(intent);
                            }
                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                Toast.makeText(RestaurantDetailActivity.this, "You must accept this permission to Call user", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
            }
        });
        image_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RestaurantDetailActivity.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
            }
        });

        menu_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RestaurantDetailActivity.this,android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth);
                builder.setTitle(new StringBuilder()
                .append(currRestaurant.getName())
                .append("'s")
                .append(" Menu"));
                View view = LayoutInflater.from(RestaurantDetailActivity.this).inflate(R.layout.layout_fab_res_menu,null);
                RecyclerView recycler_menu_fab = view.findViewById(R.id.recycler_menu_fab);
                menuViewModel.getCategoryListMutable(currRestaurant.getId(),SharedPref.getBoolean(RestaurantDetailActivity.this,"isVegOnly")).observe(RestaurantDetailActivity.this, new Observer<List<CategoryModel>>() {
                    @Override
                    public void onChanged(List<CategoryModel> categoryModels) {
                        MyFabMenuAdapter fabMenuAdapter = new MyFabMenuAdapter(RestaurantDetailActivity.this,categoryModels);
                        recycler_menu_fab.setAdapter(fabMenuAdapter);
                        recycler_menu_fab.setLayoutManager(new LinearLayoutManager(RestaurantDetailActivity.this));
                        recycler_menu_fab.setVerticalScrollBarEnabled(true);
                        recycler_menu_fab.addItemDecoration(new DividerItemDecoration(recycler_menu_fab.getContext(), DividerItemDecoration.VERTICAL));
                    }
                });
                builder.setView(view);
                dialogMenuFab = builder.create();
                dialogMenuFab.show();

            }
        });

        //VIew cart listener
        view_cart_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Navigate to home then -> to cart fragment
                finish();
                Intent homeToCartIntent = new Intent(RestaurantDetailActivity.this, HomeActivity.class);
                homeToCartIntent.putExtra("isNavigateCartFragment",true);
                startActivity(homeToCartIntent);

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Bungee.slideRight(RestaurantDetailActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    //Menu fab scroll
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMenuFabClicked(ResMenuScrollEvent event)
    {
        dialogMenuFab.dismiss();
        //write code to scroll to cat menu position
       recycler_menu_res.post(new Runnable() {
           @Override
           public void run() {
               float y = recycler_menu_res.getY() + recycler_menu_res.getChildAt(event.getPos()).getY();
               nestedScrollView.smoothScrollTo(0, (int) y,500);
               recycler_menu_res.getChildAt(event.getPos()).setBackgroundColor(getResources().getColor(R.color.quantum_orange));
               new Timer().schedule(new TimerTask() {
                   @Override
                   public void run() {
                       recycler_menu_res.getChildAt(event.getPos()).setBackgroundColor(getResources().getColor(R.color.white));

                   }
               },1000);
           }
       });

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShowCartBottomSheet(ShowViewCartBottomSheet event){
        if(event.isCount()){
            checkCartItems();
        }
    }

    private void checkCartItems() {
        compositeDisposable.add(cartDataSource.countItemInCart(Common.currentUser.getUid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(integer -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                compositeDisposable.add(cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aDouble -> {
                    cart_no_of_items_price.setText(new StringBuilder()
                    .append(integer)
                    .append(" items")
                    .append(" | ")
                    .append("₹")
                    .append(aDouble)
                    .append(" "));
                },throwable -> {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    Toast.makeText(this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
        },throwable -> {
            if(throwable.getMessage().contains("empty")){
                //Cart empty
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }

        }));

    }

}