package edu.rutgers.css.Rutgers.channels.bus.model;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import org.apache.commons.lang3.text.WordUtils;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.NextbusAPI;
import edu.rutgers.css.Rutgers.api.model.bus.Prediction;
import edu.rutgers.css.Rutgers.api.model.bus.Predictions;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.MainActivity;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGI;

/**
 * Alarm to poll the Next Bus api for use with bus notifications
 */
public final class BusNotificationService extends IntentService {
    private static final String TAG = "BusNotificationService";
    private static final long REFRESH_INTERVAL = 1000 * 60;

    private static final String ARG_AGENCY_TAG = "agency";
    private static final String ARG_ROUTE_TAG = "route";
    private static final String ARG_STOP_TAG = "stop";
    private static final String ARG_DELETE_TAG = "delete";
    private static final String ARG_THRESHOLD_TAG = "threshold";
    private static final String ARG_SOUND_URI_TAG = "soundUri";

    private static final String SERVICE_NAME = "BusNotificationService";
    private static final String DEFAULT_MESSAGE = "Bus Notification Running";

    private static final int NOTIFICATION_ID = 0;
    private static final int ALERT_NOTIFICATION_ID = 1;

    private static final int ALARM_INTENT_ID = 0;
    private static final int DELETE_INTENT_ID = 1;
    private static final int BUS_INTENT_ID = 2;
    private static final int REFRESH_INTENT_ID = 3;

    public BusNotificationService() {
        super(SERVICE_NAME);
    }

    // We need all these different pending intent creators so they don't override each other
    // See PendingIntent documentation for more details
    private static PendingIntent createPendingIntent(Context context, int id, Intent intent) {
        return PendingIntent.getService(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private static PendingIntent createAlarmPendingIntent(Context context, Intent intent) {
        return createPendingIntent(context, ALARM_INTENT_ID, intent);
    }

    private static PendingIntent createDeletePendingIntent(Context context, Intent intent) {
        return createPendingIntent(context, DELETE_INTENT_ID, intent);
    }

    private static PendingIntent createRefreshPendingIntent(Context context, Intent intent) {
        return createPendingIntent(context, REFRESH_INTENT_ID, intent);
    }

    /**
     * Start a new service that will query NextBus on an interval and update a {@link Notification}
     * @param context App context
     * @param agencyTag NextBus agency (probably "rutgers")
     * @param routeTag Bus route for Nextbus, ex. "a", "wknd1"
     * @param stopTag Stop title for Nextbus, ex. "Werblin"
     * @param link Deep link for when notification is tapped
     */
    public static synchronized void startAlarm(final Context context, String agencyTag, String routeTag, String stopTag, int alarmThreshold, Link link, Uri soundUri) {
        // Create intent for service that will query NextBus and update the notification
        final Intent alarmIntent = new Intent(context, BusNotificationService.class);
        alarmIntent.putExtra(ARG_AGENCY_TAG, agencyTag);
        alarmIntent.putExtra(ARG_ROUTE_TAG, routeTag);
        alarmIntent.putExtra(ARG_STOP_TAG, stopTag);
        alarmIntent.putExtra(ARG_THRESHOLD_TAG, alarmThreshold);
        alarmIntent.putExtra(ARG_SOUND_URI_TAG, soundUri);
        alarmIntent.setData(link.getUri());

        // Pending intent for AlarmManager
        PendingIntent alarmPendingIntent = createAlarmPendingIntent(context, alarmIntent);
        context.startService(alarmIntent);
        getAlarmManager(context).setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime(),
            REFRESH_INTERVAL,
            alarmPendingIntent
        );

        // Notification that will be shown
        // Starts with a default message that will be updated after query
        Notification notification = createNotification(
            context,
            alarmIntent,
            DEFAULT_MESSAGE,
            DEFAULT_MESSAGE,
            link.getUri()
        );

        // Display notification
        NotificationManagerCompat
            .from(context)
            .notify(NOTIFICATION_ID, notification);
    }

    public static synchronized void cancelAlarm(Context context, Intent intent) {
        getAlarmManager(context).cancel(createAlarmPendingIntent(context, intent));

        NotificationManagerCompat
            .from(context)
            .cancel(NOTIFICATION_ID);
    }

    private static AlarmManager getAlarmManager(final Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private static Notification createAlertNotification(Context context, String shortText, String longText, Uri link, Uri soundUri) {
        // Base options for notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("Rutgers Mobile Bus Prediction")
            .setContentText(shortText)
            // 4 1-second long vibrations
            .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000})
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(longText));

