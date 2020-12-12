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
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.svijayr007.androideatitv2.Common.Common;
import com.svijayr007.androideatitv2.Model.RestaurantModel;
import com.svijayr007.androideatitv2.R;
import com.svijayr007.androideatitv2.callback.IRecyclerClickListener;
import com.svijayr007.androideatitv2.ui.restaurant_detail.RestaurantDetailActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import spencerstudios.com.bungeelib.Bungee;

public class MyRestaurantAdapter extends RecyclerView.Adapter<MyRestaurantAdapter.MyViewHolder> {
    Context context;
    List<RestaurantModel> restaurantModelList;

    public MyRestaurantAdapter(Context context, List<RestaurantModel> restaurantModelList) {
        this.context = context;
        this.restaurantModelList = restaurantModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_restaurant,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        //Total number of orders
        FirebaseDatabase.getInstance(Common.ordersDB)
                .getReference(Common.ORDER_REF)
                .orderByChild("restaurantId")
                .equalTo(restaurantModelList.get(position).getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int numberOfOrders = (int) snapshot.getChildrenCount();
                        if(numberOfOrders > 150) {
                            holder.txt_restaurant_order_count.setText(new StringBuilder()
                                    .append(numberOfOrders)
                                    .append(" orders delivered happily !"));
                            holder.txt_restaurant_order_count.setVisibility(View.VISIBLE);
                        }
                        else {
                            holder.txt_restaurant_order_count.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

        Glide.with(context)
                .load(restaurantModelList.get(position).getImageUrl())
                .placeholder(R.drawable.ic_restaurant)
                .into(holder.img_restaurant);
        holder.txt_restaurant_name.setText(new StringBuilder(restaurantModelList.get(position).getName()));
        holder.txt_restaurant_rating_count.setText(new StringBuilder()
        .append(restaurantModelList.get(position).getRatingCount())
        .append(" ratings!"));

        holder.txt_res_rating.setText(new StringBuilder()
        .append(restaurantModelList.get(position).getRating()));

        holder.txt_res_prepTime.setText(new StringBuilder()
        .append(restaurantModelList.get(position).getPrepTime())
        .append(" ")
        .append("MINS"));

        holder.txt_res_price_for_two.setText(new StringBuilder()
        .append(restaurantModelList.get(position).getPriceForTwoPeople())
                .append(" FOR TWO"));


        holder.txt_res_category.setText(new StringBuilder()
        .append(restaurantModelList.get(position).getFoodCategories()));

        //Rating animation
        Animation animation = AnimationUtils.loadAnimation(context,R.anim.fade_in);
        animation.setDuration(2500);
        holder.txt_restaurant_rating_count.startAnimation(animation);


        //Is opened
        if(TextUtils.equals(restaurantModelList.get(position).getIsOpened(),"false")){
            //res closed
            holder.txt_res_open_status.setText(new StringBuilder()
            .append("Opens Soon"));
            holder.txt_restaurant_name.setTextColor(ContextCompat.getColor(context,R.color.disabledColor));
            holder.txt_res_category.setTextColor(ContextCompat.getColor(context,R.color.disabledColor));

            holder.txt_res_open_status.setTextColor(ContextCompat.getColor(context,R.color.disabledColor));
            holder.txt_res_rating.setTextColor(ContextCompat.getColor(context,R.color.disabledColor));
            holder.txt_res_rating.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_disabled,0,0,0);

            holder.txt_res_prepTime.setTextColor(ContextCompat.getColor(context,R.color.disabledColor));
            holder.txt_res_prepTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_access_time_24_disabled,0,0,0);

            holder.txt_res_price_for_two.setTextColor(ContextCompat.getColor(context,R.color.disabledColor));
            holder.txt_res_price_for_two.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attach_money_24_disabled,0,0,0);

            holder.txt_restaurant_rating_count.setTextColor(ContextCompat.getColor(context,R.color.disabledColor));

            //res image
           ColorMatrix colorMatrix = new ColorMatrix();
           colorMatrix.setSaturation(0f);
           ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
           holder.img_restaurant.setColorFilter(filter);


        }
        else if(TextUtils.equals(restaurantModelList.get(position).getIsOpened(),"true")){
           //res opened
        }

        //Event
        holder.setListener(new IRecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int pos) {
                if(TextUtils.equals(restaurantModelList.get(position).getIsOpened(),"true")){
                    Common.currentRestaurant = restaurantModelList.get(pos);
                    Intent resDetailIntent = new Intent(context, RestaurantDetailActivity.class);
                    context.startActivity(resDetailIntent);
                    Bungee.slideLeft(context);
                }
                else {
                    Toast.makeText(context, "Restaurant closed", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }

    @Override
    public int getItemCount() {
        return restaurantModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.txt_restaurant_name)
        TextView txt_restaurant_name;
        @BindView(R.id.txt_res_open_status)
        TextView txt_res_open_status;
        @BindView(R.id.img_restaurant)
        ImageView img_restaurant;

        @BindView(R.id.txt_restaurant_rating_count)
        TextView txt_restaurant_rating_count;

        @BindView(R.id.txt_restaurant_order_count)
        TextView txt_restaurant_order_count;

        @BindView(R.id.txt_res_rating)
        TextView txt_res_rating;
        @BindView(R.id.txt_res_category)
        TextView txt_res_category;

        @BindView(R.id.root_card_view)
        CardView root_card_view;

        @BindView(R.id.txt_res_prepTime)
        TextView txt_res_prepTime;

        @BindView(R.id.txt_res_price_for_two)
        TextView txt_res_price_for_two;


        Unbinder unbinder;

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
        public void onClick(View view) {
            listener.onItemClickListener(view,getAdapterPosition());
        }
    }
}
