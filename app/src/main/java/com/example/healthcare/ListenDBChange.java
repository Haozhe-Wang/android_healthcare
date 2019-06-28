package com.example.healthcare;

import android.app.Service;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ListenDBChange extends FirebaseMessagingService {
    private static final String TAG = "firebase_change_service";
    public static final String BROADCAST_ACTION_UPDATE = "UPDATE_DASHBOARD";
    public static final String BROADCAST_ACTION_NEW_TOKEN = "NEW_TOKEN";
    public static final String ACTION="action";
    public static final String ACTION_REGISTER="register";
    public static final String ACTION_ACKNOWLEDGE="acknowledge";
    public static final String TOKEN="token";

    private LocalBroadcastManager broadcaster;



    public ListenDBChange() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String,String> message = remoteMessage.getData();
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (message.containsKey("updated_patient")){
            String patientUID = message.get("updated_patient");
            Intent intent = new Intent(BROADCAST_ACTION_UPDATE);
            intent.putExtra("patientUID", patientUID);
            broadcaster.sendBroadcast(intent);
            /*
            * TODO put the code after fetched user data from database
            try {
                Log.v(TAG,"connecting");
                Socket s = new Socket("192.168.161.1", 10000);
                Log.v(TAG,"connect212");
                PrintWriter pw=new PrintWriter(s.getOutputStream());
                Map<String,String> ackMessage = new HashMap<>();
                message.put("1","2");
                pw.write("123");
                pw.flush();
                pw.close();
                s.close();
                Log.v(TAG,"connected");
            }catch (Exception e){
                Log.v(TAG,e.toString());
                e.printStackTrace();
            }*/
        }

    }

    @Override
    public void onNewToken(String token) {
        Log.v(TAG,"Received new token: "+token);
        broadcaster.sendBroadcast(new Intent(BROADCAST_ACTION_NEW_TOKEN));
//        Map<String,String> registerMessage = new HashMap<>();
//        registerMessage.put(ACTION,ACTION_REGISTER);
//        registerMessage.put(TOKEN,token);
//        SendByTCP.sendToken(registerMessage);
    }

}
