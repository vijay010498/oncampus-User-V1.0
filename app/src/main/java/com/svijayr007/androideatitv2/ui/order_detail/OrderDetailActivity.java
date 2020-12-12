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

package com.svijayr007.androideatitv2.ui.order_detail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.svijayr007.androideatitv2.Adapter.MyCartBillAdapter;
import com.svijayr007.androideatitv2.Common.Common;
import com.svijayr007.androideatitv2.Common.SpacesItemDecoration;
import com.svijayr007.androideatitv2.HomeActivity;
import com.svijayr007.androideatitv2.Model.DeliveryOrderModel;
import com.svijayr007.androideatitv2.Model.OrderModel;
import com.svijayr007.androideatitv2.Model.RefundModel;
import com.svijayr007.androideatitv2.Model.RestaurantModel;
import com.svijayr007.androideatitv2.R;
import com.svijayr007.androideatitv2.ui.orderRating.RatingActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import spencerstudios.com.bungeelib.Bungee;

import static com.svijayr007.androideatitv2.Common.Common.currentDetailOrder;

public class OrderDetailActivity extends AppCompatActivity {

    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;
    private KProgressHUD hud;
    private static DeliveryOrderModel currentDeliveryOrder;
    private FirebaseAnalytics mFirebaseAnalytics;

    //Views

   @BindView(R.id.image_close)
   ImageView image_close;

   @BindView(R.id.title_order_status)
   TextView title_order_status;

   @BindView(R.id.image_refresh)
   ImageView image_refresh;

   @BindView(R.id.image_call)
   ImageView image_call;

   @BindView(R.id.restaurant_image)
   ImageView restaurant_image;

   @BindView(R.id.text_restaurant_name)
   TextView text_restaurant_name;

    @BindView(R.id.text_order_time)
    TextView text_order_time;

    @BindView(R.id.order_otp_text)
    TextView order_otp_text;

    @BindView(R.id.text_order_rating)
    TextView text_order_rating;

    @BindView(R.id.txt_rateOrder)
    TextView txt_rateOrder;

    @BindView(R.id.text_order_id)
    TextView text_order_id;

    @BindView(R.id.order_reference_id)
    TextView order_reference_id;

    @BindView(R.id.recycler_bill_cart_detail)
    RecyclerView recycler_bill_cart_detail;

    private MyCartBillAdapter myCartBillAdapter;



    @BindView(R.id.text_payment_mode)
    TextView text_payment_mode;

    @BindView(R.id.text_total_price)
    TextView text_total_price;

    @BindView(R.id.text_cooking_info)
    TextView text_cooking_info;

    @BindView(R.id.text_delivery_location)
    TextView text_delivery_location;

    //Layouts
    @BindView(R.id.layout_otp)
    LinearLayout layout_otp;

    @BindView(R.id.layout_rating)
    LinearLayout layout_rating;

    @BindView(R.id.txt_cancel_order)
    TextView txt_cancel_order;

    @BindView(R.id.text_food_total)
    TextView text_food_total;

    @BindView(R.id.text_packing_charges)
    TextView text_packing_charges;

    @BindView(R.id.layout_delivery_charge)
    LinearLayout layout_delivery_charge;

    @BindView(R.id.text_delivery_price)
    TextView text_delivery_price;


    //Order_status layout
    //layout 0
    @BindView(R.id.status_0_layout)
    LinearLayout status_0_layout;
    @BindView(R.id.text_status_0)
    TextView text_status_0;
    @BindView(R.id.text_status_0_sup)
    TextView text_status_0_sup;
    @BindView(R.id.status_0_view_circle)
    View status_0_view_circle;


    //layout 1
    @BindView(R.id.status_1_layout)
    LinearLayout status_1_layout;
    @BindView(R.id.top_line_status_1)
    View top_line_status_1;
    @BindView(R.id.bottom_line_status_1)
    View bottom_line_status_1;
    @BindView(R.id.text_status_1)
    TextView text_status_1;
    @BindView(R.id.text_status_1_sup)
    TextView text_status_1_sup;

