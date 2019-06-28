package com.example.healthcare;

import android.util.Log;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class SendByTCP {
    private static String TAG="sending_tcp";
    public static final String SERVER_IP="192.168.161.1";
    public static final int SERVER_PORT=10000;
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
}
