package tw.firemaples.onscreenocr;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import tw.firemaples.onscreenocr.event.EventUtil;
import tw.firemaples.onscreenocr.event.events.ShowingStateChanged;
import tw.firemaples.onscreenocr.floatings.FloatingView;
import tw.firemaples.onscreenocr.floatings.screencrop.MainBar;
import tw.firemaples.onscreenocr.receivers.SamsungSpenInsertedReceiver;
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigUtil;
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler;
import tw.firemaples.onscreenocr.utils.SettingUtil;
import tw.firemaples.onscreenocr.utils.Utils;

/**
 * Created by firemaples on 21/10/2016.
 */

public class ScreenTranslatorService extends Service {
    private static final int ONGOING_NOTIFICATION_ID = 12345;

    private static final Logger logger = LoggerFactory.getLogger(ScreenTranslatorService.class);

    @SuppressLint("StaticFieldLeak")
    private static ScreenTranslatorService _instance;

    private NotificationManager notificationManager;

    private SettingUtil spUtil;

    private FloatingView mainFloatingView;
    private boolean dismissNotify = false;

    public ScreenTranslatorService() {
        _instance = this;
    }

    public static Context getContext() {
        if (_instance != null) {
            return _instance;
        }
        return null;
    }

    public static boolean isRunning() {
        return Utils.isServiceRunning(ScreenTranslatorService.class) && _instance != null;
    }

    public static void start(Context context, boolean showFloatingView) {
        if (!isRunning()) {
            ContextCompat.startForegroundService(context, new Intent(context, ScreenTranslatorService.class));
        } else if (_instance != null) {
            if (showFloatingView) {
                _instance._startFloatingView();
            } else {
                _instance._stopFloatingView(true);
            }
        }
    }

    public static void stop(boolean dismissNotify) {
        if (_instance != null) {
            _instance.dismissNotify = dismissNotify;
            _instance._stopFloatingView(false);
            _instance.stopSelf();
        }
    }

    public static void resetForeground() {
        if (_instance != null) {
            _instance.updateNotification();
        }
    }

    public static void startFloatingView() {
        if (_instance != null) {
            _instance._startFloatingView();
        }
    }

    public static void stopFloatingView() {
        if (_instance != null) {
            _instance._stopFloatingView(true);
        }
    }

    public static boolean isFloatingViewShowing() {
        if (_instance != null) {
            return _instance._isFloatingViewShowing();
        }
        return false;
    }

    private boolean _isFloatingViewShowing() {
        return mainFloatingView != null && mainFloatingView.isAttached();
    }

    @Override
    public IBinder onBind(Intent intent) {
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (_instance == null) {
            _instance = this;
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (_instance == null) {
            _instance = this;
        }

        spUtil = SettingUtil.INSTANCE;

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (SettingUtil.INSTANCE.isAppShowing()) {
            _startFloatingView();
        }

        RemoteConfigUtil.INSTANCE.tryFetchNew();

        startForeground();

        if (SettingUtil.INSTANCE.getAutoCloseAppWhenSpenInserted()) {
            SamsungSpenInsertedReceiver.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground();

        _stopFloatingView(false);

        //If process was been killed by system or user
        SettingUtil.INSTANCE.setAppShowing(true);

        if (ScreenshotHandler.isInitialized()) {
            ScreenshotHandler.getInstance().release();
        }
        _instance = null;

        SamsungSpenInsertedReceiver.stop();
    }

    private void startForeground() {
        startForeground(ONGOING_NOTIFICATION_ID, getForegroundNotification());
    }

    private Notification getForegroundNotification() {
        String channelId = getPackageName() + "_v1";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            List<NotificationChannel> channels = notificationManager.getNotificationChannels();
            for (NotificationChannel channel : channels) {
                String id = channel.getId();
                if (NotificationChannel.DEFAULT_CHANNEL_ID.equals(id)) {
                    continue;
                }
                if (!id.equals(channelId)) {
                    notificationManager.deleteNotificationChannel(id);
                }
            }
            if (notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        getString(R.string.foregroundNotification),
                        NotificationManager.IMPORTANCE_LOW);
                channel.setShowBadge(false);
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId);
        builder.setColor(ContextCompat.getColor(this, R.color.appIconColor));
        builder.setSmallIcon(R.drawable.ic_for_notify);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher_shadow));
        builder.setTicker(getString(R.string.app_name));
        builder.setContentTitle(getString(R.string.app_name));
        boolean toShow = !_isFloatingViewShowing();
        if (!toShow) {
            builder.setContentText(getString(R.string.notification_contentText_hide));
        } else {
            builder.setContentText(getString(R.string.notification_contentText_show));
        }
        Intent notificationIntent = MainActivity.getShowingStateSwitchIntent(this, toShow);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        return builder.build();
    }

    private void stopForeground() {
        stopForeground(dismissNotify);
    }

    private void updateNotification() {
        if (!dismissNotify) {
            notificationManager.notify(ONGOING_NOTIFICATION_ID, getForegroundNotification());
        }
    }

    private void _startFloatingView() {
        if (_isFloatingViewShowing()) {
            mainFloatingView.detachFromWindow();
        }
//        mainFloatingView = new FloatingPoint(this);
        mainFloatingView = new MainBar(this);
        mainFloatingView.attachToWindow();
        updateNotification();

        EventUtil.post(new ShowingStateChanged(true));
    }

    private void _stopFloatingView(boolean updateNotify) {
        if (mainFloatingView != null) {
            mainFloatingView.detachFromWindow();
        }
        if (updateNotify) {
            updateNotification();
        }

        EventUtil.post(new ShowingStateChanged(false));
    }
}
