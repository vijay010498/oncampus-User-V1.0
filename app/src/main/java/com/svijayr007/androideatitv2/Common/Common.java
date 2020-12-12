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

package com.svijayr007.androideatitv2.Common;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.svijayr007.androideatitv2.BuildConfig;
import com.svijayr007.androideatitv2.Model.BlockScreenModel;
import com.svijayr007.androideatitv2.Model.CategoryModel;
import com.svijayr007.androideatitv2.Model.DeliveryOrderModel;
import com.svijayr007.androideatitv2.Model.FoodModel;
import com.svijayr007.androideatitv2.Model.OrderModel;
import com.svijayr007.androideatitv2.Model.RestaurantModel;
import com.svijayr007.androideatitv2.Model.TokenModel;
import com.svijayr007.androideatitv2.Model.UserModel;
import com.svijayr007.androideatitv2.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Common  {
    public static final String USER_REFERENCES =  "Users";
    public static final String POPULAR_CATEGORIES_REFERENCE = "MostPopular" ;
    public static final String BEST_DEALS_REF = "BestDeals";
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String CATEGORY_REF = "Category";
    public static final String COMMENT_REF = "Comments";
    public static final String ORDER_REF = "Orders";
    public static final String NOTI_TITLE = "title";
    public static final String NOTI_CONTENT = "body";
    public static final String IS_SUBSCRIBE_NEWS = "IS_SUBSCRIBE_NEWS";
    public static final String NEWS_TOPIC = "news";
    public static final String RESTAURANT_REF = "Restaurant";
    public static final String SUGGESTIONS_REF = "suggestions";
    public static  final String REFUND_REF = "Refunds";


    /************************************************************************/

    //block screen
    public static final String BLOCK_SCREEN_REF = "block_screen";
    public static final String CAMPUS_REF = "campus";
    public static BlockScreenModel blockScreenModel;

    private static final String TOKEN_REF = "Tokens" ;
    public static final String IS_SEND_IMAGE = "IS_SEND_IMAGE";
    public static final String IMAGE_URL = "IMAGE_URL";
    public static UserModel currentUser ;
    public static String authorizeKey = "";
    public static String ONCAMPUS_PERCENTAGE_REF = "onCampusPercentage";
    public static final String DELIVERY_ORDER_REF = "deliveryOrders";
    public static DeliveryOrderModel currentDeliveryOrder;
    public static RestaurantModel currentRestaurant;



    //FCM TOPICS
    public static String globalTopic = "Global";
    public static  String globalClientTopic = "Global_client";

    //current token
    public  static String  userToken = "";



    //Rating Order model
    public static OrderModel currentRatingOrder;

    //Order Detail activity
    public static  OrderModel currentDetailOrder;




    //urls
    public static final String onCampusPrivacy = "https://oncampus.in/privacy.html";
    public  static  final String onCampusWebsite = "https://oncampus.in/";
    public static final String onCampusHelpCenter = "https://oncampus.customerly.help/";

    //Database URLS
    public static  final String usersDB = "https://eatitv2-75508-usersdb.firebaseio.com/";
    public static final String serverValues = "https://eatitv2-75508-servervalues.firebaseio.com/";
    public static final String suggestionsDB = "https://eatitv2-75508-suggestions.firebaseio.com/";
    public static final String allTokensDB  = "https://eatitv2-75508-alltokens.firebaseio.com/";
    public static final String deliveryAgentsDB = "https://eatitv2-75508-deliveryagents.firebaseio.com/";
    public static final String deliveryOrdersDB = "https://eatitv2-75508-deliveryorders.firebaseio.com/";
    public static final String refundDB = "https://eatitv2-75508-refund.firebaseio.com/";
    public static final String ordersDB = "https://eatitv2-75508-orders.firebaseio.com/";
    public static final String restaurantDB = "https://eatitv2-75508-restaurants.firebaseio.com/";



    /************************************************************************/
    //Check if access is blocked
    public static  void  checkIsAccessBlocked(){
        //check from firebase
        FirebaseDatabase.getInstance(Common.serverValues)
                .getReference(Common.BLOCK_SCREEN_REF)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Log.i("EXISTS","EXISTS");
                            Common.blockScreenModel = snapshot.getValue(BlockScreenModel.class);
                            Log.i("OVERALL",String.valueOf(Common.blockScreenModel.isOverall()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

    }


    /************************************************************************/



    /************************************************************************/

    public static String formatPrice(double displayPrice) {
        if(displayPrice != 0){
            DecimalFormat df = new DecimalFormat("#,##0.00");
            df.setRoundingMode(RoundingMode.UP);
            String finalPrice = new StringBuilder(df.format(displayPrice)).toString();
            return  finalPrice.replace(".",".");
        }
        else
            return "0.00";
    }
    /************************************************************************/

    /************************************************************************/

    public static String createOrderNumber() {
        return  new StringBuilder()
                .append(System.currentTimeMillis()) // current time in millisecond
                .append(Math.abs(new Random().nextInt(999999))) //  Add random number to block same order at same time
                .toString();
    }
    /************************************************************************/



    public static String buildToken(String authorizeKey) {
        return new StringBuilder("Bearer").append(" ").append(authorizeKey).toString();
    }
    /************************************************************************/

    public static String getDateOfWeek(int i) {
        switch (i){
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
            default:
                return "Unknown";

        }
    }
    /************************************************************************/

    public static String convertStatusToText(int orderStatus) {
        switch (orderStatus){
            case -2:
                return "Ready for pickup";
            case -1:
                return "Cancelled by restaurant";
            case -3:
                return "Cancelled by you";
            case 0:
                return "Placed, Waiting for the confirmation";
            case 1:
                return "Order confirmed, preparing food";
            case 2:
                return "on the way";
            case 3:
                return "Order delivered";
            default:
                return "Waiting for the update";
        }
    }
    /************************************************************************/

    public static void showNotification(Context context, int id, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        final Uri NOTIFICATION_SOUND_URI = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + "/" + R.raw.oncampus_notification);
        if(intent != null)
            pendingIntent = PendingIntent.getActivity(context,id,intent,pendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "onCampus";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //greater than oreo
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "onCampus",NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("onCampus");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.setVibrationPattern(new long[] {0,1000,500,1000});
            notificationChannel.enableVibration(true);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                                .build();
            notificationChannel.setSound(NOTIFICATION_SOUND_URI,audioAttributes);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.colorPrimaryDark))
                .setSmallIcon(R.mipmap.oncampus_logo_green_white_bg_512)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.oncampus_logo_green_white_bg_512));
        if(pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id,notification);

    }
    /************************************************************************/
    /************************************************************************/

    public static void showNotificationBigStyle(Context context, int id, String title, String content, Bitmap bitmap, Intent intent)
    {
        PendingIntent pendingIntent = null;
        if(intent != null)
            pendingIntent = PendingIntent.getActivity(context,id,intent,pendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "onCampus";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //greater than oreo
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "onCampus",NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("onCampus");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.setVibrationPattern(new long[] {0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.oncampus_logo_green_white_bg_512)
                .setLargeIcon(bitmap)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));

        if(pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id,notification);

    }

    /************************************************************************/

    public static void updateToken(Context context, String newToken) {
      if(Common.currentUser != null){
          userToken = newToken;
          Map<String, Object> updateToken = new HashMap<>();
          updateToken.put("token",newToken);
          FirebaseDatabase.getInstance(Common.allTokensDB)
                  .getReference(Common.TOKEN_REF)
                  .child(currentUser.getUid())
                  .updateChildren(updateToken)
                  .addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                          Toast.makeText(context, "Update Token "+e.getMessage(), Toast.LENGTH_SHORT).show();

                      }
                  }).addOnCompleteListener(new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                  if(task.isSuccessful()){
                      Log.i("TOKEN","Token Updated");
                  }

              }
          });
      }
    }

    public static void createToken(Context context, String token) {
        //Create token for the very first time -- Sign up Activity
        if(Common.currentUser != null){
            userToken = token;
            FirebaseDatabase.getInstance(Common.allTokensDB)
                    .getReference(Common.TOKEN_REF)
                    .child(currentUser.getUid())
                    .setValue(new TokenModel(currentUser.getPhone(),token,currentUser.getUid()))
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Create Token"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.i("Token","Token Created");
                    }
                }
            });
        }

    }

    /************************************************************************/



    public static void updateLastVisitedTime(Context context) {
        if(Common.currentUser != null){
            Map<String, Object> updateTime = new HashMap<>();
            updateTime.put("lastVisited",System.currentTimeMillis());
            FirebaseDatabase.getInstance(Common.usersDB)
                    .getReference(Common.USER_REFERENCES)
                    .child(currentUser.getUid())
                    .updateChildren(updateTime)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Visited Time"+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.i("VISITED TIME","Last visited updated");
                    }

                }
            });
        }

    }


    //New order FCM to each restaurant
    /************************************************************************/

    public static String createOrderTopic(String restaurantId) {
        return new StringBuilder("/topics/")
                .append(restaurantId)
                .append("_")
                .append("new_order").toString();
    }

    //News topic
    /************************************************************************/
    public static  String createTopicNews(){
        return new StringBuilder("/topics/")
                .append(Common.currentRestaurant.getId())
                .append("_")
                .append("news")
                .toString();

    }



    /************************************************************************/





    public static String generateOrderOTP(){
        return new DecimalFormat("000000").format(new Random().nextInt(999999));
    }




    /************************************************************************/
    public static FoodModel findFoodInListById(CategoryModel categoryModel, String foodId) {
        if(categoryModel.getFoods() != null && categoryModel.getFoods().size() >0)
        {
            for(FoodModel foodModel:categoryModel.getFoods())
                if(foodModel.getId().equals(foodId))
                    return foodModel;
                
                return null;

        }
        else
            return null;
    }



    //checks for internet connectivity
    public static  boolean isInternetAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return !(activeNetworkInfo != null && activeNetworkInfo.isConnected());
        }
        return false;
    }
    /************************************************************************/

    //custom browser
    public static void openCustomBrowser(Context context, String url){
        try{
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            customTabsIntent.launchUrl(context, Uri.parse(url));
            builder.setToolbarColor(context.getResources().getColor(R.color.pageColor2));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /************************************************************************/

    //Hides keyboard
    public static void hideKeyboard(Activity activity){
        try{
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view  = activity.getCurrentFocus();
            if(view == null){
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    /************************************************************************/
}
