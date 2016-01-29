package edu.rutgers.css.Rutgers.channels.bus.model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
 * Alarm to poll the Next Bus api for use with bus notifications
 */
public final class BusNotificationAlarm extends BroadcastReceiver {
    private static final long REFRESH_INTERVAL = 1000 * 60 * 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        final PowerManager.WakeLock wl = getWakeLock(context);
        wl.acquire();
        wl.release();
    }

    public void startAlarm(final Context context) {
        final Intent intent = new Intent(context, BusNotificationAlarm.class);
        final PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        getAlarmManager(context).setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), REFRESH_INTERVAL, pi);
    }

    public void cancelAlarm(final Context context) {
        final Intent intent = new Intent(context, BusNotificationAlarm.class);
        final PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        getAlarmManager(context).cancel(sender);
    }

    private AlarmManager getAlarmManager(final Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private PowerManager.WakeLock getWakeLock(final Context context) {
        final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
    }
}
