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

package com.svijayr007.androideatitv2.ui.cart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.svijayr007.androideatitv2.Adapter.MyCartAdapter;
import com.svijayr007.androideatitv2.Adapter.MyCartBillAdapter;
import com.svijayr007.androideatitv2.Common.Common;
import com.svijayr007.androideatitv2.Common.SpacesItemDecoration;
import com.svijayr007.androideatitv2.Database.CartDataSource;
import com.svijayr007.androideatitv2.Database.CartDatabase;
import com.svijayr007.androideatitv2.Database.CartItem;
import com.svijayr007.androideatitv2.Database.LocalCartDataSource;
import com.svijayr007.androideatitv2.EventBus.CounterCartEvent;
import com.svijayr007.androideatitv2.EventBus.DeleteCartItemQuantityZeroNotify;
import com.svijayr007.androideatitv2.EventBus.HideBottomNavigation;
import com.svijayr007.androideatitv2.EventBus.NavigateExploreEvent;
import com.svijayr007.androideatitv2.EventBus.UpdateItemInCart;
import com.svijayr007.androideatitv2.Model.CashFreeToken;
import com.svijayr007.androideatitv2.Model.CategoryModel;
import com.svijayr007.androideatitv2.Model.PercentagePackingDeliveryModel;
import com.svijayr007.androideatitv2.Model.RestaurantModel;
import com.svijayr007.androideatitv2.R;
import com.svijayr007.androideatitv2.Remote.ICloudFunctions;
import com.svijayr007.androideatitv2.Remote.RetroFitCloudClient;
import com.svijayr007.androideatitv2.callback.ICategoryCallbackListener;
import com.svijayr007.androideatitv2.ui.payment.PaymentActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment implements ICategoryCallbackListener {


    private FirebaseAnalytics mFirebaseAnalytics;

    //Restaurant Id to display cart restaurant
    private  String restaurantId ;
    private String restaurantOpenStatus="";
    private boolean anyItemNotAvailable = false;

    //Amount
    private static double deliveryCharges_SEND_TO_PAYMENT_INTENT;
    private static double packingCharges_SEND_TO_PAYMENT_INTENT;
    private static double onlyFoodPrice_SEND_TO_PAYMENT_INTENT;
    private static double totalPrice;
    private static double onCampusPercentageAmount;
    private static  double onCampusPercentage ;
    private static double payToRestaurantAfterCommissionAmount;
    private String  orderAmount;
    private static  String orderNumber ;



    private static double packingCharges ;
    private static double deliveryCharges;


    private static final String appId = "1769533d14ef0247f0de4922f59671";
    private static final String orderCurrency = "INR";
    private boolean isPickup;

    private ICloudFunctions cloudFunctions;
    private KProgressHUD hud;
    private KProgressHUD hud1; //Restaurant cart loading


    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;

    private String address, comment="";



    private  MyCartAdapter adapter;
    private MyCartBillAdapter myCartBillAdapter;
    private Unbinder unbinder;
    private CartViewModel cartViewModel;

    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.group_place_holder)
    CardView  group_place_holder;
    @BindView(R.id.img_empty_cart)
    ImageView img_empty_cart;
    @BindView(R.id.cart_restaurant_layout)
    LinearLayout cart_restaurant_layout;


    @BindView(R.id.cart_restaurant_image)
    ImageView cart_restaurant_image;
    @BindView(R.id.cart_restaurant_name)
    TextView cart_restaurant_name;

    @BindView(R.id.root_layout)
    CoordinatorLayout root_layout;



    @BindView(R.id.text_instruction)
    EditText text_instruction;
    @BindView(R.id.text_delivery_location)
    EditText text_delivery_location;

    @BindView(R.id.proceed_pay_layout)
    LinearLayout proceed_pay_layout;
    @BindView(R.id.total_price_proceed_layout)
    LinearLayout total_price_proceed_layout;
    @BindView(R.id.proceed_to_pay_txt)
    TextView proceed_to_pay_txt;
    @BindView(R.id.delivery_type_txt)
    TextView delivery_type_txt;

    @BindView(R.id.dot_view_1)
    View dot_view_1;
    @BindView(R.id.dot_view_2)
    View dot_view_2;
    @BindView(R.id.dot_view_3)
    View dot_view_3;
    @BindView(R.id.order_type_layout)
    LinearLayout order_type_layout;

    @BindView(R.id.text_browse_restaurants)
    TextView text_browse_restaurants;
    @BindView(R.id.nested_scrollView)
    NestedScrollView nested_scrollView;

    @BindView(R.id.radio_pickup)
    RadioButton radio_pickup;
    @BindView(R.id.radio_delivery)
    RadioButton radio_delivery;

    //Bill details
   @BindView(R.id.recycler_bill_cart)
   RecyclerView recycler_bill_cart;

    @BindView(R.id.text_cart_total)
    TextView text_cart_total;

    //For cart item availability check
    private List<CartItem> cartItemListTemp = null;
    private ICategoryCallbackListener categoryCallbackListener;


    //Delivery and Packing charges
    @BindView(R.id.packing_charges_layout)
    LinearLayout packing_charges_layout;
    @BindView(R.id.text_packing_charges)
    TextView text_packing_charges;

    @BindView(R.id.delivery_charges_layout)
    LinearLayout delivery_charges_layout;
    @BindView(R.id.text_delivery_charges)
    TextView text_delivery_charges;

    @BindView(R.id.text_cart_total_including_delivery_packing)
    TextView text_cart_total_including_delivery_packing;




    private void  makeCashFreePaymentRequest() {
        hud.show();
        orderNumber = Common.createOrderNumber();
        final Map<String, String> dataSend = new HashMap<>();
        dataSend.put("appId",appId);
        dataSend.put("orderId", orderNumber);
        dataSend.put("orderAmount", orderAmount);
        dataSend.put("orderCurrency",orderCurrency);
        dataSend.put("customerPhone", Common.currentUser.getPhone());
        dataSend.put("customerEmail",Common.currentUser.getEmail());
        dataSend.put("customerName",Common.currentUser.getName());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildToken(Common.authorizeKey));

        compositeDisposable.add(cloudFunctions.getToken(headers,
                orderNumber,orderAmount)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<CashFreeToken>() {
            @Override
            public void accept(CashFreeToken cashFreeToken) throws Exception {
                if(cashFreeToken.getStatus().equals("OK")){
                    sendToPaymentActivity(cashFreeToken.getCftoken(),dataSend,headers);
                }
                else {
                    hud.dismiss();
                    Toast.makeText(getContext(), ""+cashFreeToken.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                hud.dismiss();
                Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));


    }

    private void sendToPaymentActivity(String cftoken, Map<String, String> dataSend, Map<String, String> headers) {
        Intent paymentIntent = new Intent(getContext(), PaymentActivity.class);
        paymentIntent.putExtra("MAPDATA", (Serializable) dataSend);
        paymentIntent.putExtra("HEADER", (Serializable) headers);
        paymentIntent.putExtra("CFTOKEN",cftoken);
        paymentIntent.putExtra("SHIPPINGADDRESS",address);
        paymentIntent.putExtra("COMMENT",comment);
        paymentIntent.putExtra("TOTALPRICE",totalPrice);
        paymentIntent.putExtra("DELIVERYCHARGES",deliveryCharges_SEND_TO_PAYMENT_INTENT);
        paymentIntent.putExtra("PACKINGCHARGES",packingCharges_SEND_TO_PAYMENT_INTENT);
        paymentIntent.putExtra("ONLYFOODPRICE",onlyFoodPrice_SEND_TO_PAYMENT_INTENT);
        paymentIntent.putExtra("ONCAMPUSCOMMISSIONAMOUNT",onCampusPercentageAmount);
        paymentIntent.putExtra("ONCAMPUSCOMMISSIONPERCENTAGE",onCampusPercentage);
        Log.i("FINAL PERCENTAGE AMOUNT", String.valueOf(onCampusPercentageAmount));
        Log.i("FINAL PERCENTAGE", String.valueOf(onCampusPercentage));
        paymentIntent.putExtra("PAYTORESTAURANTAFTERCOMISSIONAMOUNT",payToRestaurantAfterCommissionAmount);
        paymentIntent.putExtra("isPickup",isPickup);

        if(currentLocation != null){
            paymentIntent.putExtra("LAT",currentLocation.getLatitude());
            paymentIntent.putExtra("LNG",currentLocation.getLongitude());
        }
        else {
            paymentIntent.putExtra("LAT",-0.1f);
            paymentIntent.putExtra("LNG",-0.1f);
        }

        paymentIntent.putExtra("ORDERNUMBER",orderNumber);
        hud.dismiss();
        startActivity(paymentIntent);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setCancellable(false)
                .setLabel("Please Wait")
                .setDetailsLabel("Initiating Payment")
                .setAnimationSpeed(1)
                .setDimAmount(0.5f);

        hud1 = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f);


       cartViewModel = ViewModelProviders.of(this).get(CartViewModel.class);
       cloudFunctions = RetroFitCloudClient.getInstance().create(ICloudFunctions.class);
        View root = inflater.inflate(R.layout.fragment_cart, container,false);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

       cartViewModel.initCartDataSource(getContext());
       cartViewModel.getMutableLiveDataCartItems().observe(getViewLifecycleOwner(), new Observer<List<CartItem>>() {
           @Override
           public void onChanged(List<CartItem> cartItems) {
               cartItemListTemp = cartItems;
               if( cartItems == null || cartItems.isEmpty() )
               {
                   recycler_cart.setVisibility(View.GONE);
                   group_place_holder.setVisibility(View.GONE);
                   img_empty_cart.setVisibility(View.VISIBLE);
                   cart_restaurant_layout.setVisibility(View.GONE);
                   text_instruction.setVisibility(View.GONE);
                   text_delivery_location.setVisibility(View.GONE);
                   dot_view_1.setVisibility(View.GONE);
                   dot_view_2.setVisibility(View.GONE);
                   dot_view_3.setVisibility(View.GONE);
                   order_type_layout.setVisibility(View.GONE);
                   delivery_type_txt.setVisibility(View.GONE);
                   text_browse_restaurants.setVisibility(View.VISIBLE);
                   nested_scrollView.setVisibility(View.GONE);
                   hud1.dismiss();
               }else {
                   recycler_cart.setVisibility(View.VISIBLE);
                   group_place_holder.setVisibility(View.VISIBLE);
                   nested_scrollView.setVisibility(View.VISIBLE);
                   img_empty_cart.setVisibility(View.GONE);
                   text_browse_restaurants.setVisibility(View.GONE);
                   cart_restaurant_layout.setVisibility(View.VISIBLE);
                   text_instruction.setVisibility(View.VISIBLE);
                   dot_view_1.setVisibility(View.VISIBLE);
                   dot_view_2.setVisibility(View.VISIBLE);
                   dot_view_3.setVisibility(View.VISIBLE);
                   order_type_layout.setVisibility(View.VISIBLE);
                   delivery_type_txt.setVisibility(View.VISIBLE);
                   setCartRestaurantDetails();
                   checkIfCartItemAvailable();
                   setBillDetails();
                   adapter = new MyCartAdapter(getContext(), cartItems);
                   recycler_cart.setAdapter(adapter);
                   myCartBillAdapter = new MyCartBillAdapter(getContext(),cartItems);
                   recycler_bill_cart.setAdapter(myCartBillAdapter);

               }
           }
       });
       unbinder = ButterKnife.bind(this,root);
       initViews();
       initLocation();
       setListener();
       return root;
    }

    private void setBillDetails() {

        /*int numberOfItems = cartItemListTemp.size();
        cart_item_details.setText("");
        cart_item_price_details.setText("");
        for(int i=0;i<numberOfItems;i++){

            cart_item_details.append(new StringBuilder()
            .append(cartItemListTemp.get(i).getFoodName())
            .append(" X ")
            .append(cartItemListTemp.get(i).getFoodQuantity())
            .append("\n"));

            cart_item_price_details.append(new StringBuilder("₹")
            .append(cartItemListTemp.get(i).getFoodPrice() * cartItemListTemp.get(i).getFoodQuantity())
            .append("\n"));

            if(i+1 < numberOfItems){
                cart_item_details.append("\n");
                cart_item_price_details.append("\n");
            }

        }*/

    }

    private void checkIfCartItemAvailable() {
        String resIdCheck = cartItemListTemp.get(0).getRestaurantId();
        List<CategoryModel> categoryTempList = new ArrayList<>();
        //Firebase api
        DatabaseReference categoryRef = FirebaseDatabase.getInstance(Common.restaurantDB)
                .getReference(Common.RESTAURANT_REF)
                .child(resIdCheck)
                .child(Common.CATEGORY_REF);
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot itemSnapShot : snapshot.getChildren()){
                    CategoryModel model = itemSnapShot.getValue(CategoryModel.class);
                    model.setMenu_id(itemSnapShot.getKey());
                    categoryTempList.add(model);
                }
                categoryCallbackListener.onCategoryLoadSuccess(categoryTempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                categoryCallbackListener.onCategoryLoadFailed(error.getMessage());
            }
        });

    }

    @Override
    public void onCategoryLoadSuccess(List<CategoryModel> categoryModelList) {
        outerLoop1:
            for (int i = 0; !(i >= categoryModelList.size()); i++) {
                for (int j = 0; !(categoryModelList.get(i).getFoods().size() <= j); j++) {
                    for (int k = 0; k < cartItemListTemp.size(); k++) {
                        if (!categoryModelList.get(i).getFoods().get(j).getId().equals(cartItemListTemp.get(k).getFoodId())) {
                            continue;
                        }
                        if (!categoryModelList.get(i).getFoods().get(j).isAvailable()) {
                            //Item not available is in cart
                            //change Ui
                            anyItemNotAvailable = true;
                            proceed_to_pay_txt.setBackgroundColor(getResources().getColor(R.color.quantum_googred800));
                            proceed_to_pay_txt.setText(new StringBuilder()
                            .append(cartItemListTemp.get(k).getFoodName())
                            .append(" not available"));
                            break outerLoop1;
                        }
                        else {
                            anyItemNotAvailable = false;
                            proceed_to_pay_txt.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                            proceed_to_pay_txt.setText(new StringBuilder()
                                    .append("PROCEED TO PAY"));
                        }
                    }
                }
            }
    }

    @Override
    public void onCategoryLoadFailed(String message) {
        Log.i("ERROR",message);

    }

    private void setListener() {
        proceed_pay_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.equals(restaurantOpenStatus,"false")){
                    Toast.makeText(getContext(), "Restaurant closed", Toast.LENGTH_SHORT).show();
                }
                else if(anyItemNotAvailable){
                    Toast.makeText(getContext(), "One of your cart item not available", Toast.LENGTH_SHORT).show();
                }
                else if(text_delivery_location.getText().toString().isEmpty() && !isPickup){
                    text_delivery_location.setError("Location needed");
                    Toast.makeText(getContext(), "Please enter delivery location", Toast.LENGTH_SHORT).show();
                }
                else {
                        address = text_delivery_location.getText().toString();
                        comment = text_instruction.getText().toString();
                        makeCashFreePaymentRequest();
                }
            }
        });
        text_browse_restaurants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               EventBus.getDefault().post(new NavigateExploreEvent(true));
            }
        });
       radio_pickup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if(isChecked){
                   radio_delivery.setChecked(false);
                   isPickup = true;
                   text_delivery_location.setVisibility(View.GONE);
                   delivery_charges_layout.setVisibility(View.GONE);
                   calculateTotalPrice();
               }
           }
       });
        radio_delivery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    radio_pickup.setChecked(false);
                    isPickup = false;
                    text_delivery_location.setVisibility(View.VISIBLE);
                    delivery_charges_layout.setVisibility(View.VISIBLE);
                    calculateTotalPrice();
                }
            }
        });
        //Keyboard
        root_layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                root_layout.getWindowVisibleDisplayFrame(r);
                int screenHeight = root_layout.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) {
                    //Keypad opened
                    group_place_holder.setVisibility(View.GONE);
                    //Hide bottom navigation
                    EventBus.getDefault().post(new HideBottomNavigation(true));
                } else {
                    EventBus.getDefault().post(new HideBottomNavigation(false));
                    if(cartItemListTemp == null || cartItemListTemp.isEmpty()){
                        group_place_holder.setVisibility(View.GONE);
                    }
                    else
                        group_place_holder.setVisibility(View.VISIBLE);
                }
            }

        });
    }

    private void setCartRestaurantDetails() {
        hud1.show();
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

                        FirebaseDatabase.getInstance(Common.serverValues)
                                .getReference(Common.ONCAMPUS_PERCENTAGE_REF)
                                .child(Common.currentUser.getCampusId())
                                .child(restaurantId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        PercentagePackingDeliveryModel percentagePackingDeliveryModel =  snapshot.getValue(PercentagePackingDeliveryModel.class);
                                        assert percentagePackingDeliveryModel != null;
                                        onCampusPercentage = percentagePackingDeliveryModel.getPercentage();
                                        packingCharges = percentagePackingDeliveryModel.getPackingCharges();
                                        deliveryCharges = percentagePackingDeliveryModel.getDeliveryCharges();
                                        calculateTotalPrice();
                                        FirebaseDatabase.getInstance(Common.restaurantDB)
                                                .getReference(Common.RESTAURANT_REF)
                                                .child(restaurantId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        RestaurantModel restaurantModel = snapshot.getValue(RestaurantModel.class);
                                                        assert restaurantModel != null;
                                                        Glide.with(getContext())
                                                                .load(restaurantModel.getImageUrl())
                                                                .placeholder(R.mipmap.oncampus_logo_green_white_bg_512)
                                                                .into(cart_restaurant_image);
                                                        cart_restaurant_name.setText(restaurantModel.getName());
                                                        text_instruction.setHint(new StringBuilder()
                                                                .append("Any information to convey ")
                                                                .append(restaurantModel.getName())
                                                                .append("?"));
                                                        hud1.dismiss();
                                                        restaurantOpenStatus = restaurantModel.getIsOpened();
                                                        if(TextUtils.equals(restaurantModel.getIsOpened(),"false")){
                                                            proceed_to_pay_txt.setBackgroundColor(getContext().getResources().getColor(R.color.googleRedColor));
                                                            proceed_to_pay_txt.setText(new StringBuilder()
                                                                    .append("Restaurant closed"));
                                                            proceed_to_pay_txt.setTextColor(getContext().getResources().getColor(R.color.white));

                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        hud1.dismiss();
                                                        Toast.makeText(getContext(), ""+ error.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }
                    @Override
                    public void onError(Throwable e) {
                        hud1.dismiss();
                        if(e.getMessage().contains("empty")){
                            restaurantId = null;
                        }
                    }
                });

    }

    @SuppressLint("MissingPermission")
    private void initLocation() {
        buildLocationRequest();
        buildLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.getMainLooper());

    }

    private void buildLocationCallback() {
       locationCallback = new LocationCallback(){
           @Override
           public void onLocationResult(LocationResult locationResult) {
               super.onLocationResult(locationResult);
               currentLocation = locationResult.getLastLocation();
           }
       };

    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);

    }

    private void initViews() {
        setHasOptionsMenu(true);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());
        recycler_cart.setHasFixedSize(true);
        recycler_bill_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_bill_cart.setLayoutManager(layoutManager1);
        recycler_bill_cart.addItemDecoration(new SpacesItemDecoration(10));
        recycler_cart.addItemDecoration(new SpacesItemDecoration(10));
        categoryCallbackListener = this;
        if(radio_pickup.isChecked()){
            isPickup = true;

        }else if(radio_delivery.isChecked()){
            isPickup = false;
        }else {
            isPickup = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        cartViewModel.onStop();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if(fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        compositeDisposable.clear();
        super.onStop();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        if(fusedLocationProviderClient != null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.getMainLooper());
    }

    /*********************************************************************************************/
    //Update cart item EVENT
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemInCart event)
    {
        if(event.getCartItem() != null)
        {
            hud1.show();
            //save state of recycler view
            recyclerViewState = recycler_cart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {


                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            calculateTotalPrice();
                            EventBus.getDefault().post(new CounterCartEvent(true));
                            recycler_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState);// fixed error - refresh recycler view after update
                            hud1.dismiss();

                        }

                        @Override
                        public void onError(Throwable e) {
                            hud1.dismiss();

                        }
                    });


        }
    }

    /*********************************************************************************************/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeleteCartItemNotifyAdapter(DeleteCartItemQuantityZeroNotify event){
        adapter.notifyItemRemoved(event.getPos());
        calculateTotalPrice();
        EventBus.getDefault().post(new CounterCartEvent(true));
    }




    /*********************************************************************************************/

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double price) {
                        //Calculate Total items for packing charges
                        int totalItems = 0;
                        for(int i=0;i<cartItemListTemp.size();i++){
                            totalItems += cartItemListTemp.get(i).getFoodQuantity();
                        }
                        double total_price = 0;

                        if(isPickup){
                            total_price = price + (packingCharges * totalItems);
                        }else {
                            total_price = price + (packingCharges * totalItems) + deliveryCharges;
                        }

                        //For payment intent
                        deliveryCharges_SEND_TO_PAYMENT_INTENT = deliveryCharges;
                        packingCharges_SEND_TO_PAYMENT_INTENT = packingCharges * totalItems;
                        onlyFoodPrice_SEND_TO_PAYMENT_INTENT = price;


                        totalPrice = total_price;
                        orderAmount = Double.toString(total_price); // To get CF TOKEN
                        onCampusPercentageAmount = calculateOnCampusPercentageAmount(price,onCampusPercentage,totalItems);

                        //Item total without packing and delivery
                        text_cart_total.setText(new StringBuilder()
                        .append("₹")
                        .append(Common.formatPrice(price)));

                        //Display packing charges
                        text_packing_charges.setText(new StringBuilder(" ₹")
                        .append(Common.formatPrice(packingCharges * totalItems)));

                        //Display delivery charges
                        text_delivery_charges.setText(new StringBuilder(" ₹")
                        .append(Common.formatPrice(deliveryCharges)));

                        //Grand Total display
                        text_cart_total_including_delivery_packing.setText(new StringBuilder(" ₹")
                        .append(Common.formatPrice(total_price)));

                        //Proceed to pay Amount display
                        txt_total_price.setText(new StringBuilder(" ₹")
                                .append(Common.formatPrice(total_price)));

                    }

                    @Override
                    public void onError(Throwable e) {
                        if(!e.getMessage().contains("Query returned empty result set"))
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private double calculateOnCampusPercentageAmount(Double price, double onCampusPercentage, int totalItems) {
        double onCampusPercentageAmount = onCampusPercentage/100 * price;
        if(isPickup){
            payToRestaurantAfterCommissionAmount = (price - onCampusPercentageAmount) + (packingCharges * totalItems);
        }
        else
            payToRestaurantAfterCommissionAmount = (price - onCampusPercentageAmount) + (packingCharges * totalItems) + deliveryCharges;
       // Toast.makeText(getContext(), "Our Amount:"+onCampusPercentageAmount, Toast.LENGTH_SHORT).show();
        //Toast.makeText(getContext(), "Pay to Restaurant:"+payToRestaurantAfterCommissionAmount, Toast.LENGTH_SHORT).show();
        return onCampusPercentageAmount;
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
