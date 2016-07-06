package fr.maiquez.jimi.notifyapp;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by itsjimi on 04/07/16.
 */
public class mService extends Service{

    public static boolean started = false;
    WebSocketClient mWebSocketClient;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("LTM", "onStartCommand");
        started = true;

        try {

            makeWebSocket(new URI("ws://192.168.1.35:3002"), intent);
            mWebSocketClient.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    void makeWebSocket(URI uri, final Intent intent) {
        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("WS", "Opened");
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("act", "connection");
                    obj.put("manufacturer", Build.MANUFACTURER);
                    obj.put("model", Build.MODEL);
                    mWebSocketClient.send(obj.toString());
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(String data) {
                Log.v("WS", data);
                try {
                    JSONObject res = new JSONObject(data);
                    NotificationCompat.Builder mBuilder =
                            (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle(res.getString("title"))
                                    .setContentText(res.getString("subtitle"));
                    //PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), res.getInt("id"), intent, 0);
                    //mBuilder.setContentIntent(pIntent);
                    if (!res.isNull("category")) {
                        mBuilder.setCategory(getCategoryFromString(res.getString("category")));
                    }
                    if (!res.isNull("color")) {
                        mBuilder.setColor(res.getInt("color"));
                    }
                    if (!res.isNull("lights")) {
                        mBuilder.setLights(
                                res.getJSONObject("lights").getInt("color"),
                                res.getJSONObject("lights").getInt("on"),
                                res.getJSONObject("lights").getInt("off")
                        );
                    }
                    if (!res.isNull("ongoing")) {
                        mBuilder.setOngoing(true);
                    }
                    if (!res.isNull("person")) {
                        mBuilder.addPerson(res.getString("person"));
                    }
                    if (!res.isNull("number")) {
                        mBuilder.setNumber(res.getInt("number"));
                    }
                    if (!res.isNull("priority")) {
                        mBuilder.setPriority(res.getInt("priority"));
                    }
                    if(!res.isNull("vibrate")) {
                        mBuilder.setVibrate(new long[] {1, 1, 1});
                    }
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    // mId allows you to update the notification later on.
                    mNotificationManager.notify(res.getInt("id"), mBuilder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("WS", "Closed " + s);
                mWebSocketClient.connect();
            }

            @Override
            public void onError(Exception e) {
                Log.i("WS", "Error " + e.getMessage());
            }
        };
    }

    @TargetApi(Build.VERSION_CODES.M)
    String getCategoryFromString(String s) {
        switch (s.toLowerCase()) {

            case "alarm":
                return Notification.CATEGORY_ALARM;
            case "call":
                return Notification.CATEGORY_CALL;
            case "email":
                return Notification.CATEGORY_EMAIL;
            case "error":
                return Notification.CATEGORY_ERROR;
            case "event":
                return Notification.CATEGORY_EVENT;
            case "message":
                return Notification.CATEGORY_MESSAGE;
            case "progress":
                return Notification.CATEGORY_PROGRESS;
            case "promo":
                return Notification.CATEGORY_PROMO;
            case "recommendation":
                return Notification.CATEGORY_RECOMMENDATION;
            case "reminder":
                return Notification.CATEGORY_REMINDER;
            case "service":
                return Notification.CATEGORY_SERVICE;
            case "social":
                return Notification.CATEGORY_SOCIAL;
            case "status":
                return Notification.CATEGORY_STATUS;
            case "system":
                return Notification.CATEGORY_SYSTEM;
            case "transport":
                return Notification.CATEGORY_TRANSPORT;
        }
        return Notification.CATEGORY_STATUS;
    }

}
