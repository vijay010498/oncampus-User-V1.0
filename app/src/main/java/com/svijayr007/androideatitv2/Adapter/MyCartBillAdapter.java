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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.svijayr007.androideatitv2.Database.CartItem;
import com.svijayr007.androideatitv2.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCartBillAdapter extends RecyclerView.Adapter<MyCartBillAdapter.MyViewHolder> {
    Context context;
    List<CartItem> cartItemList;
    Gson gson;




    public MyCartBillAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.gson = new Gson();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_cart_item_bill, parent, false));


    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final int[] quantity = {cartItemList.get(position).getFoodQuantity()};

        //New cart item layout
        Glide.with(context)
                .load(cartItemList.get(position).getFoodImage())
                .placeholder(R.drawable.ic_restaurant)
                .into(holder.cart_food_image);
        holder.cart_food_name.setText(new StringBuilder()
                .append(cartItemList.get(position).getFoodName())
                .append(" X ")
            .append(cartItemList.get(position).getFoodQuantity()));
        holder.cart_food_price.setText(new StringBuilder("₹")
                .append(cartItemList.get(position).getFoodPrice() * cartItemList.get(position).getFoodQuantity()));

        //veg non-veg image
        //default veg
        if(cartItemList.get(position).getIsVeg() == 0){
            holder.image_veg_non_veg.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_non_veg));
        }
        else if(cartItemList.get(position).getIsVeg() == 1){
            holder.image_veg_non_veg.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_veg));
        }
        else {
            holder.image_veg_non_veg.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_non_veg));
        }

    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public CartItem getItemAtPosition(int pos) {
        return  cartItemList.get(pos);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private Unbinder unbinder;


        //New cart item design
        @BindView(R.id.cart_food_image)
        ImageView cart_food_image;
        @BindView(R.id.cart_food_name)
        TextView cart_food_name;
        @BindView(R.id.cart_food_price)
        TextView cart_food_price;

        @BindView(R.id.image_veg_non_veg)
        ImageView image_veg_non_veg;





        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
