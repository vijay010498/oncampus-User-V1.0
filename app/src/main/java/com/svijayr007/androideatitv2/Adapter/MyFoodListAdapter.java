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

package com.svijayr007.androideatitv2.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.svijayr007.androideatitv2.Common.Common;
import com.svijayr007.androideatitv2.Database.CartDataSource;
import com.svijayr007.androideatitv2.Database.CartDatabase;
import com.svijayr007.androideatitv2.Database.CartItem;
import com.svijayr007.androideatitv2.Database.LocalCartDataSource;
import com.svijayr007.androideatitv2.EventBus.CounterCartEvent;
import com.svijayr007.androideatitv2.EventBus.ShowViewCartBottomSheet;
import com.svijayr007.androideatitv2.Model.FoodModel;
import com.svijayr007.androideatitv2.R;
import com.svijayr007.androideatitv2.callback.IRecyclerClickListener;
import com.svijayr007.androideatitv2.ui.restaurant_detail.CartItemCheckViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyFoodListAdapter extends RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder> {
    private Context context;
    private List<FoodModel> foodModelList;
    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;
    private String restaurantId = null;
    private String restaurantName = null;
    private KProgressHUD hud;
    private CartItemCheckViewModel cartItemCheckViewModel;


    public MyFoodListAdapter(Context context, List<FoodModel> foodModelList) {
        this.context = context;
        this.foodModelList = foodModelList;
        cartItemCheckViewModel = ViewModelProviders.of((FragmentActivity) context).get(CartItemCheckViewModel.class);
        cartItemCheckViewModel.initCartDataSource(context);
        compositeDisposable = new CompositeDisposable();
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
        hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark))
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.75f);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_food_items,parent,false));



    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
                    setUiDataNormal(holder, position);

    }


    private void setUiDataOnlyVeg(MyViewHolder holder, int position) {

        if(foodModelList.get(position).getIsVeg() == 1) {
            final int[] quantity = {0};
            //Getting  quantity
            cartItemCheckViewModel.getCartItemMutableLiveData(foodModelList.get(position).getId(), Common.currentRestaurant.getId()).observe((FragmentActivity) context, new Observer<CartItem>() {
                @Override
                public void onChanged(CartItem cartItem) {
                    if (cartItem != null) {
                        if (foodModelList.get(position) != null) {
                            if (TextUtils.equals(foodModelList.get(position).getId(), cartItem.getFoodId()) && foodModelList.get(position).isAvailable()) {
                                quantity[0] = cartItem.getFoodQuantity();
                                holder.text_quantity.setText(String.valueOf(quantity[0]));
                                holder.image_sub.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            });
            if (!foodModelList.get(position).isAvailable()) {
                holder.image_add.setVisibility(View.GONE);
                holder.text_quantity.setText("Out of stock");
            }

            /****************************************************************************************************/

            //veg non-veg image
            //default veg
            if (foodModelList.get(position).getIsVeg() == 0) {
                holder.image_veg_non_veg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_non_veg));
            } else if (foodModelList.get(position).getIsVeg() == 1) {
                holder.image_veg_non_veg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_veg));
            } else {
                holder.image_veg_non_veg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_non_veg));
            }


            /****************************************************************************************************/


            //set listener
            //Add button
            holder.image_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hud.show();
                    if (quantity[0] == 0) {
                        //Quantity not in the cart so add as new item
                        CartItem cartItem = new CartItem();
                        cartItem.setRestaurantId(Common.currentRestaurant.getId());
                        cartItem.setRestaurantName(Common.currentRestaurant.getName());
                        cartItem.setUid(Common.currentUser.getUid());
                        cartItem.setUserPhone(Common.currentUser.getPhone());
                        cartItem.setFoodId(foodModelList.get(position).getId());
                        cartItem.setFoodName(foodModelList.get(position).getName());
                        cartItem.setFoodImage(foodModelList.get(position).getImage());
                        cartItem.setIsVeg(foodModelList.get(position).getIsVeg());
                        compositeDisposable.add(cartDataSource.getLastItemRestaurantName(Common.currentUser.getUid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(lastRestaurantName -> {
                                    restaurantName = lastRestaurantName;
                                    Log.d("RESTAURANT NAME", restaurantName);

                                    //Check for last restaurant is same
                                    cartDataSource.getLastItemRestaurantId(Common.currentUser.getUid())
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new SingleObserver<String>() {
                                                @Override
                                                public void onSubscribe(Disposable d) {

                                                }

                                                @Override
                                                public void onSuccess(String lastRestaurantId) {
                                                    //Cart is not empty check check for restaurant and add to cart
                                                    restaurantId = lastRestaurantId;
                                                    if (restaurantId.equals(Common.currentRestaurant.getId())) {
                                                        //Restaurant id matches
                                                        quantity[0]++;
                                                        cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice() * quantity[0])));
                                                        cartItem.setFoodQuantity(quantity[0]);
                                                        holder.text_quantity.setText(String.valueOf(quantity[0]));
                                                        holder.image_sub.setVisibility(View.VISIBLE);
                                                        compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(() -> {
                                                                    hud.dismiss();
                                                                    //Toast.makeText(context, cartItem.getFoodName() + " Added to cart", Toast.LENGTH_SHORT).show();
                                                                    EventBus.getDefault().post(new ShowViewCartBottomSheet(true));
                                                                }, throwable -> {
                                                                    hud.dismiss();

                                                                }));

                                                    } else {
                                                        //Ask to replace cart with another restaurant
                                                        hud.dismiss();
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                        builder.setTitle("Replace cart item?");
                                                        builder.setMessage(new StringBuilder()
                                                                .append("your cart contains dishes from ")
                                                                .append(restaurantName)
                                                                .append(" . Do you want to clear the cart and add from ")
                                                                .append(cartItem.getRestaurantName()).append("."));
                                                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int which) {
                                                                dialogInterface.dismiss();
                                                            }
                                                        });
                                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int which) {
                                                                //Clean cart and add new Item
                                                                hud.show();
                                                                cartDataSource.cleanCart(Common.currentUser.getUid())
                                                                        .subscribeOn(Schedulers.io())
                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                        .subscribe(new SingleObserver<Integer>() {
                                                                            @Override
                                                                            public void onSubscribe(Disposable d) {

                                                                            }

                                                                            @Override
                                                                            public void onSuccess(Integer integer) {
                                                                                //Cart cleaned Add to cart
                                                                                quantity[0]++;
                                                                                cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice() * quantity[0])));
                                                                                cartItem.setFoodQuantity(quantity[0]);
                                                                                holder.text_quantity.setText(String.valueOf(quantity[0]));
                                                                                holder.image_sub.setVisibility(View.VISIBLE);
                                                                                compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                                                        .subscribeOn(Schedulers.io())
                                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                                        .subscribe(() -> {
                                                                                            hud.dismiss();
                                                                                            //Toast.makeText(context, cartItem.getFoodName() + " added to cart", Toast.LENGTH_SHORT).show();
                                                                                            EventBus.getDefault().post(new ShowViewCartBottomSheet(true));

                                                                                        }, throwable -> {
                                                                                            hud.dismiss();
                                                                                            Toast.makeText(context, "[CART ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                                                                        }));


                                                                            }

                                                                            @Override
                                                                            public void onError(Throwable e) {
                                                                                hud.dismiss();
                                                                                Log.i("ERROR", e.getMessage());
                                                                            }
                                                                        });

                                                            }
                                                        });
                                                        AlertDialog dialog = builder.create();
                                                        dialog.show();
                                                    }


                                                }

                                                @Override
                                                //When restaurantId is empty only fired when cart is empty for the first time
                                                public void onError(Throwable e) {
                                                    if (e.getMessage().contains("empty")) {
                                                        quantity[0]++;
                                                        cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice() * quantity[0])));
                                                        cartItem.setFoodQuantity(quantity[0]);
                                                        holder.text_quantity.setText(String.valueOf(quantity[0]));
                                                        holder.image_sub.setVisibility(View.VISIBLE);
                                                        compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(() -> {
                                                                    hud.dismiss();
                                                                    //Toast.makeText(context, cartItem.getFoodName() + " Added to cart", Toast.LENGTH_SHORT).show();
                                                                    EventBus.getDefault().post(new ShowViewCartBottomSheet(true));

                                                                }, throwable -> {
                                                                    hud.dismiss();

                                                                }));
                                                    } else {
                                                        hud.dismiss();
                                                    }

                                                }
                                            });

                                }, throwable -> {
                                    //Check for last restaurant is same
                                    if (throwable.getMessage().contains("empty")) {
                                        cartDataSource.getLastItemRestaurantId(Common.currentUser.getUid())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new SingleObserver<String>() {
                                                    @Override
                                                    public void onSubscribe(Disposable d) {

                                                    }

                                                    @Override
                                                    public void onSuccess(String lastRestaurantId) {
                                                        //Cart is not empty check check for restaurant and add to cart
                                                        restaurantId = lastRestaurantId;
                                                        if (restaurantId.equals(Common.currentRestaurant.getId())) {
                                                            //Restaurant id matches
                                                            quantity[0]++;
                                                            cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice() * quantity[0])));
                                                            cartItem.setFoodQuantity(quantity[0]);
                                                            holder.text_quantity.setText(String.valueOf(quantity[0]));
                                                            holder.image_sub.setVisibility(View.VISIBLE);
                                                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                                    .subscribeOn(Schedulers.io())
                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                    .subscribe(() -> {
                                                                        hud.dismiss();
                                                                        // Toast.makeText(context, cartItem.getFoodName() + " Added to cart", Toast.LENGTH_SHORT).show();
                                                                        EventBus.getDefault().post(new ShowViewCartBottomSheet(true));
                                                                    }, throwable -> {
                                                                        hud.dismiss();

                                                                    }));

                                                        } else {
                                                            //Ask to replace cart with another restaurant
                                                            hud.dismiss();
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                            builder.setTitle("Replace cart item?");
                                                            builder.setMessage(new StringBuilder()
                                                                    .append("your cart contains dishes from ")
                                                                    .append(restaurantName)
                                                                    .append(" . Do you want to clear the cart and add from ")
                                                                    .append(cartItem.getRestaurantName()).append("."));
                                                            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                                    dialogInterface.dismiss();
                                                                }
                                                            });
                                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                                    //Clean cart and add new Item
                                                                    hud.show();
                                                                    cartDataSource.cleanCart(Common.currentUser.getUid())
                                                                            .subscribeOn(Schedulers.io())
                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                            .subscribe(new SingleObserver<Integer>() {
                                                                                @Override
                                                                                public void onSubscribe(Disposable d) {

                                                                                }

                                                                                @Override
                                                                                public void onSuccess(Integer integer) {
                                                                                    //Cart cleaned Add to cart
                                                                                    quantity[0]++;
                                                                                    cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice() * quantity[0])));
                                                                                    cartItem.setFoodQuantity(quantity[0]);
                                                                                    holder.text_quantity.setText(String.valueOf(quantity[0]));
                                                                                    holder.image_sub.setVisibility(View.VISIBLE);
                                                                                    compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                                                            .subscribeOn(Schedulers.io())
                                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                                            .subscribe(() -> {
                                                                                                hud.dismiss();
                                                                                                //Toast.makeText(context, cartItem.getFoodName() + " added to cart", Toast.LENGTH_SHORT).show();
                                                                                                EventBus.getDefault().post(new ShowViewCartBottomSheet(true));

                                                                                            }, throwable -> {
                                                                                                hud.dismiss();
                                                                                                Toast.makeText(context, "[CART ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                                                                            }));


                                                                                }

                                                                                @Override
                                                                                public void onError(Throwable e) {
                                                                                    hud.dismiss();
                                                                                    Log.i("ERROR", e.getMessage());
                                                                                }
                                                                            });

                                                                }
                                                            });
                                                            AlertDialog dialog = builder.create();
                                                            dialog.show();
                                                        }


                                                    }

                                                    @Override
                                                    //When restaurantId is empty only fired when cart is empty for the first time
                                                    public void onError(Throwable e) {
                                                        if (e.getMessage().contains("empty")) {
                                                            quantity[0]++;
                                                            cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice() * quantity[0])));
                                                            cartItem.setFoodQuantity(quantity[0]);
                                                            holder.text_quantity.setText(String.valueOf(quantity[0]));
                                                            holder.image_sub.setVisibility(View.VISIBLE);
                                                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                                    .subscribeOn(Schedulers.io())
                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                    .subscribe(() -> {
                                                                        hud.dismiss();
                                                                        //Toast.makeText(context, cartItem.getFoodName() + " Added to cart", Toast.LENGTH_SHORT).show();
                                                                        EventBus.getDefault().post(new ShowViewCartBottomSheet(true));

                                                                    }, throwable -> {
                                                                        hud.dismiss();

                                                                    }));
                                                        } else {
                                                            hud.dismiss();
                                                        }

                                                    }
                                                });
                                    } else {
                                        hud.dismiss();
                                    }

                                }));

                    } else if (quantity[0] > 0) {
                        //Already in cart just update cart
                        quantity[0]++;
                        holder.text_quantity.setText(String.valueOf(quantity[0]));
                        compositeDisposable.add(cartDataSource.getItemInCart(foodModelList.get(position).getId(), Common.currentUser.getUid(), Common.currentRestaurant.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(cartItem -> {
                                    //Update quantity and multiply with food price
                                    cartItem.setFoodQuantity(quantity[0]);
                                    compositeDisposable.add(cartDataSource.updateCartItems(cartItem)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(integer -> {
                                                hud.dismiss();
                                                // Toast.makeText(context, "Cart updated", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().post(new ShowViewCartBottomSheet(true));

                                            }, throwable -> {
                                                hud.dismiss();
                                                Toast.makeText(context, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                            }));

                                }, throwable -> {
                                    hud.dismiss();
                                    Toast.makeText(context, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                }));
                    }
                    EventBus.getDefault().post(new CounterCartEvent(true));

                }
            });

            //sub button
            holder.image_sub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hud.show();
                    if (quantity[0] - 1 > 0) {
                        //Update cart
                        quantity[0]--;
                        holder.text_quantity.setText(String.valueOf(quantity[0]));
                        compositeDisposable.add(cartDataSource.getItemInCart(foodModelList.get(position).getId(), Common.currentUser.getUid(), Common.currentRestaurant.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(cartItem -> {
                                    cartItem.setFoodQuantity(quantity[0]);
                                    compositeDisposable.add(cartDataSource.updateCartItems(cartItem)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(integer -> {
                                                hud.dismiss();
                                                //Toast.makeText(context, "Cart updated", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().post(new ShowViewCartBottomSheet(true));

                                            }, throwable -> {
                                                hud.dismiss();
                                                Toast.makeText(context, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                            }));

                                }, throwable -> {
                                    hud.dismiss();
                                    Toast.makeText(context, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }));

                    } else {
                        //clear cart
                        quantity[0]--;
                        holder.text_quantity.setText("Add");
                        holder.image_sub.setVisibility(View.GONE);
                        compositeDisposable.add(cartDataSource.getItemInCart(foodModelList.get(position).getId(), Common.currentUser.getUid(), Common.currentRestaurant.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(cartItem -> {
                                    cartItem.setFoodQuantity(quantity[0]);
                                    compositeDisposable.add(cartDataSource.deleteCartItem(cartItem)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(integer -> {
                                                hud.dismiss();
                                                //Toast.makeText(context, "Cart updated", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().post(new ShowViewCartBottomSheet(true));
                                            }, throwable -> {
                                                hud.dismiss();
                                                Toast.makeText(context, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }));

                                }, throwable -> {
                                    hud.dismiss();
                                    Toast.makeText(context, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }));

                    }
                    EventBus.getDefault().post(new CounterCartEvent(true));
                }

            });


            if (foodModelList.get(position) != null)
                Glide.with(context).load(foodModelList.get(position).getImage())
                        .placeholder(R.drawable.ic_food)
                        .into(holder.img_food_image);
            if (foodModelList.get(position) != null)
                holder.txt_food_price.setText(new StringBuilder("₹")
                        .append(foodModelList.get(position).getPrice()));
            if (foodModelList.get(position).getDescription() == null || TextUtils.isEmpty(foodModelList.get(position).getDescription()))
                holder.food_desc.setVisibility(View.GONE);
            else
                holder.food_desc.setText(new StringBuilder()
                        .append(foodModelList.get(position).getDescription()));
            if (foodModelList.get(position) != null)
                holder.txt_food_name.setText(new StringBuilder("")
                        .append(foodModelList.get(position).getName()));


            holder.txt_food_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "" + foodModelList.get(position).getDescription(), Toast.LENGTH_SHORT).show();

                }
            });

            //Event
            holder.setListener((view, pos) -> {

            });
        }
        else {
            holder.root_layout_food.setVisibility(View.GONE);
        }
    }

    private void setUiDataNormal(MyViewHolder holder, int position) {
        final int[] quantity = {0};
        //Getting  quantity
        cartItemCheckViewModel.getCartItemMutableLiveData(foodModelList.get(position).getId(), Common.currentRestaurant.getId()).observe((FragmentActivity) context, new Observer<CartItem>() {
            @Override
            public void onChanged(CartItem cartItem) {
                if (cartItem != null) {
                    if (foodModelList.get(position) != null) {
                        if (TextUtils.equals(foodModelList.get(position).getId(), cartItem.getFoodId()) && foodModelList.get(position).isAvailable()) {
                            quantity[0] = cartItem.getFoodQuantity();
                            holder.text_quantity.setText(String.valueOf(quantity[0]));
                            holder.image_sub.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
        if (!foodModelList.get(position).isAvailable()) {
            holder.image_add.setVisibility(View.GONE);
            holder.text_quantity.setText("Out of stock");
        }


        /****************************************************************************************************/

        //veg non-veg image
        //default veg
        if (foodModelList.get(position).getIsVeg() == 0) {
            holder.image_veg_non_veg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_non_veg));
        } else if (foodModelList.get(position).getIsVeg() == 1) {
            holder.image_veg_non_veg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_veg));
        } else {
            holder.image_veg_non_veg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_non_veg));
        }


        /****************************************************************************************************/


        //set listener
        //Add button
        holder.image_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hud.show();
                if (quantity[0] == 0) {
                    //Quantity not in the cart so add as new item
                    CartItem cartItem = new CartItem();
                    cartItem.setRestaurantId(Common.currentRestaurant.getId());
                    cartItem.setRestaurantName(Common.currentRestaurant.getName());
                    cartItem.setUid(Common.currentUser.getUid());
                    cartItem.setUserPhone(Common.currentUser.getPhone());
                    cartItem.setFoodId(foodModelList.get(position).getId());
                    cartItem.setFoodName(foodModelList.get(position).getName());
                    cartItem.setFoodImage(foodModelList.get(position).getImage());
                    cartItem.setIsVeg(foodModelList.get(position).getIsVeg());
                    compositeDisposable.add(cartDataSource.getLastItemRestaurantName(Common.currentUser.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(lastRestaurantName -> {
                                restaurantName = lastRestaurantName;
                                Log.d("RESTAURANT NAME", restaurantName);

                                //Check for last restaurant is same
                                cartDataSource.getLastItemRestaurantId(Common.currentUser.getUid())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SingleObserver<String>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {

                                            }

                                            @Override
                                            public void onSuccess(String lastRestaurantId) {
                                                //Cart is not empty check check for restaurant and add to cart
                                                restaurantId = lastRestaurantId;
                                                if (restaurantId.equals(Common.currentRestaurant.getId())) {
                                                    //Restaurant id matches
                                                    quantity[0]++;
                                                    cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice() * quantity[0])));
                                                    cartItem.setFoodQuantity(quantity[0]);
                                                    holder.text_quantity.setText(String.valueOf(quantity[0]));
                                                    holder.image_sub.setVisibility(View.VISIBLE);
                                                    compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(() -> {
                                                                hud.dismiss();
                                                                //Toast.makeText(context, cartItem.getFoodName() + " Added to cart", Toast.LENGTH_SHORT).show();
                                                                EventBus.getDefault().post(new ShowViewCartBottomSheet(true));
                                                            }, throwable -> {
                                                                hud.dismiss();

                                                            }));

                                                } else {
                                                    //Ask to replace cart with another restaurant
                                                    hud.dismiss();
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                    builder.setTitle("Replace cart item?");
                                                    builder.setMessage(new StringBuilder()
                                                            .append("your cart contains dishes from ")
                                                            .append(restaurantName)
                                                            .append(" . Do you want to clear the cart and add from ")
                                                            .append(cartItem.getRestaurantName()).append("."));
                                                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int which) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    });
                                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int which) {
                                                            //Clean cart and add new Item
                                                            hud.show();
                                                            cartDataSource.cleanCart(Common.currentUser.getUid())
                                                                    .subscribeOn(Schedulers.io())
                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                    .subscribe(new SingleObserver<Integer>() {
                                                                        @Override
                                                                        public void onSubscribe(Disposable d) {

                                                                        }

                                                                        @Override
                                                                        public void onSuccess(Integer integer) {
                                                                            //Cart cleaned Add to cart
                                                                            quantity[0]++;
                                                                            cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice() * quantity[0])));
                                                                            cartItem.setFoodQuantity(quantity[0]);
                                                                            holder.text_quantity.setText(String.valueOf(quantity[0]));
                                                                            holder.image_sub.setVisibility(View.VISIBLE);
                                                                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                                                    .subscribeOn(Schedulers.io())
                                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                                    .subscribe(() -> {
                                                                                        hud.dismiss();
                                                                                        //Toast.makeText(context, cartItem.getFoodName() + " added to cart", Toast.LENGTH_SHORT).show();
                                                                                        EventBus.getDefault().post(new ShowViewCartBottomSheet(true));

                                                                                    }, throwable -> {
                                                                                        hud.dismiss();
                                                                                        Toast.makeText(context, "[CART ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                                                                    }));


                                                                        }

                                                                        @Override
                                                                        public void onError(Throwable e) {
                                                                            hud.dismiss();
                                                                            Log.i("ERROR", e.getMessage());
                                                                        }
                                                                    });

                                                        }
                                                    });
                                                    AlertDialog dialog = builder.create();
                                                    dialog.show();
                                                }


                                            }

                                            @Override
                                            //When restaurantId is empty only fired when cart is empty for the first time
                                            public void onError(Throwable e) {
                                                if (e.getMessage().contains("empty")) {
                                                    quantity[0]++;
                                                    cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice() * quantity[0])));
                                                    cartItem.setFoodQuantity(quantity[0]);
                                                    holder.text_quantity.setText(String.valueOf(quantity[0]));
                                                    holder.image_sub.setVisibility(View.VISIBLE);
                                                    compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(() -> {
                                                                hud.dismiss();
                                                                //Toast.makeText(context, cartItem.getFoodName() + " Added to cart", Toast.LENGTH_SHORT).show();
                                                                EventBus.getDefault().post(new ShowViewCartBottomSheet(true));

                                                            }, throwable -> {
                                                                hud.dismiss();

                                                            }));
                                                } else {
                                                    hud.dismiss();
                                                }

                                            }
                                        });

                            }, throwable -> {
                                //Check for last restaurant is same
                                if (throwable.getMessage().contains("empty")) {
                                    cartDataSource.getLastItemRestaurantId(Common.currentUser.getUid())
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new SingleObserver<String>() {
                                                @Override
                                                public void onSubscribe(Disposable d) {

                                                }

                                                @Override
                                                public void onSuccess(String lastRestaurantId) {
                                                    //Cart is not empty check check for restaurant and add to cart
                                                    restaurantId = lastRestaurantId;
                                                    if (restaurantId.equals(Common.currentRestaurant.getId())) {
                                                        //Restaurant id matches
                                                        quantity[0]++;
                                                        cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice() * quantity[0])));
                                                        cartItem.setFoodQuantity(quantity[0]);
                                                        holder.text_quantity.setText(String.valueOf(quantity[0]));
                                                        holder.image_sub.setVisibility(View.VISIBLE);
                                                        compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(() -> {
                                                                    hud.dismiss();
                                                                    // Toast.makeText(context, cartItem.getFoodName() + " Added to cart", Toast.LENGTH_SHORT).show();
                                                                    EventBus.getDefault().post(new ShowViewCartBottomSheet(true));
                                                                }, throwable -> {
                                                                    hud.dismiss();

                                                                }));

                                                    } else {
                                                        //Ask to replace cart with another restaurant
                                                        hud.dismiss();
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                        builder.setTitle("Replace cart item?");
                                                        builder.setMessage(new StringBuilder()
                                                                .append("your cart contains dishes from ")
                                                                .append(restaurantName)
                                                                .append(" . Do you want to clear the cart and add from ")
                                                                .append(cartItem.getRestaurantName()).append("."));
                                                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int which) {
                                                                dialogInterface.dismiss();
                                                            }
                                                        });
                                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int which) {
                                                                //Clean cart and add new Item
                                                                hud.show();
                                                                cartDataSource.cleanCart(Common.currentUser.getUid())
                                                                        .subscribeOn(Schedulers.io())
                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                        .subscribe(new SingleObserver<Integer>() {
                                                                            @Override
                                                                            public void onSubscribe(Disposable d) {

                                                                            }

                                                                            @Override
                                                                            public void onSuccess(Integer integer) {
                                                                                //Cart cleaned Add to cart
                                                                                quantity[0]++;
                                                                                cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice() * quantity[0])));
                                                                                cartItem.setFoodQuantity(quantity[0]);
                                                                                holder.text_quantity.setText(String.valueOf(quantity[0]));
                                                                                holder.image_sub.setVisibility(View.VISIBLE);
                                                                                compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                                                        .subscribeOn(Schedulers.io())
                                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                                        .subscribe(() -> {
                                                                                            hud.dismiss();
                                                                                            //Toast.makeText(context, cartItem.getFoodName() + " added to cart", Toast.LENGTH_SHORT).show();
                                                                                            EventBus.getDefault().post(new ShowViewCartBottomSheet(true));

                                                                                        }, throwable -> {
                                                                                            hud.dismiss();
                                                                                            Toast.makeText(context, "[CART ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                                                                        }));


                                                                            }

                                                                            @Override
                                                                            public void onError(Throwable e) {
                                                                                hud.dismiss();
                                                                                Log.i("ERROR", e.getMessage());
                                                                            }
                                                                        });

                                                            }
                                                        });
                                                        AlertDialog dialog = builder.create();
                                                        dialog.show();
                                                    }


                                                }

                                                @Override
                                                //When restaurantId is empty only fired when cart is empty for the first time
                                                public void onError(Throwable e) {
                                                    if (e.getMessage().contains("empty")) {
                                                        quantity[0]++;
                                                        cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice() * quantity[0])));
                                                        cartItem.setFoodQuantity(quantity[0]);
                                                        holder.text_quantity.setText(String.valueOf(quantity[0]));
                                                        holder.image_sub.setVisibility(View.VISIBLE);
                                                        compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(() -> {
                                                                    hud.dismiss();
                                                                    //Toast.makeText(context, cartItem.getFoodName() + " Added to cart", Toast.LENGTH_SHORT).show();
                                                                    EventBus.getDefault().post(new ShowViewCartBottomSheet(true));

                                                                }, throwable -> {
                                                                    hud.dismiss();

                                                                }));
                                                    } else {
                                                        hud.dismiss();
                                                    }

                                                }
                                            });
                                } else {
                                    hud.dismiss();
                                }

                            }));

                } else if (quantity[0] > 0) {
                    //Already in cart just update cart
                    quantity[0]++;
                    holder.text_quantity.setText(String.valueOf(quantity[0]));
                    compositeDisposable.add(cartDataSource.getItemInCart(foodModelList.get(position).getId(), Common.currentUser.getUid(), Common.currentRestaurant.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(cartItem -> {
                                //Update quantity and multiply with food price
                                cartItem.setFoodQuantity(quantity[0]);
                                compositeDisposable.add(cartDataSource.updateCartItems(cartItem)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(integer -> {
                                            hud.dismiss();
                                            // Toast.makeText(context, "Cart updated", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().post(new ShowViewCartBottomSheet(true));

                                        }, throwable -> {
                                            hud.dismiss();
                                            Toast.makeText(context, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                        }));

                            }, throwable -> {
                                hud.dismiss();
                                Toast.makeText(context, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                            }));
                }
                EventBus.getDefault().post(new CounterCartEvent(true));

            }
        });

        //sub button
        holder.image_sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hud.show();
                if (quantity[0] - 1 > 0) {
                    //Update cart
                    quantity[0]--;
                    holder.text_quantity.setText(String.valueOf(quantity[0]));
                    compositeDisposable.add(cartDataSource.getItemInCart(foodModelList.get(position).getId(), Common.currentUser.getUid(), Common.currentRestaurant.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(cartItem -> {
                                cartItem.setFoodQuantity(quantity[0]);
                                compositeDisposable.add(cartDataSource.updateCartItems(cartItem)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(integer -> {
                                            hud.dismiss();
                                            //Toast.makeText(context, "Cart updated", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().post(new ShowViewCartBottomSheet(true));

                                        }, throwable -> {
                                            hud.dismiss();
                                            Toast.makeText(context, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                        }));

                            }, throwable -> {
                                hud.dismiss();
                                Toast.makeText(context, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));

                } else {
                    //clear cart
                    quantity[0]--;
                    holder.text_quantity.setText("Add");
                    holder.image_sub.setVisibility(View.GONE);
                    compositeDisposable.add(cartDataSource.getItemInCart(foodModelList.get(position).getId(), Common.currentUser.getUid(), Common.currentRestaurant.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(cartItem -> {
                                cartItem.setFoodQuantity(quantity[0]);
                                compositeDisposable.add(cartDataSource.deleteCartItem(cartItem)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(integer -> {
                                            hud.dismiss();
                                            //Toast.makeText(context, "Cart updated", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().post(new ShowViewCartBottomSheet(true));
                                        }, throwable -> {
                                            hud.dismiss();
                                            Toast.makeText(context, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }));

                            }, throwable -> {
                                hud.dismiss();
                                Toast.makeText(context, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));

                }
                EventBus.getDefault().post(new CounterCartEvent(true));
            }

        });


        if (foodModelList.get(position) != null)
            Glide.with(context).load(foodModelList.get(position).getImage())
                    .placeholder(R.drawable.ic_food)
                    .into(holder.img_food_image);
        if (foodModelList.get(position) != null)
            holder.txt_food_price.setText(new StringBuilder("₹")
                    .append(foodModelList.get(position).getPrice()));
        if (foodModelList.get(position).getDescription() == null || TextUtils.isEmpty(foodModelList.get(position).getDescription()))
            holder.food_desc.setVisibility(View.GONE);
        else
            holder.food_desc.setText(new StringBuilder()
                    .append(foodModelList.get(position).getDescription()));
        if (foodModelList.get(position) != null)
            holder.txt_food_name.setText(new StringBuilder("")
                    .append(foodModelList.get(position).getName()));


        holder.txt_food_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "" + foodModelList.get(position).getDescription(), Toast.LENGTH_SHORT).show();

            }
        });

        //Event
        holder.setListener((view, pos) -> {

        });
    }


    @Override
    public int getItemCount() {
        return foodModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Unbinder unbinder;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.food_desc)
        TextView food_desc;

        @BindView(R.id.image_veg_non_veg)
        ImageView image_veg_non_veg;
        @BindView(R.id.root_layout_food)
        LinearLayout root_layout_food;



        //Quantity control
        @BindView(R.id.text_quantity)
        TextView text_quantity;
        @BindView(R.id.image_sub)
        ImageView image_sub;
        @BindView(R.id.image_add)
        ImageView image_add;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v, getAdapterPosition());

        }
    }

}