    //layout 2
    @BindView(R.id.status_2_layout)
    LinearLayout status_2_layout;
    @BindView(R.id.top_line_status_2)
    View top_line_status_2;
    @BindView(R.id.text_status_2)
    TextView text_status_2;
    @BindView(R.id.text_status_2_sup)
    TextView text_status_2_sup;
    @BindView(R.id.status_2_view_circle)
    View status_2_view_circle;

    //layout 3
    @BindView(R.id.status_3_layout)
    LinearLayout status_3_layout;
    @BindView(R.id.top_line_status_3)
    View top_line_status_3;
    @BindView(R.id.text_status_3)
    TextView text_status_3;
    @BindView(R.id.text_status_3_sup)
    TextView text_status_3_sup;
    @BindView(R.id.status_3_view_circle)
    View status_3_view_circle;

    //Delivery Agent Layout
    @BindView(R.id.delivery_agent_layout)
    LinearLayout delivery_agent_layout;
    @BindView(R.id.agent_image)
    CircleImageView agent_image;
    @BindView(R.id.agent_name)
    TextView agent_name;
    @BindView(R.id.call_agent)
    ImageView call_agent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        hud = KProgressHUD.create(OrderDetailActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setCancellable(false)
                .setLabel("Loading")
                .setAnimationSpeed(1)
                .setDimAmount(0.5f).show();
        //init important
        ButterKnife.bind(this);
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        recycler_bill_cart_detail.setHasFixedSize(true);
        recycler_bill_cart_detail.setLayoutManager(new LinearLayoutManager(this));
        recycler_bill_cart_detail.addItemDecoration(new SpacesItemDecoration(5));
        updateTrackOrderUi();
        checkAndHideLayout();
        setUIData();
        setListener();
    }

