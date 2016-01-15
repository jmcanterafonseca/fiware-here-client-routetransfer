package fiware.routetransfer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;


/**
 *
 *   Simple prototype for route reception and opening Map application
 *
 *
 */
public class MainActivity extends AppCompatActivity {
    private ConnectionListener connectionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.activity = this;
        connectionThread = new ConnectionListener(activityHandler);
        connectionThread.start();
    }


    @Override
    protected void onDestroy() {
        connectionThread.kill();
        super.onDestroy();
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler activityHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == Application.COORDS_MSG) {
                String data = msg.getData().getString(Application.COORDS_KEY);
                String[] destData = data.split(";");
                String originCoords = destData[0];
                String originName = destData[1];
                String destCoords = destData[2];
                String destName = destData[3];

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(Application.activity)
                                .setSmallIcon(R.drawable.car)
                                .setContentTitle("FIWARE-HERE")
                                .setContentText("Click to continue route to " + destName);

                builder.setAutoCancel(true);
                builder.setStyle(new NotificationCompat.InboxStyle());
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                builder.setVibrate(new long[]{1000, 1000});

                int notificationId = destCoords.hashCode();

                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                Intent intent = MapsIntentBuilder.build(originCoords, originName,
                        destCoords, destName);

                PendingIntent notifHandler = PendingIntent.getActivity(
                        Application.activity, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentIntent(notifHandler);

                mNotifyMgr.notify(notificationId, builder.build());
            }
        }
    };
}