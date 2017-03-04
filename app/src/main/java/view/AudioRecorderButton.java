package view;

import android.content.Context;
import android.os.Environment;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.asus.recorder.R;

import android.os.Handler;
import java.util.logging.LogRecord;


/**
 * Created by asus on 2017/3/4.
 */
public class AudioRecorderButton extends Button implements AudioManager.AudioStateListener {
    private static final int DISTANCE_Y_CANCEL = 50;
    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2; //录音状态
    private static final int STATE_WANT_TO_CANCEL = 3;

    private int mCurState = STATE_NORMAL;  // 当前记录状态
    //已经开始录音
    private boolean isRecording = false;

    private DialogManager mDialogManager;

    private AudioManager mAudioManager;

    private float mTime;
    //是否触发longClick
    private boolean mReady;




    public AudioRecorderButton(Context context) {
        this(context, null);
    }
//初始化按钮和按钮长按事件
    public AudioRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        mDialogManager = new DialogManager(getContext());
        String dir = Environment.getExternalStorageDirectory() + "/cwx_recorder_audios";
        mAudioManager = AudioManager.getInstance(dir);
        mAudioManager.setOnAudioStateListener(this);


        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mReady = true;
               mAudioManager.prepareAudio();;
                return false;
            }
        });
    }
    /*
    * 录音完成后的回调*/
    public interface AudioFinishRecorderListener
    {
        void onFinish(float seconds,String filePath);
    }
    private AudioFinishRecorderListener mListener;

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener)
    {
        mListener = listener;
    }
    /*
    * 获取音量大小的Runnable*/
    private Runnable mGetVoiceLevelRunnable = new Runnable() {

        public void run() {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    mTime += 0.1f;
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private static final int MSG_AUDIO_PREPARED = 0X110;
    private static final int MSG_VOICE_CHANGED = 0X111;
    private static final int MSG_DIALOG_DIMISS = 0X112;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case MSG_AUDIO_PREPARED:
                    //TODO 真正显示应该在audio end prepared 之后
                    mDialogManager.showRecordingDialog();
                    isRecording = true;
                    //开启一个线程
                    new Thread(mGetVoiceLevelRunnable).start();
                    break;
                case MSG_VOICE_CHANGED:
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
                    break;
                case MSG_DIALOG_DIMISS:
                    mDialogManager.diMissDialog();
                    break;
            }
            super.handleMessage(msg);
        };
    };

    @Override
    public void wellPrepared() {
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);

    }
/*
* 屏幕触摸事件*/
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                changeState(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isRecording) {
                    //根据x，y的坐标，判断是否想要取消
                    if (wantToCancel(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);

                    } else {
                        changeState(STATE_RECORDING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mReady)
                {
                    reset();
                    return super.onTouchEvent(event);
                }
                if (!isRecording || mTime <0.6f )
                {
                    mDialogManager.tooShort();
                    mAudioManager.cancel();
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS,1000);//延迟显示对话框
                }else if (mCurState == STATE_RECORDING)//正常录制结束
                {
                    mDialogManager.diMissDialog();
                    mAudioManager.release();

                    if (mListener != null)
                    {
                        mListener.onFinish(mTime,mAudioManager.getCurrentFilePath());
                    }
                    //callbackToAct
                } else if (mCurState == STATE_WANT_TO_CANCEL) {
                    mDialogManager.diMissDialog();
                    mAudioManager.cancel();
                    //cancel
                }
                reset();
                break;
        }
        return super.onTouchEvent(event);
    }

    /*恢复状态及标志位*/
    private void reset() {
        isRecording = false;
        mReady = false;
        mTime = 0;
        changeState(STATE_NORMAL);

    }

    private boolean wantToCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {
            return true;
        }
        if (y < DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }
        return false;
    }

    private void changeState(int state) {
        if (mCurState != state) {
            mCurState = state;
            switch (state) {
                case STATE_NORMAL:
                    setBackgroundResource(R.drawable.btn_recorder_normal);
                    setText(R.string.str_recorder_normal);
                    break;
                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.btn_recorder_recording);
                    setText(R.string.str_recorder_recording);
                    if (isRecording) {
                        mDialogManager.recording();
                    }
                    break;
                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.btn_recorder_recording);
                    setText(R.string.str_recorder_want_cancel);

                    mDialogManager.wantToCancel();
                    break;
            }
        }
    }


}
