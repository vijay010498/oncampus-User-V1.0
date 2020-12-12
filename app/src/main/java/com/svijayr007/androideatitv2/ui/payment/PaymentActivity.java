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

package com.svijayr007.androideatitv2.ui.payment;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.gocashfree.cashfreesdk.CFPaymentService;
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
import com.svijayr007.androideatitv2.Database.CartDataSource;
import com.svijayr007.androideatitv2.Database.CartDatabase;
import com.svijayr007.androideatitv2.Database.LocalCartDataSource;
import com.svijayr007.androideatitv2.EventBus.CounterCartEvent;
import com.svijayr007.androideatitv2.HomeActivity;
import com.svijayr007.androideatitv2.Model.CashFreeVerifySignature;
import com.svijayr007.androideatitv2.Model.OrderModel;
import com.svijayr007.androideatitv2.Model.RestaurantModel;
import com.svijayr007.androideatitv2.Model.SuggestionsModel;
import com.svijayr007.androideatitv2.R;
import com.svijayr007.androideatitv2.Remote.ICloudVerifySignature;
import com.svijayr007.androideatitv2.Remote.RetroFitVerifySignatureClient;
import com.svijayr007.androideatitv2.callback.ILoadTimeFromFirebaseListener;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import spencerstudios.com.bungeelib.Bungee;

public class PaymentActivity extends AppCompatActivity implements ILoadTimeFromFirebaseListener {

    private FirebaseAnalytics mFirebaseAnalytics;
    //from cart activity
    private  Map<String, String> dataSend = new HashMap<>();
    private  Map<String, String> headers = new HashMap<>();
    private  String cfToken = "";
    private String shippingAddress = "";
    private String comment = "";
    private double totalAmount ;
    private double onCampusCommissionAmount;
    private double onCampusCommissionPercentage;
    private double payToRestaurantAfterCommissionAmount;
    private double lat;
    private double lng;
    private String orderNumber;
    private boolean isPickup = false;
    private double deliveryCharges;
    private double packingCharges;
    private double onlyFoodPricel;

    //Restaurant Details
    private  String restaurantId =  null;
    private String restaurantName = null;
    private String restaurantAddress = null;
    private String restaurantPhone = null;


    //PAYMENT SOUND
    private  MediaPlayer paymentSuccess;
    private MediaPlayer paymentFailure;


    private String paymentMode = "";
    private String orderId = "";
    private String txTime = "";
    private String referenceId = "";
    private String type = "";
    private String txMsg = "";
    private String signature = "";
    private String orderAmount = "";
    private String txStatus = "";

    private KProgressHUD hud;

    //Media player for sound



    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CartDataSource cartDataSource;
    ILoadTimeFromFirebaseListener listener;

    private ImageView transaction_status;
    private TextView text_place_order_status;
    private TextView text_Home;


    //verify signature
    private ICloudVerifySignature iCloudVerifySignature;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getArgs();
        iCloudVerifySignature = RetroFitVerifySignatureClient.getInstance().create(ICloudVerifySignature.class);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
        setContentView(R.layout.activity_payment);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        hud = KProgressHUD.create(PaymentActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setCancellable(false)
                .setLabel("Please Wait")
                .setDetailsLabel("Processing Payment")
                .setAnimationSpeed(1)
                .setDimAmount(0.5f);
         paymentSuccess = MediaPlayer.create(this,R.raw.payment_success);
         paymentFailure = MediaPlayer.create(this,R.raw.payment_failure);

        makeCashFreePayment();
        transaction_status = findViewById(R.id.transaction_status);
        text_place_order_status = findViewById(R.id.text_place_order_status);
        text_Home = findViewById(R.id.text_Home);
        listener  = this;

    }