        // Intent that will (probably) link to BusDisplay
        // Used when the notification is clicked
        Intent busIntent = new Intent(context, MainActivity.class);
        busIntent.setData(link);
        busIntent.setAction(Intent.ACTION_VIEW);
        // Create the backstack for the busIntent (probably not necessary)
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context)
            .addParentStack(MainActivity.class)
            .addNextIntent(busIntent);

        PendingIntent busPendingIntent = stackBuilder.getPendingIntent(BUS_INTENT_ID, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(busPendingIntent);

        Notification notification = builder.build();
        notification.flags |= NotificationCompat.FLAG_INSISTENT;

        return notification;
    }

    private static Notification createNotification(Context context, Intent alarmIntent, String shortText, String longText, Uri link) {
        // Base options for notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("Rutgers Mobile Bus Prediction")
            .setContentText(shortText)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(longText))
            .setOngoing(true);

        // Intent that will (probably) link to BusDisplay
        // Used when the notification is clicked
        Intent busIntent = new Intent(context, MainActivity.class);
        busIntent.setData(link);
        busIntent.setAction(Intent.ACTION_VIEW);
        // Create the backstack for the busIntent (probably not necessary)
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context)
            .addParentStack(MainActivity.class)
            .addNextIntent(busIntent);

        PendingIntent busPendingIntent = stackBuilder.getPendingIntent(BUS_INTENT_ID, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(busPendingIntent);

        // Intent for when notification is canceled
        final Intent deleteIntent = new Intent(alarmIntent);
        deleteIntent.putExtra(ARG_DELETE_TAG, true);
        final PendingIntent deletePendingIntent = createDeletePendingIntent(context, deleteIntent);
        builder.setDeleteIntent(deletePendingIntent);

        builder.addAction(R.drawable.ic_xicon, "Remove", deletePendingIntent);
        builder.addAction(R.drawable.ic_refresh, "Refresh", createRefreshPendingIntent(context, alarmIntent));

        return builder.build();
    }

    protected void onHandleIntent(Intent intent) {
        LOGI(TAG, "Got intent for notification");

        boolean delete = intent.getBooleanExtra(ARG_DELETE_TAG, false);
        final String agencyTag = intent.getStringExtra(ARG_AGENCY_TAG);
        final String routeTag = intent.getStringExtra(ARG_ROUTE_TAG);
        final String stopTag = intent.getStringExtra(ARG_STOP_TAG);
        final int alarmThreshold = intent.getIntExtra(ARG_THRESHOLD_TAG, Integer.MAX_VALUE);
        final Uri soundUri = intent.getParcelableExtra(ARG_SOUND_URI_TAG);
        final Uri link = intent.getData();

        if (delete) {
            cancelAlarm(this, intent);
            return;
        }

        Predictions predictions = NextbusAPI.stopPredict(agencyTag, stopTag)
            .toBlocking().getIterator().next();
        Prediction foundPrediction = null;
        for (final Prediction prediction : predictions.getPredictions()) {
            if (prediction.getTag().equals(routeTag)) {
                foundPrediction = prediction;
                break;
            }
        }

        if (foundPrediction == null) {
            LOGE(TAG, "Could not find prediction");
            cancelAlarm(this, intent);
            return;
        }

        final int predictionMinutes = foundPrediction.getMinutes().get(0);

        final String shortText
            = String.valueOf(predictionMinutes)
            + (predictionMinutes == 1 ? " minute" : " minutes")
            + " remaining";

        final String longText
            = WordUtils.capitalize(routeTag)
            + " bus arrives at "
            + stopTag
            + (predictionMinutes == 0
                ? (" now!")
                : (" in "
                    + String.valueOf(predictionMinutes)
                    + (predictionMinutes == 1 ? " minute" : " minutes")
                )
            );

        LOGI(TAG, longText);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (predictionMinutes <= alarmThreshold) {
            Notification notification = createAlertNotification(
                this,
                shortText,
                longText,
                link,
                soundUri
            );
            notificationManager.notify(ALERT_NOTIFICATION_ID, notification);
            notificationManager.cancel(NOTIFICATION_ID);
            cancelAlarm(this, intent);
            return;
        }

        Notification notification = createNotification(
                this,
                intent,
                shortText,
                longText,
                link
        );
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
