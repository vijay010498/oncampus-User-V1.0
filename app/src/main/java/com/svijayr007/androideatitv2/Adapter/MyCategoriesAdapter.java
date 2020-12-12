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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.svijayr007.androideatitv2.Common.Common;
import com.svijayr007.androideatitv2.Model.CategoryModel;
import com.svijayr007.androideatitv2.R;
import com.svijayr007.androideatitv2.callback.IRecyclerClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyCategoriesAdapter  extends RecyclerView.Adapter<MyCategoriesAdapter.MyViewHolder> {
    Context context;
    List<CategoryModel> categoryModelList;
    MyFoodListAdapter adapter;


    public MyCategoriesAdapter(Context context, List<CategoryModel> categoryModelList) {
        this.context = context;
        this.categoryModelList = categoryModelList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.layout_category_items, parent, false));



    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
                setUiDataNormal(holder, position);
                
    }
    private void setUiDataNormal(MyViewHolder holder, int position) {
        //Get food list
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
            gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (adapter != null) {
                        switch (adapter.getItemViewType(position)) {
                            case Common.DEFAULT_COLUMN_COUNT:
                                return 1;
                            case Common.FULL_WIDTH_COLUMN:
                                return 2;
                            default:
                                return -1;
                        }
                    }
                    return -1;
                }
            });
            adapter = new MyFoodListAdapter(context, categoryModelList.get(position).getFoods());
            holder.menu_items_recycler.setAdapter(adapter);
            holder.menu_items_recycler.setLayoutManager(gridLayoutManager);
            holder.menu_items_recycler.setHasFixedSize(true);


            Glide.with(context)
                    .load(categoryModelList.get(position).getImage())
                    .placeholder(R.drawable.ic_restaurant_menu_green_24dp)
                    .into(holder.category_image);
            holder.category_name.setText(new StringBuilder(categoryModelList.get(position).getName()));

            holder.category_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "" + categoryModelList.get(position).getName() + " " + holder.getAdapterPosition(), Toast.LENGTH_SHORT).show();
                }
            });

            //Event
            holder.setListener((view, pos) -> {

            });

    }

    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }


    public class MyViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;
        @BindView(R.id.txt_category)
        TextView category_name;
        @BindView(R.id.category_image)
        CircleImageView category_image;
        @BindView(R.id.menu_items_recycler)
        RecyclerView menu_items_recycler;
        IRecyclerClickListener listener;

        @BindView(R.id.root_layout_category)
        LinearLayout root_layout_category;

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
            listener.onItemClickListener(v,getAdapterPosition());
        }
    }

    @Override
    public int getItemViewType(int position) {

        if(categoryModelList.size() == 1)
            return Common.DEFAULT_COLUMN_COUNT;
        else {
            if(categoryModelList.size() % 2 == 0)
                return Common.DEFAULT_COLUMN_COUNT;
            else
                return (position > 1 && position == categoryModelList.size()-1) ? Common.FULL_WIDTH_COLUMN:Common.DEFAULT_COLUMN_COUNT;
        }
    }
}
