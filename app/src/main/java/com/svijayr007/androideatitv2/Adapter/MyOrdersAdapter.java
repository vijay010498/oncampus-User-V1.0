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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.svijayr007.androideatitv2.Common.Common;
import com.svijayr007.androideatitv2.Model.OrderModel;
import com.svijayr007.androideatitv2.R;
import com.svijayr007.androideatitv2.callback.IRecyclerClickListener;
import com.svijayr007.androideatitv2.ui.orderRating.RatingActivity;
import com.svijayr007.androideatitv2.ui.order_detail.OrderDetailActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import spencerstudios.com.bungeelib.Bungee;

public class MyOrdersAdapter extends  RecyclerView.Adapter<MyOrdersAdapter.MyViewHolder> {

    private Context context;
    private List<OrderModel> orderModelList;
    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;
    private KProgressHUD hud;


    public MyOrdersAdapter(Context context, List<OrderModel> orderModelList) {
        this.context = context;
        this.orderModelList = orderModelList;
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark))
                .setCancellable(false)
                .setLabel("Loading")
                .setAnimationSpeed(1)
                .setDimAmount(0.5f);
    }

    public OrderModel getItemAtPosition(int pos){
        return orderModelList.get(pos);
    }
    public void setItemAtPosition(int pos, OrderModel item){
        orderModelList.set(pos,item);
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.layout_order_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        /*****************************************************************************************/
        //SET DATA

        //Restaurant Name

        holder.txt_restaurant.setText(new StringBuilder().append(orderModelList.get(position).getRestaurantName()).append(" "));

        //Restaurant address
        holder.txt_restaurant_address.setText(new StringBuilder().append(orderModelList.get(position).getRestaurantAddress()).append(" "));


        //Order amount
        holder.txt_order_amount.setText(new StringBuilder("₹").append(orderModelList.get(position).getTotalPayment()).append(">"));

        //order_items
        int numberOfItems = orderModelList.get(position).getCartItemList().size();
        //item count
        holder.txt_item_count.setText(new StringBuilder()
        .append(numberOfItems)
        .append(" ITEM(s)"));
        holder.txt_order_items.setText("");
        if(numberOfItems <=4 ) {
            for (int i = 0; i < numberOfItems; i++) {
                //Log.i("ITEM NAME",orderModelList.get(position).getCartItemList().get(i).getFoodName());
                holder.txt_order_items.append(new StringBuilder()
                        .append(orderModelList.get(position).getCartItemList().get(i).getFoodName())
                        .append(" x ")
                        .append(orderModelList.get(position).getCartItemList().get(i).getFoodQuantity()));

                if (i + 1 < numberOfItems)
                    holder.txt_order_items.append(",\n");
                else
                    holder.txt_order_items.append(".");
            }
        }
        else if(numberOfItems > 4){
            for(int i=0;i<4;i++){
                holder.txt_order_items.append(new StringBuilder()
                        .append(orderModelList.get(position).getCartItemList().get(i).getFoodName())
                        .append(" x ")
                        .append(orderModelList.get(position).getCartItemList().get(i).getFoodQuantity()));

                if (i + 1 < 4)
                    holder.txt_order_items.append(",\n");

            }
            holder.txt_order_items.append(" ...see more>");
        }

        //Order type
        if(orderModelList.get(position).isPickup()){
            holder.txt_order_type.setText(new StringBuilder()
            .append(" Pickup "));
        }
        else {
            holder.txt_order_type.setText(new StringBuilder()
            .append(" Delivery "));
        }

        //Order Date
        calendar.setTimeInMillis(orderModelList.get(position).getCreateDate());
        Date date = new Date(orderModelList.get(position).getCreateDate());
        holder.txt_order_date.setText(new StringBuilder(Common.getDateOfWeek(calendar.get(Calendar.DAY_OF_WEEK)))
        .append(" ").append(simpleDateFormat.format(date)));


        /*****************************************************************************************/

        //Hide reorder and rate order
        if(orderModelList.get(position).getOrderStatus() == 0){
            holder.order_payment_success_text.setText("Payment Success");
            holder.rating_display_layout.setVisibility(View.GONE);
            holder.rate_layout.setVisibility(View.GONE);
        }
        else if(orderModelList.get(position).getOrderStatus() == 1){
            holder.order_payment_success_text.setText("Confirmed");
            holder.rating_display_layout.setVisibility(View.GONE);
            holder.rate_layout.setVisibility(View.GONE);
        }
        else if(orderModelList.get(position).getOrderStatus() == 2){
            holder.order_payment_success_text.setText("on the way");
            holder.rating_display_layout.setVisibility(View.GONE);
            holder.rate_layout.setVisibility(View.GONE);
        }
        else if(orderModelList.get(position).getOrderStatus() == -2){
            //Ready for pickup
            holder.order_payment_success_text.setText("Ready for pickup");
            holder.rating_display_layout.setVisibility(View.GONE);
            holder.rate_layout.setVisibility(View.GONE);
        }
        else if(orderModelList.get(position).getOrderStatus() == -1){
            //Order cancelled refund process
            holder.order_payment_success_text.setText("CANCELLED");
            holder.order_payment_success_text.setCompoundDrawablesRelative(null,null,null,null);
            holder.rating_display_layout.setVisibility(View.GONE);
            holder.rate_layout.setVisibility(View.GONE);
            holder.div_line.setVisibility(View.GONE);

        }
        else if(orderModelList.get(position).getOrderStatus() == -3){
            holder.order_payment_success_text.setText("CANCELLED");
            holder.order_payment_success_text.setCompoundDrawablesRelative(null,null,null,null);
            holder.rating_display_layout.setVisibility(View.GONE);
            holder.rate_layout.setVisibility(View.GONE);
            holder.div_line.setVisibility(View.GONE);
        }
        else if(orderModelList.get(position).getOrderStatus() == 3){

            //Order delivered status update
            holder.rate_layout.setVisibility(View.VISIBLE);
            if(!orderModelList.get(position).isPickup())
                holder.order_payment_success_text.setText(new StringBuilder("Delivered"));
            else
                holder.order_payment_success_text.setText(new StringBuilder("Picked Up"));

            holder.rating_display_layout.setVisibility(View.VISIBLE);
            //Check and set order rating value
            if(orderModelList.get(position).getRatingValue() == -1.0 || orderModelList.get(position).getRatingValue() == -1){
                holder.txt_rateOrder.setVisibility(View.VISIBLE);
                holder.rating_value.setText(new StringBuilder()
                .append("You haven't rated ")
                .append("\n")
                .append("this order yet"));
                holder.rating_star.setVisibility(View.GONE);
            }
            else {
                holder.rating_star.setVisibility(View.VISIBLE);
                holder.rating_value.setText(new StringBuilder()
                .append(orderModelList.get(position).getRatingValue()));
                holder.txt_rateOrder.setVisibility(View.GONE);
            }
        }

        /*****************************************************************************************/
        //Rate order clicked
        holder.txt_rateOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.currentRatingOrder = orderModelList.get(position);
                Intent rateOrderIntent = new Intent(context, RatingActivity.class);
                context.startActivity(rateOrderIntent);
                Bungee.slideLeft(context);

            }
        });

        /*****************************************************************************************/


        //Order click handle
        holder.setiRecyclerClickListener(new IRecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int pos) {
                //Toast.makeText(context, "Order Clicked", Toast.LENGTH_SHORT).show();
                //showDialog(orderModelList.get(pos).getCartItemList());
                Common.currentDetailOrder = orderModelList.get(pos);
                Intent orderDetailIntent = new Intent(context, OrderDetailActivity.class);
                ((Activity)context).finish();
                context.startActivity(orderDetailIntent);
                Bungee.slideLeft(context);
            }
        });

        /*****************************************************************************************/
    }




    @Override
    public int getItemCount() {
        return orderModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //New Layout file
        @BindView(R.id.txt_restaurant)
        TextView txt_restaurant;

        @BindView(R.id.order_payment_success_text)
        TextView order_payment_success_text;

        @BindView(R.id.txt_restaurant_address)
        TextView txt_restaurant_address;

        @BindView(R.id.txt_order_amount)
        TextView  txt_order_amount;

        @BindView(R.id.txt_order_items)
        TextView txt_order_items;

        @BindView(R.id.txt_order_date)
        TextView txt_order_date;


        @BindView(R.id.txt_rateOrder)
        TextView txt_rateOrder;

        @BindView(R.id.rate_layout)
        LinearLayout rate_layout;

        @BindView(R.id.rating_display_layout)
        LinearLayout rating_display_layout;

        @BindView(R.id.rating_value)
        TextView rating_value;

        @BindView(R.id.rating_star)
        ImageView rating_star;

        @BindView(R.id.txt_order_type)
        TextView txt_order_type;

        @BindView(R.id.txt_item_count)
        TextView txt_item_count;

        @BindView(R.id.div_line)
        View div_line;

        Unbinder unbinder;
        IRecyclerClickListener iRecyclerClickListener;

        public void setiRecyclerClickListener(IRecyclerClickListener iRecyclerClickListener) {
            this.iRecyclerClickListener = iRecyclerClickListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerClickListener.onItemClickListener(view,getAdapterPosition());


        }
    }
}
