package net.macdidi5.picomfire;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.utilities.Base64;

import java.io.IOException;

public class ListenService extends Service {

    private boolean isConnect = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (TurtleUtil.checkNetwork(this)) {
            processListen();
        }
        else {
            Log.d("ListenService", "onStartCommand: Connection required.");
        }

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ListenServiceBinder();
    }

    public class ListenServiceBinder extends Binder {
        public ListenService getListenService() {
            return ListenService.this;
        }
    }

    public void processListen() {
        String appUrl = TurtleUtil.getPref(this, TurtleUtil.KEY_APP_URL, null);

        if (isConnect || appUrl == null) {
            return;
        }

        isConnect = true;

        final Firebase firebaseRef, controlRef;
        Firebase[] gpios;
        int length = PiGPIO.length();
        firebaseRef = new Firebase(
                TurtleUtil.getPref(this, TurtleUtil.KEY_APP_URL, ""));
        controlRef = firebaseRef.child(MainActivity.CHILD_CONTROL);

        ValueEventListener listener = new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot ds) {
                final String gpioName = PiGPIO.valueOf(ds.getKey()).getName();
                final CommanderItem item = MainActivity.getCommanderItem(
                        TurtleUtil.getListeners(ListenService.this), gpioName);

                if (item != null) {
                    item.setStatus((Boolean) ds.getValue());

                    if ((item.isStatus() && item.isHighNotify()) ||
                            (!item.isStatus() && item.isLowNotify())) {

                        Firebase imageRef = firebaseRef.child(MainActivity.CHILD_IMAGE);

                        imageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                String imageStr = (String) snapshot.getValue();

                                if (imageStr == null) {
                                    processNotify(null, item, gpioName);
                                    return;
                                }

                                try {
                                    byte[] imageByte = Base64.decode(imageStr);
                                    Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                                    processNotify(imageBitmap, item, gpioName);
                                }
                                catch (IOException e) {
                                    Log.d("ListenService", e.toString());
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                Log.d("ListenService", firebaseError.toString());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError fe) {
                Log.d("ListenService", fe.toString());
            }
        };

        gpios = new Firebase[length];

        for (int i = 0; i < length; i++) {
            gpios[i] = controlRef.child(PiGPIO.fromOrdinal(i).name());
            gpios[i].addValueEventListener(listener);
        }
    }

    private void processNotify(Bitmap bigPicture, CommanderItem item, String gpioName) {
        if (bigPicture == null) {
            bigPicture = BitmapFactory.decodeResource(
                    getResources(), R.drawable.notify_big_picture);
        }

        String nm = item.isStatus() ?
                item.getHighDesc() :
                item.getLowDesc();
        nm = item.getDesc() + ":" + nm;

        Notification.Builder builder = new Notification.Builder(ListenService.this);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(ListenService.this, 0, new Intent(), 0);

        builder.setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setTicker(ListenService.this.getString(R.string.app_name))
                .setContentTitle(ListenService.this.getString(R.string.app_name))
                .setContentText(nm)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        Notification.BigPictureStyle bigPictureStyle =
                new Notification.BigPictureStyle();
        bigPictureStyle.bigPicture(bigPicture)
                .setSummaryText(nm);
        builder.setStyle(bigPictureStyle);

        NotificationManager manager =(NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        manager.notify(gpioName, 0, notification);
    }

}
