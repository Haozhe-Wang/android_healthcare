package com.example.healthcare;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SendByTCP {
    private static String TAG="sending_tcp";
    public static final String SERVER_IP="192.168.161.1";
    public static final int SERVER_PORT=10000;
    public static final String NAME_ACTION="action";
    public static final String ACTION_ACKNOWLEDGE="acknowledge";
    public static String UID = "uid";
    public static String LATESTUPDATETIME = "latestUpdateTime";
    public static void sendToken(final Map<String,String> message){
        try {
            Socket s = new Socket(SERVER_IP, SERVER_PORT);
            PrintWriter pw=new PrintWriter(s.getOutputStream());
            pw.write(new JSONObject(message).toString());
            pw.flush();
            pw.close();
            s.close();
            Log.v(TAG,"Message sent: ");

        }catch (Exception e){
            Log.e(TAG,e.toString());
            e.printStackTrace();
        }
    }
    public static void sendAckToServer(final String uid, final String latestUpdateTime){
        Map<String,String> ack = new HashMap<>();
        ack.put(NAME_ACTION,ACTION_ACKNOWLEDGE);
        ack.put(UID,uid);
        ack.put(LATESTUPDATETIME,latestUpdateTime);
        SendByTCP.sendToken(ack);
    }
    public static void SendTokenToDB(final String UserID){

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId(token) failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Map<String, Object> tokens = new HashMap<>();
                        tokens.put(UserID,token);
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference tokenRef=db.document("tokens/tokens");
                        tokenRef.set(tokens, SetOptions.merge()).addOnFailureListener(new OnFailureListener(){
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document: ", e);
                            }
                        });
                    }
                });

    }
}
