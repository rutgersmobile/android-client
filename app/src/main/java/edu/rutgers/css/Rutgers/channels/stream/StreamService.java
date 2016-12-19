package edu.rutgers.css.Rutgers.channels.stream;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import edu.rutgers.css.Rutgers.R;
import rx.Observable;
import rx.subjects.PublishSubject;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGI;

/**
 * Background service that controls a media stream
 */
public class StreamService extends Service implements AudioManager.OnAudioFocusChangeListener {
    private static final String ARG_PLAY_TAG = "play";
    private static final String ARG_URL_TAG = "url";
    private static final String ARG_STOP_TAG = "stop";

    private static final String TAG = "StreamService";

    private static final int  NOTIFICATION_ID = 1;

    private static final int DELETE_INTENT_ID = 0;
    private static final int PLAY_INTENT_ID = 1;

    public static void startStream(Context context, boolean play, String url) {
        context.startService(createPlayIntent(context, play, url));
    }

    private static Intent createPlayIntent(Context context, boolean play, String url) {
        final Intent intent = new Intent(context, StreamService.class);
        intent.putExtra(ARG_PLAY_TAG, play);
        intent.putExtra(ARG_URL_TAG, url);
        return intent;
    }

    private static PublishSubject<Boolean> playingSubject = PublishSubject.create();
    private static boolean playing = false;
    private static void setPlaying(boolean playing) {
        StreamService.playing = playing;
        playingSubject.onNext(playing);
    }
    public static Observable<Boolean> playing() {
        return playingSubject.asObservable();
    }
    public static boolean isPlaying() {
        return playing;
    }

    private int startId;
    private boolean errored = false;
    private static String resourceUrl;
    public static String getResourceUrl() {
        return resourceUrl;
    }

    private MediaPlayer mediaPlayer;
    private boolean needsToStart = true;
    private WifiManager.WifiLock wifiLock;

    private void startMediaPlayer(String url) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        try {
            mediaPlayer.setDataSource(url);

            mediaPlayer.setOnCompletionListener(mp -> {
                needsToStart = true;
                mediaPlayer.stop();
                mediaPlayer.reset();
                if (errored) {
                    stop();
                }
            });

            mediaPlayer.setOnPreparedListener(mediaPlayer1 -> {
                LOGI(TAG, "Prepare finished, starting");
                needsToStart = false;
                mediaPlayer.start();
                if (wifiLock == null) {
                    wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                        .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
                    if (!wifiLock.isHeld()) {
                        wifiLock.acquire();
                    }
                }
            });

            mediaPlayer.setOnErrorListener((mediaPlayer1, what, extra) -> {
                LOGE(TAG, "Error! What: " + what + "; Extra: " + extra);
                errored = true;
                return false;
            });

            mediaPlayer.prepareAsync();
        } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
            LOGE(TAG, e.getMessage());
            stop();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.startId = startId;
        final boolean stop = intent.getBooleanExtra(ARG_STOP_TAG, false);
        if (stop) {
            stop();
            return START_NOT_STICKY;
        }

        final boolean play = intent.getBooleanExtra(ARG_PLAY_TAG, false);
        final String url = intent.getStringExtra(ARG_URL_TAG);
        if (!StringUtils.equals(url, resourceUrl)) {
            resourceUrl = url;
            disableMediaPlayer();
        }

        if (play) {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                stop();
                return START_NOT_STICKY;
            }
            if (needsToStart) {
                startMediaPlayer(resourceUrl);
            } else if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
            setPlaying(true);
        } else if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            setPlaying(false);
            releaseWifi();
        }

        startNotification(play);

        return START_NOT_STICKY;
    }

    private static PendingIntent createPendingIntent(Context context, int id, Intent intent) {
        return PendingIntent.getService(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private static PendingIntent createDeletePendingIntent(Context context) {
        final Intent intent = new Intent(context, StreamService.class);
        intent.putExtra(ARG_STOP_TAG, true);
        return createPendingIntent(context, DELETE_INTENT_ID, intent);
    }

    private static PendingIntent createPlayPendingIntent(Context context, boolean play, String resourceUrl) {
        Intent intent = createPlayIntent(context, play, resourceUrl);
        return createPendingIntent(context, PLAY_INTENT_ID, intent);
    }

    private void startNotification(boolean play) {
        PendingIntent deletePendingIntent = createDeletePendingIntent(getApplicationContext());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
            .setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
            .setContentTitle("WRNU")
            .setContentText("Playing WRNU")
            .setDeleteIntent(deletePendingIntent);

        if (!play) {
            PendingIntent playPendingIntent = createPlayPendingIntent(getApplicationContext(), true, resourceUrl);
            builder.addAction(R.drawable.ic_play_arrow_black_24dp, "Play", playPendingIntent);
        } else {
            PendingIntent playPendingIntent = createPlayPendingIntent(getApplicationContext(), false, resourceUrl);
            builder.addAction(R.drawable.ic_pause_black_24dp, "Pause", playPendingIntent);
        }

        builder.addAction(R.drawable.ic_xicon, "Stop", deletePendingIntent);

        startForeground(NOTIFICATION_ID, builder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disableMediaPlayer();
        releaseWifi();
    }

    private void stop() {
        setPlaying(false);
        disableMediaPlayer();
        stopSelf(startId);
        releaseWifi();
    }

    private void releaseWifi() {
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
    }

    private void disableMediaPlayer() {
        if (mediaPlayer != null) {
            needsToStart = true;
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onAudioFocusChange(int i) {
        switch (i) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null) {
                    startMediaPlayer(resourceUrl);
                } else if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
                mediaPlayer.setVolume(1.0f, 1.0f);
                startNotification(true);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                disableMediaPlayer();
                startNotification(false);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                startNotification(false);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }
}
