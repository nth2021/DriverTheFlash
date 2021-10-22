package com.example.androidtheflash.Services;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.Random;

import com.example.androidtheflash.Common;
import com.example.androidtheflash.Utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            UserUtils.updateToken(this,s);
        }

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String,String> dataREcv = remoteMessage.getData();
        if(dataREcv !=null){
            Common.showNotification(this,new Random().nextInt(),
            dataREcv.get(Common.NOTI_TITTLE),
            dataREcv.get(Common.NOTI_CONTENT),
            null);
        }
    }
}