    private void getArgs() {
        Intent intent = getIntent();
        dataSend = (HashMap<String, String>)intent.getSerializableExtra("MAPDATA");
        headers = (HashMap<String, String>)intent.getSerializableExtra("HEADER");
        cfToken = (String) intent.getExtras().get("CFTOKEN");
        shippingAddress = (String) intent.getExtras().get("SHIPPINGADDRESS");
        comment = (String) intent.getExtras().get("COMMENT");
        totalAmount = intent.getExtras().getDouble("TOTALPRICE");
        deliveryCharges = intent.getExtras().getDouble("DELIVERYCHARGES");
        packingCharges = intent.getExtras().getDouble("PACKINGCHARGES");
        onlyFoodPricel = intent.getExtras().getDouble("ONLYFOODPRICE");
        onCampusCommissionAmount = intent.getExtras().getDouble("ONCAMPUSCOMMISSIONAMOUNT");
        onCampusCommissionPercentage = intent.getExtras().getDouble("ONCAMPUSCOMMISSIONPERCENTAGE");
        payToRestaurantAfterCommissionAmount = intent.getExtras().getDouble("PAYTORESTAURANTAFTERCOMISSIONAMOUNT");
        lat = intent.getExtras().getDouble("LAT");
        lng = intent.getExtras().getDouble("LNG");
        orderNumber = (String) intent.getExtras().get("ORDERNUMBER");
        isPickup = intent.getExtras().getBoolean("isPickup",false);
    }

