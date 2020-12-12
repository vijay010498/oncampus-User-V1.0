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

package com.svijayr007.androideatitv2.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.svijayr007.androideatitv2.Common.Common;
import com.svijayr007.androideatitv2.R;

public class LoginActivity extends AppCompatActivity {


    private FirebaseAnalytics mFirebaseAnalytics;
    private MaterialButton login_Button;
    private TextView phone_number_edt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        init();

    }

    private void init() {
        login_Button = findViewById(R.id.button_login);
        phone_number_edt = findViewById(R.id.edit_phone_login);
        setListener();

}

    private void setListener() {
        phone_number_edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                if(phone_number_edt.getText().toString().length() == 10) {
                    if(TextUtils.isDigitsOnly(phone_number_edt.getText().toString())) {
                        Common.hideKeyboard(LoginActivity.this);
                        String phone_number = phone_number_edt.getText().toString();
                            Intent otpIntent = new Intent(LoginActivity.this,OtpActivity.class);
                            otpIntent.putExtra("CUSTOMER_MOBILE","+91"+phone_number);
                            startActivity(otpIntent);
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Invalid Number", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        login_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone_number = phone_number_edt.getText().toString();
                if(!phone_number.isEmpty() && phone_number.length()==10 && TextUtils.isDigitsOnly(phone_number_edt.getText().toString())){
                    Intent otpIntent = new Intent(LoginActivity.this,OtpActivity.class);
                    otpIntent.putExtra("CUSTOMER_MOBILE","+91"+phone_number);
                    startActivity(otpIntent);
                }else {
                    Toast.makeText(LoginActivity.this, "Invalid Phone Number!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}