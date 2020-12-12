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

package com.svijayr007.androideatitv2.ui.signup;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.svijayr007.androideatitv2.Common.Common;
import com.svijayr007.androideatitv2.Model.CampusModel;
import com.svijayr007.androideatitv2.callback.ICampusCallbackListener;

import java.util.ArrayList;
import java.util.List;

public class CampusViewModel extends ViewModel implements ICampusCallbackListener {
    private MutableLiveData<List<CampusModel>> campusListMutable;
    private MutableLiveData<String> messageError = new MutableLiveData<>();

    private ICampusCallbackListener listener;

    public CampusViewModel() {
        listener = this;
    }

    public MutableLiveData<List<CampusModel>> getCampusListMutable() {
        if(campusListMutable == null){
            campusListMutable = new MutableLiveData<>();
            loadAllCampusFromFirebase();
        }
        return campusListMutable;
    }

    private void loadAllCampusFromFirebase() {
        List<CampusModel> campusModels = new ArrayList<>();
        DatabaseReference campusRef = FirebaseDatabase.getInstance(Common.serverValues)
                .getReference(Common.CAMPUS_REF);
        campusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot campusSnap : dataSnapshot.getChildren()){
                        CampusModel campusModel = campusSnap.getValue(CampusModel.class);
                        Log.i("CAMPUS CHECK",campusModel.getName());
                        campusModels.add(campusModel);
                    }
                    if(campusModels.size() > 0)
                        listener.onCampusLoadSuccess(campusModels);
                    else
                        listener.onCampusLoadFailed("Campus List Empty");

                }else {
                    listener.onCampusLoadFailed("No Campus Found on Server!");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onCampusLoadSuccess(List<CampusModel> campusModelList) {
        campusListMutable.setValue(campusModelList);

    }

    @Override
    public void onCampusLoadFailed(String message) {
        messageError.setValue(message);

    }
}