    private void makeCashFreePayment() {
        CFPaymentService.getCFPaymentServiceInstance()
                .doPayment(this,dataSend,cfToken,"TEST","#006400","#FFFFFF");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        hud.show();
        super.onActivityResult(requestCode, resultCode, data);
        //Same request code for all payment APIs.
        Log.d("CODE", "ReqCode : " + CFPaymentService.REQ_CODE);
        Log.d("TAG", "API Response : ");
        //Prints all extras. Replace with app logic.
        if (data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null)
                for (String key : bundle.keySet()) {
                    if(key != null){
                        if(bundle.getString(key) != null) {
                            if (key.equals("paymentMode"))
                                paymentMode = bundle.getString(key);
                            if (key.equals("orderId"))
                                orderId = bundle.getString(key);
                            if (key.equals("txTime"))
                                txTime = bundle.getString(key);
                            if (key.equals("referenceId"))
                                referenceId = bundle.getString(key);
                            if (key.equals("type"))
                                type = bundle.getString(key);
                            if (key.equals("txMsg"))
                                txMsg = bundle.getString(key);
                            if (key.equals("signature"))
                                signature = bundle.getString(key);
                            if (key.equals("orderAmount"))
                                orderAmount = bundle.getString(key);
                            if (key.equals("txStatus"))
                                txStatus = bundle.getString(key);
                        }
                    }
                }
        }
        checkTransaction(txStatus);
    }




    private void checkTransaction(String txStatus) {
        if(txStatus.equals("SUCCESS"))
        {
            //trans success
            transactionSuccess();
        }
        else if(txStatus.equals("FAILED")){
            //trns failed
            hud.dismiss();
            transactionFailed();
        }
        else if(txStatus.equals("CANCELLED")){
            //trns CANCELLED
            hud.dismiss();
            transactionCancelled();
        }
        else if(txStatus.equals("PENDING")){
            //trns PENDING
            hud.dismiss();
        }
        else if(txStatus.equals("FLAGGED")){
            //trns FLAGGED
            hud.dismiss();
        }

    }

    private void transactionCancelled() {
        updateUIToCancelled();

    }
    private void transactionFailed() {
        updateUIToFailed();
    }




    private void transactionSuccess() {

        //Get restaurant Id from cart

        cartDataSource.getLastItemRestaurantId(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(String lastItemRestaurantId) {
                        restaurantId = lastItemRestaurantId;

                        if (restaurantId != null){

                            //Get Restaurant Name
                            FirebaseDatabase.getInstance(Common.restaurantDB).getReference(Common.RESTAURANT_REF)
                                    .child(restaurantId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                RestaurantModel tempResModel = snapshot.getValue(RestaurantModel.class);
                                                restaurantName = tempResModel.getName();
                                                restaurantAddress = tempResModel.getCampus();
                                                restaurantPhone = tempResModel.getPhone();

                                                if(restaurantName != null){
                                                    //Verify signature process

                                                    hud.setDetailsLabel("Verifying Signature");
                                                    //Signature verify server side
                                                    Map<String, String> headers = new HashMap<>();
                                                    headers.put("Authorization", Common.buildToken(Common.authorizeKey));

                                                    //calling verify signature server API
                                                    compositeDisposable.add(iCloudVerifySignature.getSignatureResult(headers,
                                                            orderId, orderAmount, referenceId, txStatus, paymentMode, txMsg, txTime)
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(new Consumer<CashFreeVerifySignature>() {
                                                                @Override
                                                                public void accept(CashFreeVerifySignature cashFreeVerifySignature) throws Exception {
                                                                    //checking signature
                                                                    if (cashFreeVerifySignature.getComputedSignature().equals(signature)) {
                                                                        //signature matches
                                                                        hud.setDetailsLabel("Placing Order");
                                                                        Toast.makeText(PaymentActivity.this, "Signature Verified", Toast.LENGTH_SHORT).show();
                                                                        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
                                                                                .subscribeOn(Schedulers.io())
                                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                                .subscribe(cartItems -> {
                                                                                    //when we have all cart items we will get total price
                                                                                    cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                                                                                            .subscribeOn(Schedulers.io())
                                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                                            .subscribe(new SingleObserver<Double>() {
                                                                                                @Override
                                                                                                public void onSubscribe(Disposable d) {
                                                                                                }

                                                                                                @Override
                                                                                                public void onSuccess(Double totalPrice) {
                                                                                                    OrderModel orderModel = new OrderModel();
                                                                                                    orderModel.setUserId(Common.currentUser.getUid());
                                                                                                    orderModel.setUserName(Common.currentUser.getName());
                                                                                                    orderModel.setUserEmail(Common.currentUser.getEmail());
                                                                                                    orderModel.setUserPhone(Common.currentUser.getPhone());
                                                                                                    orderModel.setShippingAddress(shippingAddress);
                                                                                                    orderModel.setComment(comment);
                                                                                                    orderModel.setTransactionId(referenceId);
                                                                                                    orderModel.setPaymentMode(paymentMode);
                                                                                                    orderModel.setTransactionTime(txTime);
                                                                                                    orderModel.setPaymentMessage(txMsg);
                                                                                                    orderModel.setPaymentSignature(signature);
                                                                                                    orderModel.setTransactionStatus(txStatus);
                                                                                                    orderModel.setLat(lat);
                                                                                                    orderModel.setLng(lng);
                                                                                                    orderModel.setOTP(Common.generateOrderOTP());
                                                                                                    orderModel.setTotalPayment(totalAmount);
                                                                                                    if(!isPickup)
                                                                                                        orderModel.setDeliveryCharges(deliveryCharges);
                                                                                                    orderModel.setPackingCharges(packingCharges);
                                                                                                    orderModel.setOnlyFoodPrice(onlyFoodPricel);
                                                                                                    orderModel.setOnCampusCommissionAmount(onCampusCommissionAmount);
                                                                                                    orderModel.setOnCampusCommissionPercentage(onCampusCommissionPercentage);
                                                                                                    orderModel.setPayToRestaurantAfterCommissionAmount(payToRestaurantAfterCommissionAmount);
                                                                                                    orderModel.setCartItemList(cartItems);
                                                                                                    orderModel.setRestaurantId(restaurantId);
                                                                                                    orderModel.setRestaurantName(restaurantName);
                                                                                                    orderModel.setRestaurantAddress(restaurantAddress);
                                                                                                    orderModel.setRestaurantPhone(restaurantPhone);
                                                                                                    //initial rating -1
                                                                                                    orderModel.setRatingValue(-1.0);
                                                                                                    orderModel.setPickup(isPickup);
                                                                                                    orderModel.setUserToken(Common.userToken);

                                                                                                    //update to firebase
                                                                                                    syncLocalTimeWithGlobaltime(orderModel);

                                                                                                }

                                                                                                @Override
                                                                                                public void onError(Throwable e) {
                                                                                                    hud.dismiss();
                                                                                                    //Toast.makeText(PaymentActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            });
                                                                                }, throwable -> {
                                                                                    hud.dismiss();
                                                                                    Toast.makeText(PaymentActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                }));

                                                                    } else {

                                                                        /*********************************************************/
                                                                        //Signature Failed
                                                                        Bundle bundle = new Bundle();
                                                                        bundle.putString("userId",Common.currentUser.getUid());
                                                                        bundle.putString("userName",Common.currentUser.getUid());
                                                                        bundle.putString("orderAmount",dataSend.get("orderAmount"));
                                                                        bundle.putString("userPhone",Common.currentUser.getPhone());
                                                                        bundle.putString("userEmail",Common.currentUser.getEmail());
                                                                        bundle.putString("orderId",orderId);
                                                                        bundle.putString("referenceId",referenceId);
                                                                        mFirebaseAnalytics.logEvent("payment_signature_failed",bundle);
                                                                        /*********************************************************/



                                                                        hud.dismiss();
                                                                        Toast.makeText(PaymentActivity.this, "Signature verification Failed", Toast.LENGTH_SHORT).show();
                                                                        updateUIToFailed();
                                                                    }
                                                                }
                                                            }, new Consumer<Throwable>() {
                                                                @Override
                                                                public void accept(Throwable throwable) throws Exception {
                                                                    hud.dismiss();
                                                                    Toast.makeText(PaymentActivity.this, "Verification Failed" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    updateUIToFailed();
                                                                }
                                                            }));

                                                }
                                                else {
                                                    hud.dismiss();
                                                    Toast.makeText(PaymentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                                                }

                                            }
                                            else
                                            {
                                                hud.dismiss();
                                                Toast.makeText(PaymentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.i("ERROR",error.getMessage());
                                        }
                                    });

                    }
                        else {
                            hud.dismiss();
                            Toast.makeText(PaymentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                }
                    @Override
                    public void onError(Throwable e) {
                        hud.dismiss();
                        Log.i("ERROR",e.getMessage());
                    }
                });




    }


    private void syncLocalTimeWithGlobaltime(OrderModel orderModel) {
        final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long offset = dataSnapshot.getValue(Long.class);
                long estimatedServerTimeMs = System.currentTimeMillis() + offset ; //offset is missing time b/w local time and server time
                SimpleDateFormat sdf = new SimpleDateFormat("MM dd,yyyy hh:mm a");
                Date resultDate = new Date(estimatedServerTimeMs);
                Log.d("TEST DATE",""+sdf.format(resultDate));

                listener.onLoadTimeSuccess(orderModel,estimatedServerTimeMs);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hud.dismiss();
                listener.onLoadTimeFailed(databaseError.getMessage());
            }
        });

    }
    private void writeOrderToFirebase(OrderModel orderModel) {
        FirebaseDatabase.getInstance(Common.ordersDB)
                .getReference(Common.ORDER_REF)
                .child(orderNumber) //  create orderModel number with only digits
                .setValue(orderModel)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hud.dismiss();
                        Toast.makeText(PaymentActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // orderModel placed successfully clear cart and all process
                cartDataSource.cleanCart(Common.currentUser.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(Integer integer) {
                                Toast.makeText(PaymentActivity.this, "Order placed Successfully!", Toast.LENGTH_SHORT).show();
                                hud.dismiss();
                                updateUIToSuccess();
                            }
                            @Override
                            public void onError(Throwable e) {
                                hud.dismiss();
                                Toast.makeText(PaymentActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }




    private void updateUIToSuccess() {
        paymentSuccess.start();
        paymentSuccess.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                paymentSuccess.release();
            }
        });
        transaction_status.setVisibility(View.VISIBLE);
        text_place_order_status.setVisibility(View.VISIBLE);
        text_Home.setVisibility(View.VISIBLE);

        /********************************************************/
        //Order Placed Log Analytics

        Bundle bundle = new Bundle();
        bundle.putString("userId",Common.currentUser.getUid());
        bundle.putString("userName",Common.currentUser.getUid());
        bundle.putString("orderAmount",dataSend.get("orderAmount"));
        bundle.putString("userPhone",Common.currentUser.getPhone());
        bundle.putString("userEmail",Common.currentUser.getEmail());
        bundle.putString("orderId",orderId);
        bundle.putString("referenceId",referenceId);
        mFirebaseAnalytics.logEvent("order_placed",bundle);

        /********************************************************/


        Glide.with(this).asGif().load(R.drawable.payment_success)
                    .placeholder(R.drawable.homeload)
                    .centerCrop()
                    .into(transaction_status);
        text_place_order_status.setText(new StringBuilder("Order Placed!"));
        text_Home.setText(new StringBuilder(" View Order "));
        text_Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent homeIntent = new Intent(PaymentActivity.this, HomeActivity.class);
                    homeIntent.putExtra("isNavigateViewOrder",true);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);
                    Bungee.fade(PaymentActivity.this);
                    finish();


            }
        });
        EventBus.getDefault().post(new CounterCartEvent(true));


    }

    private void updateUIToCancelled() {
        paymentFailure.start();
        paymentFailure.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                paymentFailure.release();
            }
        });

        transaction_status.setVisibility(View.VISIBLE);
        text_place_order_status.setVisibility(View.VISIBLE);
        text_Home.setVisibility(View.VISIBLE);

        /********************************************************/
        //payment Cancelled Log Analytics

        Bundle bundle = new Bundle();
        bundle.putString("userId",Common.currentUser.getUid());
        bundle.putString("userName",Common.currentUser.getUid());
        bundle.putString("orderAmount",dataSend.get("orderAmount"));
        bundle.putString("userPhone",Common.currentUser.getPhone());
        bundle.putString("userEmail",Common.currentUser.getEmail());
        bundle.putString("orderId",orderId);
        bundle.putString("referenceId",referenceId);
        mFirebaseAnalytics.logEvent("payment_cancelled",bundle);

        /********************************************************/

        Glide.with(this).asGif().load(R.drawable.payment_cancelled)
                .placeholder(R.drawable.homeload)
                .centerCrop()
                .into(transaction_status);
        text_place_order_status.setText(new StringBuilder("Payment Cancelled"));
        text_Home.setText(new StringBuilder("Try again"));
        text_Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                onBackPressed();
            }
        });
        EventBus.getDefault().post(new CounterCartEvent(true));



    }
    private void updateUIToFailed() {
        paymentFailure.start();
        paymentFailure.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                paymentFailure.release();
            }
        });
        transaction_status.setVisibility(View.VISIBLE);
        text_place_order_status.setVisibility(View.VISIBLE);
        text_Home.setVisibility(View.VISIBLE);

        /********************************************************/
        //payment Failed Log Analytics

        Bundle bundle = new Bundle();
        bundle.putString("userId",Common.currentUser.getUid());
        bundle.putString("userName",Common.currentUser.getUid());
        bundle.putString("orderAmount",dataSend.get("orderAmount"));
        bundle.putString("userPhone",Common.currentUser.getPhone());
        bundle.putString("userEmail",Common.currentUser.getEmail());
        bundle.putString("orderId",orderId);
        bundle.putString("referenceId",referenceId);

        mFirebaseAnalytics.logEvent("payment_failed",bundle);

        /********************************************************/
        Glide.with(this).asGif().load(R.drawable.payment_failed)
                .placeholder(R.drawable.homeload)
                .centerCrop()
                .into(transaction_status);
        text_place_order_status.setText(new StringBuilder("Payment Failed!"));
        text_Home.setText(new StringBuilder("Retry"));
        text_Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                onBackPressed();
            }
        });
        EventBus.getDefault().post(new CounterCartEvent(true));
    }


    @Override
    public void onLoadTimeSuccess(OrderModel orderModel, long estimateTimeInMs) {
        orderModel.setCreateDate(estimateTimeInMs);
        orderModel.setOrderStatus(0);
        writeOrderToFirebase(orderModel);

    }



    @Override
    public void onLoadTimeSuccess(SuggestionsModel suggestionsModel, long estimateTimeInMs) {

    }


    @Override
    public void onLoadTimeFailed(String Message) {
        hud.dismiss();
        Toast.makeText(this, ""+Message, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onStop() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();
    }
}