    private void updateTrackOrderUi() {
        FirebaseDatabase.getInstance(Common.ordersDB)
                .getReference(Common.ORDER_REF)
                .child(currentDetailOrder.getOrderNumber())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            OrderModel liveOrderModel = snapshot.getValue(OrderModel.class);
                            liveOrderModel.setOrderNumber(snapshot.getKey());
                            if(liveOrderModel.getOrderStatus() == 3){
                                if(liveOrderModel.isPickup()){
                                    title_order_status.setText(new StringBuilder()
                                            .append("Picked Up"));

                                }
                                else {
                                    title_order_status.setText(new StringBuilder()
                                            .append("Delivered"));
                                }
                            }else {
                                title_order_status.setText(new StringBuilder()
                                        .append(Common.convertStatusToText(liveOrderModel.getOrderStatus())));
                            }

                            if(!liveOrderModel.isPickup()) {
                                //Delivery Order
                                int status = liveOrderModel.getOrderStatus();
                                if (status == 0) {
                                    //Order just placed
                                } else if (status == 1) {
                                    //order confirmed
                                    status_1_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    top_line_status_1.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    bottom_line_status_1.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    text_status_1.setTextColor(getResources().getColor(R.color.black));
                                    text_status_1_sup.setText("");
                                    text_status_0_sup.setText("");

                                } else if (status == 2) {
                                    //on the way
                                    status_1_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    top_line_status_1.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    bottom_line_status_1.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    text_status_1.setTextColor(getResources().getColor(R.color.black));

                                    status_2_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    top_line_status_2.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    text_status_2.setTextColor(getResources().getColor(R.color.black));

                                    text_status_0.setText(new StringBuilder()
                                            .append(liveOrderModel.getRestaurantName()));
                                    text_status_0_sup.setText("");
                                    text_status_1_sup.setText("");

                                    //Fetch Delivery agent Details with delay
                                    hud.show();
                                    hud.setLabel("Fetching Delivery Agent");
                                    new Thread(new Runnable() {
                                        int seconds = 0;
                                        @Override
                                        public void run() {
                                            while(seconds <=100){
                                                if(seconds == 100){
                                                    //fetch delivery agent details
                                                    FirebaseDatabase.getInstance(Common.deliveryOrdersDB)
                                                            .getReference(Common.DELIVERY_ORDER_REF)
                                                            .child(liveOrderModel.getOrderNumber())
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if(snapshot.exists()){
                                                                        currentDeliveryOrder  = snapshot.getValue(DeliveryOrderModel.class);
                                                                        currentDeliveryOrder.setKey(snapshot.getKey());

                                                                        //init UI
                                                                        if(!TextUtils.isEmpty(currentDeliveryOrder.getDeliveryAgentModel().getImageUrl())){
                                                                            Glide.with(OrderDetailActivity.this)
                                                                                    .load(currentDeliveryOrder.getDeliveryAgentModel().getImageUrl())
                                                                                    .placeholder(R.drawable.ic_restaurant)
                                                                                    .into(agent_image);
                                                                        }
                                                                        agent_name.setText(new StringBuilder()
                                                                                .append(currentDeliveryOrder.getDeliveryAgentModel().getName()));
                                                                        delivery_agent_layout.setVisibility(View.VISIBLE);
                                                                        hud.dismiss();
                                                                    }


                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    hud.dismiss();
                                                                    delivery_agent_layout.setVisibility(View.GONE);
                                                                    Toast.makeText(OrderDetailActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();

                                                                }
                                                            });
                                                }
                                                try {
                                                    Thread.sleep(35);

                                                }catch (InterruptedException e){
                                                    e.printStackTrace();
                                                }
                                                seconds++;
                                            }

                                        }
                                    }).start();
                                } else if (status == 3) {
                                    //Order delivered
                                    status_1_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    top_line_status_1.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    bottom_line_status_1.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    text_status_1.setTextColor(getResources().getColor(R.color.black));

                                    status_2_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    top_line_status_2.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    text_status_2.setTextColor(getResources().getColor(R.color.black));

                                    status_3_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    top_line_status_3.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    text_status_3.setTextColor(getResources().getColor(R.color.black));

                                    text_status_0.setText(new StringBuilder()
                                            .append(liveOrderModel.getRestaurantName()));
                                    text_status_0_sup.setText("");
                                    text_status_1_sup.setText("");

                                    status_1_layout.setVisibility(View.GONE);
                                    status_2_layout.setVisibility(View.GONE);

                                    text_status_3.setText(new StringBuilder()
                                            .append(liveOrderModel.getUserName())
                                            .append(" , ")
                                            .append(liveOrderModel.getShippingAddress()));

                                    text_status_3_sup.setText(new StringBuilder()
                                            .append("Delivered on time"));
                                }
                                else if(status == -1){
                                    text_status_3_sup.setVisibility(View.GONE);
                                    //Cancelled by restaurant
                                    //show refund
                                    status_1_layout.setVisibility(View.GONE);
                                    status_2_layout.setVisibility(View.GONE);
                                    status_3_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    text_status_0_sup.setText("");
                                    text_status_3.setText(new StringBuilder()
                                            .append("Cancelled").append(" , by ")
                                            .append(liveOrderModel.getRestaurantName()));
                                    text_status_3.setTextColor(getResources().getColor(R.color.black));
                                    status_3_view_circle.setBackground(getResources().getDrawable(R.drawable.ic_baseline_cancel_24));
                                    top_line_status_3.setBackgroundColor(getResources().getColor(R.color.instagramColor));


                                    //Refund status with 5 seconds delay
                                    hud.show();
                                    hud.setLabel("Fetching Refund Status");
                                    new Thread(new Runnable() {
                                        int seconds = 0;
                                        @Override
                                        public void run() {
                                            while (seconds <= 50){
                                                if(seconds == 50){
                                                    FirebaseDatabase.getInstance(Common.refundDB)
                                                            .getReference(Common.REFUND_REF)
                                                            .child(currentDetailOrder.getOrderNumber())
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if(snapshot.exists()){
                                                                        hud.dismiss();
                                                                        text_status_3_sup.setVisibility(View.VISIBLE);
                                                                        RefundModel tempRefund = snapshot.getValue(RefundModel.class);
                                                                        text_status_3_sup.setText(new StringBuilder()
                                                                                .append(tempRefund.getStatus())
                                                                                .append(" ₹")
                                                                                .append(tempRefund.getRefundAmount()));

                                                                    }
                                                                    else {
                                                                        hud.dismiss();
                                                                        text_status_3_sup.setText("Refund Processing");
                                                                        text_status_3_sup.setVisibility(View.VISIBLE);
                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    hud.dismiss();
                                                                    Toast.makeText(OrderDetailActivity.this, "REFUND FETCH"+error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    text_status_3_sup.setText("Refund Processing");
                                                                    text_status_3_sup.setVisibility(View.VISIBLE);
                                                                }
                                                            });
                                                }
                                                try {
                                                    Thread.sleep(35);

                                                }catch (InterruptedException e){
                                                    e.printStackTrace();
                                                }
                                                seconds++;
                                            }
                                        }
                                    }).start();


                                }
                                else if(status == -3){
                                    //Cancelled by user
                                    status_1_layout.setVisibility(View.GONE);
                                    status_2_layout.setVisibility(View.GONE);
                                    status_3_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    text_status_0_sup.setText("");
                                    text_status_3.setText(new StringBuilder()
                                            .append("Cancelled , by you"));
                                    text_status_3.setTextColor(getResources().getColor(R.color.black));
                                    status_3_view_circle.setBackground(getResources().getDrawable(R.drawable.ic_baseline_cancel_24));
                                    top_line_status_3.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    text_status_3_sup.setText("");
                                }
                            }
                            else if(liveOrderModel.isPickup()){
                                //Pickup order
                                //initial setup UI
                                text_status_2.setText(new StringBuilder()
                                        .append("Ready for pickup"));
                                text_status_2_sup.setText(new StringBuilder()
                                        .append("Good Food ready for pickup"));
                                text_status_3.setText(new StringBuilder()
                                        .append("Order Picked Up!"));
                                text_status_3_sup.setText(new StringBuilder()
                                        .append("Your order ready for pickup"));
                                status_3_view_circle.setBackground(getResources().getDrawable(R.drawable.ic_location));
                                status_2_view_circle.setBackground(getResources().getDrawable(R.drawable.ic_baseline_directions_walk_24));

                                //Status UI
                                int status = liveOrderModel.getOrderStatus();
                                if (status == 0) {
                                    //Order just placed
                                } else if (status == 1) {
                                    //order confirmed
                                    status_1_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    top_line_status_1.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    bottom_line_status_1.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    text_status_1.setTextColor(getResources().getColor(R.color.black));
                                    text_status_1_sup.setText("");
                                    text_status_0_sup.setText("");

                                } else if (status == -2) {
                                    //Ready for pickup
                                    status_1_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    top_line_status_1.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    bottom_line_status_1.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    text_status_1.setTextColor(getResources().getColor(R.color.black));

                                    status_2_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    top_line_status_2.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    text_status_2.setTextColor(getResources().getColor(R.color.black));

                                    text_status_0.setText(new StringBuilder()
                                            .append(liveOrderModel.getRestaurantName()));
                                    text_status_0_sup.setText("");
                                    text_status_1_sup.setText("");


                                } else if (status == 3) {
                                    //Order delivered
                                    status_1_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    top_line_status_1.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    bottom_line_status_1.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    text_status_1.setTextColor(getResources().getColor(R.color.black));

                                    status_2_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    top_line_status_2.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    text_status_2.setTextColor(getResources().getColor(R.color.black));

                                    status_3_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    top_line_status_3.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    text_status_3.setTextColor(getResources().getColor(R.color.black));

                                    text_status_0.setText(new StringBuilder()
                                            .append(liveOrderModel.getRestaurantName()));
                                    text_status_0_sup.setText("");
                                    text_status_1_sup.setText("");

                                    status_1_layout.setVisibility(View.GONE);
                                    status_2_layout.setVisibility(View.GONE);

                                    text_status_3.setText(new StringBuilder()
                                            .append(liveOrderModel.getUserName()));

                                    text_status_3_sup.setText(new StringBuilder()
                                            .append("Order Picked up!"));
                                }
                                else if(status == -1){
                                    text_status_3_sup.setVisibility(View.GONE);
                                    //Cancelled by restaurant
                                    //show refund
                                    status_1_layout.setVisibility(View.GONE);
                                    status_2_layout.setVisibility(View.GONE);
                                    status_3_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    text_status_0_sup.setText("");
                                    text_status_3.setText(new StringBuilder()
                                            .append("Cancelled").append(" , by ")
                                    .append(liveOrderModel.getRestaurantName()));
                                    text_status_3.setTextColor(getResources().getColor(R.color.black));
                                    status_3_view_circle.setBackground(getResources().getDrawable(R.drawable.ic_baseline_cancel_24));
                                    top_line_status_3.setBackgroundColor(getResources().getColor(R.color.instagramColor));

                                    hud.show();
                                    hud.setLabel("Fetching Refund Status");
                                    //Refund status with 5 seconds delay
                                    new Thread(new Runnable() {
                                        int seconds = 0;
                                        @Override
                                        public void run() {
                                            while(seconds <= 50){
                                                if(seconds == 50){
                                                    FirebaseDatabase.getInstance(Common.refundDB)
                                                            .getReference(Common.REFUND_REF)
                                                            .child(currentDetailOrder.getOrderNumber())
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if(snapshot.exists()){
                                                                        hud.dismiss();
                                                                        text_status_3_sup.setVisibility(View.VISIBLE);
                                                                        RefundModel tempRefund = snapshot.getValue(RefundModel.class);
                                                                        text_status_3_sup.setText(new StringBuilder()
                                                                                .append(tempRefund.getStatus())
                                                                                .append(" ₹")
                                                                                .append(tempRefund.getRefundAmount()));

                                                                    }
                                                                    else {
                                                                        hud.dismiss();
                                                                        text_status_3_sup.setText("Refund Processing");
                                                                        text_status_3_sup.setVisibility(View.VISIBLE);
                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    hud.dismiss();
                                                                    Toast.makeText(OrderDetailActivity.this, "REFUND FETCH"+error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    text_status_3_sup.setText("Refund Processing");
                                                                    text_status_3_sup.setVisibility(View.VISIBLE);
                                                                }
                                                            });
                                                }
                                                try {
                                                    Thread.sleep(35);

                                                }catch (InterruptedException e){
                                                    e.printStackTrace();
                                                }
                                                seconds++;
                                            }
                                        }
                                    }).start();



                                }
                                else if(status == -3){
                                    //Cancelled by user
                                    status_1_layout.setVisibility(View.GONE);
                                    status_2_layout.setVisibility(View.GONE);
                                    status_3_layout.setBackgroundColor(getResources().getColor(R.color.white));
                                    text_status_0_sup.setText("");
                                    text_status_3.setText(new StringBuilder()
                                            .append("Cancelled , by you"));
                                    text_status_3.setTextColor(getResources().getColor(R.color.black));
                                    status_3_view_circle.setBackground(getResources().getDrawable(R.drawable.ic_baseline_cancel_24));
                                    top_line_status_3.setBackgroundColor(getResources().getColor(R.color.instagramColor));
                                    text_status_3_sup.setText("");
                                }
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.i("ERROR",error.getMessage());
                    }
                });




    }

    private void setListener() {
        //Close image
        image_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        image_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Refresh order status only
                //Toast.makeText(OrderDetailActivity.this, "Implement Later", Toast.LENGTH_SHORT).show();
                hud.show();
                FirebaseDatabase.getInstance(Common.ordersDB)
                        .getReference(Common.ORDER_REF)
                        .child(currentDetailOrder.getOrderNumber())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    hud.dismiss();
                                    OrderModel tempModel = snapshot.getValue(OrderModel.class);
                                    //Toast.makeText(OrderDetailActivity.this, ""+tempModel.getOrderStatus(), Toast.LENGTH_SHORT).show();
                                    currentDetailOrder.setOrderStatus(tempModel.getOrderStatus());

                                    //refresh activity without blink
                                    finish();
                                    overridePendingTransition(0,0);
                                    startActivity(getIntent());
                                    overridePendingTransition(0,0);


                                }else {
                                    hud.dismiss();
                                    Toast.makeText(OrderDetailActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                hud.dismiss();
                                Toast.makeText(OrderDetailActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

            }
        });

        image_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(OrderDetailActivity.this)
                        .withPermission(Manifest.permission.CALL_PHONE)
                        .withListener(new PermissionListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                Intent intent = new Intent(Intent.ACTION_CALL);
                                intent.setData(Uri.parse(new StringBuilder("tel:").append(currentDetailOrder.getRestaurantPhone()).toString()));
                                startActivity(intent);
                            }
                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                Toast.makeText(OrderDetailActivity.this, "You must accept this permission to Call user", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();

            }
        });

        //Rate order listener
        txt_rateOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.currentRatingOrder = currentDetailOrder;
                Intent ratingIntent = new Intent(OrderDetailActivity.this, RatingActivity.class);
                startActivity(ratingIntent);
                Bungee.fade(OrderDetailActivity.this);

            }
        });
        txt_cancel_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(OrderDetailActivity.this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog);
                builder.setTitle("Cancel order!");
                builder.setMessage("Do you want to cancel this order");
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hud.show();
                        Map<String, Object> updateStatus = new HashMap<>();
                        updateStatus.put("orderStatus",-3);
                        FirebaseDatabase.getInstance(Common.ordersDB).getReference(Common.ORDER_REF)
                                .child(currentDetailOrder.getOrderNumber())
                                .updateChildren(updateStatus)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            //
                                            hud.dismiss();
                                            Toast.makeText(OrderDetailActivity.this, "Order Cancelled", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                hud.dismiss();
                                Toast.makeText(OrderDetailActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                AlertDialog dialogs = builder.create();
                dialogs.show();
            }
        });

        //Call Delivery Agent
        call_agent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dexter.withContext(OrderDetailActivity.this)
                        .withPermission(Manifest.permission.CALL_PHONE)
                        .withListener(new PermissionListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                Intent intent = new Intent(Intent.ACTION_CALL);
                                intent.setData(Uri.parse(new StringBuilder("tel:").append(currentDeliveryOrder.getDeliveryAgentModel().getPhone()).toString()));
                                startActivity(intent);
                            }
                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                Toast.makeText(OrderDetailActivity.this, "You must accept this permission to Call Delivery Agent's", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();

            }
        });

    }

    private void checkAndHideLayout() {
        //Check order status
        //Cancel order display
        FirebaseDatabase.getInstance(Common.ordersDB)
                .getReference(Common.ORDER_REF)
                .child(currentDetailOrder.getOrderNumber())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            OrderModel tempLiveModel = snapshot.getValue(OrderModel.class);
                            tempLiveModel.setOrderNumber(snapshot.getKey());
                            if(tempLiveModel.getOrderStatus() == 0){
                                txt_cancel_order.setVisibility(View.VISIBLE);
                            }
                            else {
                                txt_cancel_order.setVisibility(View.GONE);
                            }
                            if(tempLiveModel.getOrderStatus() == 3){
                                //Ordered delivered
                                image_refresh.setVisibility(View.GONE);
                                image_call.setVisibility(View.GONE);
                                layout_otp.setVisibility(View.GONE);
                                layout_rating.setVisibility(View.VISIBLE);
                                if(tempLiveModel.getRatingValue() == -1.0 || tempLiveModel.getRatingValue() == -1){
                                    //Check rating value
                                    text_order_rating.setVisibility(View.GONE);
                                    txt_rateOrder.setVisibility(View.VISIBLE);
                                }
                                else {
                                    txt_rateOrder.setVisibility(View.GONE);
                                    text_order_rating.setVisibility(View.VISIBLE);
                                    text_order_rating.setText(new StringBuilder()
                                            .append(currentDetailOrder.getRatingValue()));
                                }
                            }
                            else if(tempLiveModel.getOrderStatus() == -1){
                                //Cancelled
                                layout_otp.setVisibility(View.GONE);
                                image_refresh.setVisibility(View.GONE);
                                image_call.setVisibility(View.GONE);
                            }
                            else if(tempLiveModel.getOrderStatus() == -3){
                                layout_otp.setVisibility(View.GONE);
                                image_refresh.setVisibility(View.GONE);
                                image_call.setVisibility(View.GONE);
                            }
                            else {
                                layout_otp.setVisibility(View.VISIBLE);
                                layout_rating.setVisibility(View.GONE);
                                order_otp_text.setText(new StringBuilder()
                                        .append(tempLiveModel.getOTP()));
                            }

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




    }


    private void setUIData() {
        //Restaurant Image
        FirebaseDatabase.getInstance(Common.restaurantDB)
                .getReference(Common.RESTAURANT_REF)
                .child(currentDetailOrder.getRestaurantId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            RestaurantModel tempModel = snapshot.getValue(RestaurantModel.class);
                            Glide.with(OrderDetailActivity.this)
                                    .load(tempModel.getImageUrl())
                                    .placeholder(R.drawable.ic_restaurant)
                                    .into(restaurant_image);
                        }
                        else {
                            Toast.makeText(OrderDetailActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderDetailActivity.this, ""+ error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



        text_restaurant_name.setText(new StringBuilder()
        .append(currentDetailOrder.getRestaurantName()));

        //Order date
        calendar.setTimeInMillis(currentDetailOrder.getCreateDate());
        Date date = new Date(currentDetailOrder.getCreateDate());

        text_order_time.setText(new StringBuilder()
        .append(Common.getDateOfWeek(calendar.get(Calendar.DAY_OF_WEEK)))
        .append(" ").append(simpleDateFormat.format(date)));

        text_order_id.setText(new StringBuilder("#")
        .append(currentDetailOrder.getOrderNumber()));

        order_reference_id.setText(new StringBuilder("#")
        .append(currentDetailOrder.getTransactionId()));

        myCartBillAdapter = new MyCartBillAdapter(OrderDetailActivity.this,currentDetailOrder.getCartItemList());
        recycler_bill_cart_detail.setAdapter(myCartBillAdapter);



        //order items and price
        /*int numberOfItems = Common.currentDetailOrder.getCartItemList().size();
        order_item_details.setText("");
        order_item_price_details.setText("");
        for(int i=0;i<numberOfItems;i++){

            //Append item and price
            order_item_details.append(new StringBuilder()
            .append(Common.currentDetailOrder.getCartItemList().get(i).getFoodName())
            .append(" X ")
            .append(Common.currentDetailOrder.getCartItemList().get(i).getFoodQuantity()));

            order_item_price_details.append(new StringBuilder("₹")
            .append(Common.currentDetailOrder.getCartItemList().get(i).getFoodPrice() * Common.currentDetailOrder.getCartItemList().get(i).getFoodQuantity()));

            if(i+1 < numberOfItems) {
                order_item_details.append("\n");
                order_item_price_details.append("\n");
            }
        }*/



        text_payment_mode.setText(new StringBuilder("Paid Via ")
        .append(currentDetailOrder.getPaymentMode().toLowerCase()));

        text_total_price.setText(new StringBuilder("₹")
        .append(currentDetailOrder.getTotalPayment()));

        text_food_total.setText(new StringBuilder("₹")
        .append(currentDetailOrder.getOnlyFoodPrice()));

        text_packing_charges.setText(new StringBuilder("₹")
        .append(currentDetailOrder.getPackingCharges()));




        //Cooking info
        if(!TextUtils.isEmpty(currentDetailOrder.getComment())){
            text_cooking_info.setText(new StringBuilder()
            .append(currentDetailOrder.getComment()));
        }
        else {
            text_cooking_info.setText(new StringBuilder()
                    .append("No cooking instructions"));
        }

        //Delivery location
        if(!currentDetailOrder.isPickup()) {
            text_delivery_location.setText(new StringBuilder()
                    .append(currentDetailOrder.getShippingAddress()));
            text_delivery_price.setText(new StringBuilder("₹")
            .append(currentDetailOrder.getDeliveryCharges()));
        }else {
            text_delivery_location.setVisibility(View.GONE);
            layout_delivery_charge.setVisibility(View.GONE);
        }

        hud.dismiss();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent homeIntent = new Intent(OrderDetailActivity.this, HomeActivity.class);
        homeIntent.putExtra("isNavigateViewOrder",true);
        startActivity(homeIntent);
        Bungee.slideRight(OrderDetailActivity.this);
    }
}