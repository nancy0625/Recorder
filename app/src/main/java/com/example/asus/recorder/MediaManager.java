package com.example.asus.recorder;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener ;
import android.print.PageRange;
import android.media.MediaPlayer.OnErrorListener;
import android.support.v4.app.NotificationCompatSideChannelService;

import java.io.IOException;

/**
 * Created by asus on 2017/3/4.
 */
public class MediaManager {
    private static MediaPlayer mMediaPlayer;
    private static boolean isPause;


    public static void playSound(String filePath, OnCompletionListener onCompletionListener) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        }else
        {
            mMediaPlayer.reset();
        }
        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void pause()
    {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying())
        {
            mMediaPlayer.pause();
            isPause = true;

        }
    }
    public static void resume()
    {
        if (mMediaPlayer != null && isPause)
        {
            mMediaPlayer.start();
            isPause = false;
        }
    }

    public static void release()
    {
        if (mMediaPlayer != null)
        {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
