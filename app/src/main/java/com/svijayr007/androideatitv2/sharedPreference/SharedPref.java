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

package com.svijayr007.androideatitv2.sharedPreference;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    public static SharedPreferences getSharePref(Context context){
        return context.getSharedPreferences("status", Context.MODE_PRIVATE);
    }

    public static  SharedPreferences getSharePref(Context context, String name){
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    /********************************************************************************/

    public static String getString(Context context, String key) {
        return getSharePref(context).getString(key, null);
    }

    public static String getString(Context context, String name, String key) {
        return getSharePref(context, name).getString(key, null);
    }

    /*********************************************************/
    public static int getInt(Context context, String key) {
        return getSharePref(context).getInt(key, 0);
    }

    public static int getInt(Context context, String name, String key) {
        return getSharePref(context, name).getInt(key, 0);
    }

    /*********************************************************/
    public static long getLong(Context context, String key) {
        return getSharePref(context).getLong(key, 0);
    }

    public static long getLong(Context context, String name, String key) {
        return getSharePref(context, name).getLong(key, 0);
    }

    /*********************************************************/
    public static Boolean getBoolean(Context context, String key) {
        return getSharePref(context).getBoolean(key, false);
    }




    public static Boolean getBoolean(Context context, String name, String key) {
        return getSharePref(context, name).getBoolean(key, false);
    }

    /********************************************************************************/
    public static void putString(Context context, String key, String value) {
        getSharePref(context).edit().putString(key, value).apply();
    }

    public static void putString(Context context, String name, String key, String value) {
        getSharePref(context, name).edit().putString(key, value).apply();
    }

    /*********************************************************/

    public static void putInt(Context context, String key, int value) {
        getSharePref(context).edit().putInt(key, value).apply();
    }

    public static void putInt(Context context, String name, String key, int value) {
        getSharePref(context, name).edit().putInt(key, value).apply();
    }

    /*********************************************************/

    public static void putLong(Context context, String key, long value) {
        getSharePref(context).edit().putLong(key, value).apply();
    }

    public static void putLong(Context context, String name, String key, long value) {
        getSharePref(context, name).edit().putLong(key, value).apply();
    }

    /*********************************************************/
    public static void putBoolean(Context context, String key, Boolean value) {
        getSharePref(context).edit().putBoolean(key, value).apply();
    }

    public static void putBoolean(Context context, String name, String key, Boolean value) {
        getSharePref(context, name).edit().putBoolean(key, value).apply();
    }

    /********************************************************************************/

    public static void remove(Context context, String key) {
        getSharePref(context).edit().remove(key).apply();
    }

    public static void remove(Context context, String name, String key) {
        getSharePref(context, name).edit().remove(key).apply();
    }

    /*********************************************************/

    public static void removeAll(Context context) {
        getSharePref(context).edit().clear().apply();
    }

    public static void removeAll(Context context, String name) {
        getSharePref(context, name).edit().clear().apply();
    }




}
