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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.svijayr007.androideatitv2.EventBus.CampusSelectedEvent;
import com.svijayr007.androideatitv2.Model.CampusModel;
import com.svijayr007.androideatitv2.R;
import com.svijayr007.androideatitv2.callback.IRecyclerClickListener;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyCampusAdapter extends RecyclerView.Adapter<MyCampusAdapter.MyViewHolder>{
    Context context;
    List<CampusModel> campusModelList;

    public MyCampusAdapter(Context context, List<CampusModel> campusModelList) {
        this.context = context;
        this.campusModelList = campusModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return  new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_campus_items,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context)
                .load(campusModelList.get(position).getImageUrl())
                .placeholder(R.drawable.ic_restaurant)
                .into(holder.campus_image);
        holder.campus_name.setText(new StringBuilder()
        .append(campusModelList.get(position).getName()));
        holder.campus_address.setText(new StringBuilder()
        .append(campusModelList.get(position).getAddress()));
        holder.campus_no_of_restaurants.setText(new StringBuilder()
        .append(campusModelList.get(position).getNumber_of_restaurants())
        .append(" Restaurants"));

        holder.setListener((view, pos) ->{
            EventBus.getDefault().post(new CampusSelectedEvent(campusModelList.get(pos)));
        });


    }

    @Override
    public int getItemCount() {
        return campusModelList.size();
    }

    public class  MyViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;
        @BindView(R.id.campus_image)
        CircleImageView campus_image;
        @BindView(R.id.campus_name)
        TextView campus_name;
        @BindView(R.id.campus_address)
        TextView campus_address;
        @BindView(R.id.campus_no_of_restaurants)
        TextView campus_no_of_restaurants;


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
