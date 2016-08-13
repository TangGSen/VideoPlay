package com.example.testvideoplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.video.bean.VideoListAdapter;
import com.video.bean.ViedoDatas;
import com.video.player.MeasureUtil;
import com.video.player.NetUtil;
import com.video.player.StringUtil;
import com.video.player.VideoBaseActivity;
import com.video.player.VideoView;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.CordovaPlugin;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import player.video.com.videoplayerlib.R;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class VideoPlayerActivity extends VideoBaseActivity implements CordovaInterface {


    //使用cordova webview
    // The webview for our app
    protected CordovaWebView video_webview;
    // Plugin to call when activity result is received  
    protected int activityResultRequestCode;
    protected CordovaPlugin activityResultCallback;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private VideoView video_view;
    // 底部控制面板控件
    private ListView listview_video_show;
    private LinearLayout ll_bottom_control, layout_volume_brightness,
            ll_top_control, ll_buffering, root_layout, layout_forward_back;
    private SeekBar video_seekbar;
    private TextView tv_name, tv_current_progress, tv_duration,
            btn_exit_or_srceen, btn_play, btn_exit_video, tv_choose_item,
            tv_forward_back, btn_exit_repate_video, btn_repeat_video;
    private RelativeLayout ll_loading, layout_video_player,
            layout_error_handle;
    private ProgressBar progressbar, pb_volume_brightness;
    private ImageView img_volume_brightness, img_forward_back, btn_screen;
    // 音量广播
    private VolumeReceiver mVolumeReceiver;

    // handler 的变量
    private final int MESSAGE_UPDATE_PLAY_PROGRESS = 1;// 更新播放进度
    private final int MESSAGE_HIDE_CONTROL = 2;// 延时隐藏控制面板
    private final int CLOSE_VOLUME_SHOW = 3;// 延时关闭声音亮度
    private final int CLOSE_FORWORD_BACK = 4;// 延时关闭快进快退
    private final int CLOSE_VIDEO_LIST = 5;// 横屏时关闭延时菜单栏
    public final int CHANGE_SREEN = 6;// 重力感应改UI
    private final int VIDEO_ERROR_NOTNET = 7;

    private final long HIDE_CONTROL = 2500;

    private AudioManager audioManager;
    private int currentVolume;// 当前的音量
    private int maxVolume;// 系统最大音量

    private int currentIndex;// 播放当前的
    private VideoListAdapter VideoListAdapter;
    /**
     * 手势的变量
     */
    private float mBrightness = -1f;
    private boolean firstScroll = false;// 每次触摸屏幕后，第一次scroll的标志

    private int GESTURE_FLAG = 0;// 1,调节进度，2，调节音量
    private static final int GESTURE_MODIFY_PROGRESS = 1; // 进度标记
    private static final int GESTURE_MODIFY_VOLUME = 2; // 声音标记
    private static final int GESTURE_MODIFY_LIGHTNESS = 3; // 亮度标记
    private static final float STEP_BRIGHTNESS = 4f;// 协调亮度滑动时的步长，避免每次滑动都改变，导致改变过快
    private static final float STEP_PROGRESS = 2f;// 设定进度滑动时的步长，避免每次滑动都改变，导致改变过快
    private static final float STEP_VOLUME = 4f;// 协调音量滑动时的步长，避免每次滑动都改变，导致改变过快

    private int screenWidth;
    private int screenHeight;
    private int mTouchSlop;// 滑动的界限值
    private int currentPosition;// 当前视频在videoList中的位置
    private List<ViedoDatas.ChapterListBean> videoList;// 视频列表
    private GestureDetector gestureDetector;
    // 横竖屏切换
    private boolean sensor_flag = true;
    private boolean stretch_flag = true;

    private SensorManager sm;
    private OrientationSensorListener listener;
    private Sensor sensor;

    private SensorManager sm1;
    private Sensor sensor1;
    private OrientationSensorListener2 listener1;

    // 计算学时
    private long startTime;
    private long endTime;
    private long useTime;
    //private	DBDao mDbDao;
    private String lessonId;
    private String userId;
    private String lessonItemId;

	/*
     * 播放时错误处理变量 ps 本人也想过使用监听网络状态变化时候才改变UI 但是播放视频时有缓存就算没有网络但是也能播放
	 */

    // 因为播放失败由于格式不支持，无网络分几种情景（虽说分这么细，有些错误可以合并的，方便以后要用）
    // ERROR_FROM_DEFUAL正常播放时突然没网或者刚进来就检查有无
    private static final int ERROR_FROM_DEFUAL = 0;
    private static final int ERROR_FROM_NOTSUPPORT = 1; // 不支持的格式
    private static final int ERROR_FROM_LIST = 2; // 从章节切换时无网络标记，自动切换时,重播
    private int currentErrorProgress;
    private int currentErrorType = -1;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {

                case MESSAGE_UPDATE_PLAY_PROGRESS:
                    updatePlayProgress();
                    break;
                case MESSAGE_HIDE_CONTROL:
                    hideControlLayout();
                    break;

                case CLOSE_VOLUME_SHOW:
                    if (layout_volume_brightness != null) {
                        layout_volume_brightness.setVisibility(View.GONE);
                    }
                    break;
                case CLOSE_FORWORD_BACK:
                    if (layout_forward_back != null) {
                        layout_forward_back.setVisibility(View.GONE);
                    }
                    break;
                case CLOSE_VIDEO_LIST:
                    setVideoFullSreen();
                    break;

                case CHANGE_SREEN:
                    int orientation = msg.arg1;
                    if (orientation > 45 && orientation < 135) {

                    } else if (orientation > 135 && orientation < 225) {

                    } else if (orientation > 225 && orientation < 315) {
                        // System.out.println("切换成横屏");
                        VideoPlayerActivity.this
                                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        sensor_flag = false;
                        stretch_flag = false;

                    } else if ((orientation > 315 && orientation < 360)
                            || (orientation > 0 && orientation < 45)) {
                        // System.out.println("切换成竖屏");
                        VideoPlayerActivity.this
                                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        sensor_flag = true;
                        stretch_flag = true;

                    }
                    break;

                case VIDEO_ERROR_NOTNET:
                    // video_view.pause();
                    // 更换播放按钮图片
                    layout_error_handle.setVisibility(View.VISIBLE);
                    ll_buffering.setVisibility(View.GONE);
                    btn_repeat_video.setText("请检查网络");

                    break;

            }
        }

    };

    @Override
    protected void initView() {
        // Activity全屏显示，且状态栏被隐藏覆盖掉。

        setContentView(R.layout.act_video_player);
        /**
         * 记录学时才把这个给放开
         */
        //	mDbDao = new DBThreadDaoImpls(getApplicationContext());
        layout_video_player = (RelativeLayout) findViewById(R.id.layout_video_player);
        listview_video_show = (ListView) findViewById(R.id.listview_video_show);

        video_view = (VideoView) findViewById(R.id.video_view);

        ll_top_control = (LinearLayout) findViewById(R.id.ll_top_control);
        tv_name = (TextView) findViewById(R.id.tv_name);

        ll_bottom_control = (LinearLayout) findViewById(R.id.ll_bottom_control);
        video_seekbar = (SeekBar) findViewById(R.id.video_seekbar);
        btn_play = (TextView) findViewById(R.id.btn_play);
        btn_exit_or_srceen = (TextView) findViewById(R.id.btn_exit_or_srceen);
        btn_exit_video = (TextView) findViewById(R.id.btn_exit_video);
        btn_screen = (ImageView) findViewById(R.id.btn_screen);
        tv_current_progress = (TextView) findViewById(R.id.tv_current_progress);
        tv_duration = (TextView) findViewById(R.id.tv_duration);

        layout_volume_brightness = (LinearLayout) findViewById(R.id.layout_volume_brightness);
        tv_choose_item = (TextView) findViewById(R.id.tv_choose_item);
        btn_repeat_video = (TextView) findViewById(R.id.btn_repeat_video);
        btn_exit_repate_video = (TextView) findViewById(R.id.btn_exit_repate_video);
        ll_loading = (RelativeLayout) findViewById(R.id.ll_loading);

        layout_error_handle = (RelativeLayout) findViewById(R.id.layout_repeat_play);
        ll_buffering = (LinearLayout) findViewById(R.id.ll_buffering);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        pb_volume_brightness = (ProgressBar) findViewById(R.id.pb_volume_brightness);
        img_volume_brightness = (ImageView) findViewById(R.id.img_volume_brightness);

        root_layout = (LinearLayout) findViewById(R.id.root_layout);

        layout_forward_back = (LinearLayout) findViewById(R.id.layout_forward_back);
        tv_forward_back = (TextView) findViewById(R.id.tv_forward_back);
        img_forward_back = (ImageView) findViewById(R.id.img_forward_back);

        video_webview = (CordovaWebView) findViewById(R.id.video_webview);
//		video_webview.setOnKeyListener(null);
        Config.init(this);


        // 注册重力感应器 屏幕旋转
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        listener = new OrientationSensorListener(handler);
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);

        // 根据 旋转之后 点击 符合之后 激活sm
        sm1 = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor1 = sm1.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        listener1 = new OrientationSensorListener2();
        sm1.registerListener(listener1, sensor1, SensorManager.SENSOR_DELAY_UI);
        // 根据横竖屏设置View 的大小和是否显示
        setVideoSetting();


    }

    // 横竖屏切换对控件的显示，宽高设置
    private void setVideoSetting() {
        handler.removeMessages(CLOSE_VIDEO_LIST);
        // 如果是竖屏的话显示列表在下
        int[] wh = MeasureUtil.getScreenSize(VideoPlayerActivity.this);
        LinearLayout.LayoutParams param;
        if (!stretch_flag) {
            // 横屏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            root_layout.setOrientation(LinearLayout.HORIZONTAL);
            param = new LinearLayout.LayoutParams(wh[0], wh[1]);
            video_view.setWHScreen(wh[0], wh[1]);
            tv_choose_item.setVisibility(View.VISIBLE);
            listview_video_show.setBackgroundColor(Color.BLACK);
            listview_video_show.setVisibility(View.GONE);
            video_webview.setVisibility(View.GONE);

            // 横屏才出现锁屏的按钮
            btn_screen.setImageResource(R.drawable.btn_esc_sreen);

        } else {
            // 取消全屏设置
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            root_layout.setOrientation(LinearLayout.VERTICAL);
            param = new LinearLayout.LayoutParams(wh[0], wh[1] / 3);
            video_view.setWHScreen(wh[0], wh[1] / 3);
            tv_choose_item.setVisibility(View.GONE);
            listview_video_show.setVisibility(View.GONE);
            video_webview.setVisibility(View.VISIBLE);
            btn_screen.setImageResource(R.drawable.btn_screen_change);

        }
        layout_video_player.setLayoutParams(param);
        updatePlayBtnBg(!video_view.isPlaying());
    }

    // 设置全屏
    private void setVideoFullSreen() {
        if (listview_video_show.getVisibility() == View.VISIBLE) {
            int[] wh = MeasureUtil.getScreenSize(VideoPlayerActivity.this);
            LinearLayout.LayoutParams param;
            param = new LinearLayout.LayoutParams(wh[0], wh[1]);
            video_view.setWHScreen(wh[0], wh[1]);
            listview_video_show.setVisibility(View.GONE);
            layout_video_player.setLayoutParams(param);
            btn_screen.setImageResource(R.drawable.btn_esc_sreen);
        }

    }

    private void setVideoNotFullSreen() {
        int[] wh = MeasureUtil.getScreenSize(VideoPlayerActivity.this);
        listview_video_show.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                wh[0] * 2 / 3, wh[1]);
        video_view.setWHScreen(wh[0] * 2 / 3, wh[1]);
        layout_video_player.setLayoutParams(param);
        btn_screen.setImageResource(R.drawable.btn_screen_change_big);
    }

    public boolean getScreenDirection() {

        Configuration mConfiguration = getResources().getConfiguration(); // 获取设置的配置信息
        int ori = mConfiguration.orientation; // 获取屏幕方向

        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {

            // 横屏
            return true;
        } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {

            // 竖屏
            return false;
        }
        return false;
    }

    @Override
    protected void initListener() {
        btn_exit_or_srceen.setOnClickListener(this);
        btn_exit_video.setOnClickListener(this);
        btn_screen.setOnClickListener(this);
        btn_play.setOnClickListener(this);

        video_seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.sendEmptyMessageDelayed(MESSAGE_HIDE_CONTROL, HIDE_CONTROL);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeMessages(MESSAGE_HIDE_CONTROL);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    video_view.seekTo(progress);
                    tv_current_progress.setText(StringUtil
                            .formatVideoDuration(progress));
                }
            }
        });
        // 监听播放结束
        video_view.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 如果是播放完毕自动播放下一个，最后一个播放完毕那就重现重播
                if (currentIndex < videoList.size() - 1) {
                    endTime = System.currentTimeMillis();

                    //	calculateAndUpload(startTime, endTime);
                    Toast.makeText(getApplicationContext(), "正在播放下一个视频",
                            Toast.LENGTH_SHORT).show();
                    Log.e("sen", "切换前" + currentIndex);
                    changeItemPlay(currentIndex, ++currentIndex,
                            ERROR_FROM_LIST);

                } else {
                    // btn_repeat_video 重播，和网络出错重播
                    btn_repeat_video.setText("重播");
                    video_view.pause();
                    endTime = System.currentTimeMillis();
                    //	calculateAndUpload(startTime, endTime);
                    updatePlayBtnBg(true);
                    layout_error_handle.setVisibility(View.VISIBLE);
                    ll_buffering.setVisibility(View.GONE);
                    currentErrorType = ERROR_FROM_LIST;

                }

            }
        });
        video_view
                .setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        // LogUtil.e(this, "percent: "+percent);
                        // percent:0-100
                        int bufferedProgress = (int) ((percent / 100.0f) * video_view
                                .getDuration());
                        video_seekbar.setSecondaryProgress(bufferedProgress);
                    }
                });
        video_view.setOnInfoListener(new OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:// 当拖动卡顿开始时调用
                        if (NetUtil.isNetworkConnected(VideoPlayerActivity.this)) {
                            ll_buffering.setVisibility(View.VISIBLE);
                        } else {
                            handler.sendEmptyMessage(VIDEO_ERROR_NOTNET);
                            currentErrorType = ERROR_FROM_DEFUAL;
                            currentErrorProgress = video_seekbar.getProgress();
                        }
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:// 当拖动卡顿结束调用
                        ll_buffering.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });
        video_view.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        // 判断是不是网络没连接
                        if (NetUtil.isNetworkConnected(VideoPlayerActivity.this)) {
                            Toast.makeText(VideoPlayerActivity.this, "不支持视频格式",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            handler.sendEmptyMessage(VIDEO_ERROR_NOTNET);
                            currentErrorType = ERROR_FROM_DEFUAL;
                            currentErrorProgress = video_seekbar.getProgress();
                            Log.e("sen", "出错地方" + currentErrorProgress);
                        }
                        break;

                }
                return false;
            }
        });

        // 监听并响应所点击的item
        listview_video_show.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {

                changeItemPlay(currentIndex, position, ERROR_FROM_LIST);
            }
        });
        // 选集
        tv_choose_item.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (listview_video_show.getVisibility() == View.GONE) {
                    setVideoNotFullSreen();
                } else {
                    setVideoFullSreen();
                }

            }
        });

        // 重播或出错处理
        btn_repeat_video.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!NetUtil.isNetworkConnected(VideoPlayerActivity.this)) {
                    handler.sendEmptyMessage(VIDEO_ERROR_NOTNET);
                    return;
                }
                layout_error_handle.setVisibility(View.GONE);
                // Log.e("sen", currentErrorType + "______");

                // Log.e("sen", currentErrorProgress + "进度______");
                if (currentErrorType == ERROR_FROM_DEFUAL) {
                    // 恢复到原来的出错无网的进度
                    // Log.e("sen","修改进度");
                    changeItemPlay(currentIndex, currentIndex, ERROR_FROM_DEFUAL);
                    video_view.seekTo(currentErrorProgress);
                    video_seekbar.setProgress(video_view.getCurrentPosition());
                } else {
                    changeItemPlay(currentIndex, currentIndex, ERROR_FROM_LIST);
                }

            }
        });

        btn_exit_repate_video.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finishVideoCheck(false);

            }
        });
        //屏蔽webview 长按出现系统复制粘贴
        video_webview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });

    }

    // 播放课程检查是否有网络
    private void changeItemPlay(int oldInt, int newInt, int errorType) {
        // 同时之前的状态播放为false ,
        videoList.get(oldInt).setPlay(false);
        videoList.get(newInt).setPlay(true);
        currentIndex = newInt;
        VideoListAdapter.setPlayStatus();
        if (NetUtil.isNetworkConnected(VideoPlayerActivity.this)) {
            playVideo(newInt);
            updatePlayBtnBg(false);
            layout_error_handle.setVisibility(View.GONE);
        } else {
            currentErrorType = errorType;
            handler.sendEmptyMessage(VIDEO_ERROR_NOTNET);
        }

    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        Bundle chapterListBundle = intent.getBundleExtra("chapterListData");
        currentIndex = chapterListBundle.getInt("currentIndex");
        userId = chapterListBundle.getString("userId");
        String chapterListString = chapterListBundle.getString("chapterList");
        // Log.e("sen", chapterListString);

        gestureDetector = new GestureDetector(this, new MyGestureLitener());
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        mTouchSlop = ViewConfiguration.getTouchSlop();

        RegisterReceiverVolumeChange();
        // 初始化音量
        initVolume();

        video_view.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // ll_loading.setVisibility(View.GONE);
                // 给加载界面增加渐隐动画
                ViewPropertyAnimator.animate(ll_loading).alpha(0)
                        .setDuration(600)
                        .setListener(new Animator.AnimatorListener() {


                            @Override
                            public void onAnimationStart(Animator arg0) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator arg0) {
                            }

                            @Override
                            public void onAnimationEnd(Animator arg0) {
                                ll_loading.setVisibility(View.GONE);
                                updatePlayBtnBg(false);
                                layout_error_handle.setVisibility(View.GONE);
                                handler.sendEmptyMessageDelayed(MESSAGE_HIDE_CONTROL, HIDE_CONTROL);
                            }

                            @Override
                            public void onAnimationCancel(Animator arg0) {
                            }
                        });
                // 视频开始播放
                video_view.start();
                startTime = System.currentTimeMillis();
                // btn_play.setBackgroundResource(R.drawable.selector_btn_pause);
                video_seekbar.setMax(video_view.getDuration());
                tv_duration.setText(StringUtil.formatVideoDuration(video_view
                        .getDuration()));
                updatePlayProgress();
            }
        });

        ViedoDatas videoDatas = JSON.parseObject(chapterListString,
                ViedoDatas.class);
        Log.e("sen", videoDatas.getChapterList().get(0).getLE_NAME());
        videoList = videoDatas.getChapterList();
        if (videoList != null && videoList.size() > 0) {
            // 防止越界
            currentIndex = currentIndex > videoList.size() - 1 ? 0
                    : currentIndex;
            // 向adapter装载数据
            VideoListAdapter = new VideoListAdapter(this, videoList);
            listview_video_show.setAdapter(VideoListAdapter);
            changeItemPlay(currentIndex, currentIndex, ERROR_FROM_LIST);

        } else {
            Toast.makeText(VideoPlayerActivity.this, "没有课程章节视频数据...",
                    Toast.LENGTH_LONG).show();
        }

        if (video_webview.getVisibility() == View.VISIBLE) {
            video_webview.loadUrl("file:///android_asset/www/lesson/lessonDetail.html");
        }

    }

    /**
     * 初始化音量
     */
    private void initVolume() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

    }

    /**
     * 注册当音量发生变化时接收的广播
     */
    private void RegisterReceiverVolumeChange() {
        mVolumeReceiver = new VolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(mVolumeReceiver, filter);
    }

    /**
     * 处理音量变化时的界面显示
     *
     * @author long
     */
    private class VolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 如果音量发生变化则更改seekbar的位置
            if (intent.getAction()
                    .equals("android.media.VOLUME_CHANGED_ACTION")) {
                currentVolume = audioManager
                        .getStreamVolume(AudioManager.STREAM_MUSIC);// 当前的媒体音量

            }
        }
    }

    // 更新volume img and text
    private void updataVolumeBrightBar(int percent, int resId) {
        handler.removeMessages(CLOSE_VOLUME_SHOW);
        if (layout_volume_brightness.getVisibility() != View.VISIBLE) {
            layout_volume_brightness.setVisibility(View.VISIBLE);
        }
        img_volume_brightness.setImageResource(resId);
        if (pb_volume_brightness != null) {
            pb_volume_brightness.setProgress(percent);
        }
        handler.sendEmptyMessageDelayed(CLOSE_VOLUME_SHOW, 1000);
    }

    // 更新volume img and text
    private void updataForwordBack(String change, int resId) {
        handler.removeMessages(CLOSE_FORWORD_BACK);
        if (layout_forward_back.getVisibility() != View.VISIBLE) {
            layout_forward_back.setVisibility(View.VISIBLE);
        }
        img_forward_back.setImageResource(resId);
        tv_forward_back.setText(change);
        handler.sendEmptyMessageDelayed(CLOSE_FORWORD_BACK, 1000);
    }

    // 在被其他应用，就暂停播放
    @Override
    protected void onPause() {
        if (video_view.isPlaying()) {
            video_view.pause();
            endTime = System.currentTimeMillis();

            //calculateAndUpload(startTime, endTime);
            handler.removeMessages(MESSAGE_UPDATE_PLAY_PROGRESS);
        }
        super.onPause();
    }

    /**
     * 播放currentPosition当前位置的视频
     */
    private void playVideo(int currentPosition) {
        if (videoList == null || videoList.size() == 0) {
            // 待会再做错误界面
            return;
        }
        ViedoDatas.ChapterListBean videoItem = videoList.get(currentPosition);
        updatePlayBtnBg(false);
        tv_name.setText(videoItem.getLE_NAME());

		/*
         * video_view.setVideoURI(Uri.parse("http://172.30.1.176/" +
		 * videoItem.getLE_ID() + "/" + videoItem.getENTER_URL()));
		 */
        // 修改图标

        video_view.setVideoURI(Uri.parse(videoItem.getENTER_URL()));
        video_view
                .setVideoURI(Uri
                        .parse("http://gd3.tv.cq3g.cn/app_1/_definst_/smil:podcast/2014/0116/188869.smil/playlist.m3u8"));
        // video_view
        // .setVideoURI(Uri.parse(url));
        // video_view
        // .setVideoURI(Uri
        // .parse("http://downmp413.ffxia.com/mp413/%E5%BC%A0%E9%B9%8F-%E8%80%81%E5%A9%86%E8%AF%B4%E4%BA%86%E7%AE%97[68mtv.com].mp4"));

    }

    /**
     * 更新播放进度
     */
    private void updatePlayProgress() {
        tv_current_progress.setText(StringUtil.formatVideoDuration(video_view
                .getCurrentPosition()));
        video_seekbar.setProgress(video_view.getCurrentPosition());
        handler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PLAY_PROGRESS, 1000);
    }

    @Override
    protected void processClick(View v) {
        switch (v.getId()) {

            case R.id.btn_exit_or_srceen:
                // 竖屏的情况下直接退出，横屏的话直接退回到竖屏
                finishVideoCheck(false);
                break;
            case R.id.btn_exit_video:
                finish();
                break;
            case R.id.btn_play:
                if (video_view.isPlaying()) {
                    endTime = System.currentTimeMillis();
                    //calculateAndUpload(startTime, endTime);
                    video_view.pause();
                    updatePlayBtnBg(true);
                    handler.removeMessages(MESSAGE_UPDATE_PLAY_PROGRESS);
                } else {
                    updatePlayBtnBg(false);
                    video_view.start();
                    startTime = System.currentTimeMillis();
                    handler.sendEmptyMessage(MESSAGE_UPDATE_PLAY_PROGRESS);
                }

                break;

            case R.id.btn_screen:
                // 如果是竖屏的那么就是横屏，如果横屏，并且菜单栏还显示的话那么就是全屏了
                sm.unregisterListener(listener);
                if (getScreenDirection()) {

                    if (listview_video_show.getVisibility() == View.GONE) {
                        stretch_flag = true;
                        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
                        listview_video_show.setVisibility(View.VISIBLE);
                    } else {
                        handler.removeMessages(CLOSE_VIDEO_LIST);
                        setVideoFullSreen();
                    }
                } else {
                    stretch_flag = false;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    listview_video_show.setVisibility(View.VISIBLE);
                }

                break;
        }
    }

    // 按返回键和退出键检查
    private void finishVideoCheck(boolean isKeyDown) {
        if (getScreenDirection()) {
            sm.unregisterListener(listener);
            setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
            // VideoPlayerActivity.this.setRequestedOrientation(1);
            sensor_flag = true;
            stretch_flag = true;
        } else {
            sm.unregisterListener(listener);
            sm1.unregisterListener(listener1);
            if (video_view.isPlaying()) {
                video_view.pause();
                handler.removeMessages(MESSAGE_UPDATE_PLAY_PROGRESS);
            }
            if (!isKeyDown) {
                finish();
            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        unregisterReceiver(mVolumeReceiver);
        sm.unregisterListener(listener);
        sm1.unregisterListener(listener1);
        getApplication();
        getApplicationContext();
    }

    private class MyGestureLitener extends SimpleOnGestureListener {

        /**
         * 滑动 改变
         */

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if (firstScroll) {// 以触摸屏幕后第一次滑动为标准，避免在屏幕上操作切换混乱
                if (Math.abs(distanceX) >= Math.abs(distanceY)) {

                    GESTURE_FLAG = GESTURE_MODIFY_PROGRESS;

                } else {
                    // 左右屏幕滑动
                    Display displayMetrics = getWindowManager()
                            .getDefaultDisplay();
                    screenWidth = displayMetrics.getWidth();
                    screenHeight = displayMetrics.getHeight();
                    if (getScreenDirection()
                            && listview_video_show.getVisibility() == View.VISIBLE) {
                        screenWidth = screenWidth * 2 / 3;
                    }

                    if (e1.getX() >= screenWidth * 2 / 3) {

                        GESTURE_FLAG = GESTURE_MODIFY_VOLUME;

                    } else if (e1.getX() <= screenWidth / 3) {

                        GESTURE_FLAG = GESTURE_MODIFY_LIGHTNESS;
                    }

                }
            }
            // 如果每次触摸屏幕后第一次scroll是调节进度，那之后的scroll事件都处理音量进度，直到离开屏幕执行下一次操作
            if (GESTURE_FLAG == GESTURE_MODIFY_PROGRESS) {
                // distanceX=lastScrollPositionX-currentScrollPositionX，因此为正时是快进

                int cunrentPos = video_view.getCurrentPosition();
                int allTime = video_view.getDuration();
                int resId = R.drawable.img_back;
                if (Math.abs(distanceX) > Math.abs(distanceY)) {// 横向移动大于纵向移动

                    if (distanceX >= MeasureUtil.dip2px(
                            VideoPlayerActivity.this, STEP_PROGRESS)
                            && cunrentPos > 3 * 1000) {
                        // 快退，用步长控制改变速度，可微调,e1初次触控地图的event1
                        // e2每次触发onScroll函数得到的的event2
                        // distance是上一次的event2减去当前event2得到的结果//注意到顺序 lastEvent2
                        // -event2 =distance

                        cunrentPos -= 3 * 1000;// scroll方法执行一次快退3秒

                        // 改变图片
                        resId = R.drawable.img_back;
                    } else if (distanceX <= -MeasureUtil.dip2px(
                            VideoPlayerActivity.this, STEP_PROGRESS)
                            && cunrentPos + 3 * 1000 < allTime) {// 快进

                        cunrentPos += 3 * 1000;// scroll执行一次快进3秒
                        resId = R.drawable.img_forword;
                    }

                }

                video_view.seekTo(cunrentPos);
                video_seekbar.setProgress(video_view.getCurrentPosition());
                updataForwordBack(
                        StringUtil.formatVideoDuration(video_view
                                .getCurrentPosition())
                                + " | "
                                + StringUtil.formatVideoDuration(video_view
                                .getDuration()), resId);

                // geture_tv_progress_time.setText(converLongTimeToStr(palyerChangePosition)+
                // "/" + converLongTimeToStr(playerDuration));

                // 手动拉速度时，显示缓冲进度条
                // gesture_progress_layout.setVisibility(View.VISIBLE);

            }
            // 如果每次触摸屏幕后第一次scroll是调节音量，那之后的scroll事件都处理音量调节，直到离开屏幕执行下一次操作
            else if (GESTURE_FLAG == GESTURE_MODIFY_VOLUME) {
                currentVolume = audioManager
                        .getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
                if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                    if (distanceY >= MeasureUtil.dip2px(
                            VideoPlayerActivity.this, STEP_VOLUME)) {// 音量调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                        if (currentVolume < maxVolume) {// 为避免调节过快，distanceY应大于一个设定值
                            currentVolume++;
                        }

                    } else if (distanceY <= -MeasureUtil.dip2px(
                            VideoPlayerActivity.this, STEP_VOLUME)) {// 音量调小
                        if (currentVolume > 0) {
                            currentVolume--;
                        }
                    }
                    float tmpcurrentVolume = (float) currentVolume;
                    float tmpmaxVolume = (float) maxVolume;
                    int percentage = (int) (tmpcurrentVolume * 100 / tmpmaxVolume);

                    // 设置声音大小
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            currentVolume, 0);
                    updataVolumeBrightBar(percentage, R.drawable.volume_middle);

                }

            }
            // 如果每次触摸屏幕后第一次scroll是调节亮度，那之后的scroll事件都处理亮度调节，直到离开屏幕执行下一次操作
            else if (GESTURE_FLAG == GESTURE_MODIFY_LIGHTNESS) {

                if (mBrightness < 0) {
                    // 获取系统亮度
                    mBrightness = getWindow().getAttributes().screenBrightness;
                    if (mBrightness <= 0.00f)
                        mBrightness = 0.5f;
                    if (mBrightness < 0.0f)
                        mBrightness = 0.0f;
                }
                float percent;
                // 按照px值进行转换
                if (distanceY >= MeasureUtil.dip2px(VideoPlayerActivity.this,
                        STEP_BRIGHTNESS)
                        || distanceY <= -MeasureUtil.dip2px(
                        VideoPlayerActivity.this, STEP_BRIGHTNESS)) {
                    // 亮度调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                    // 根据滑动高度百分比添加
                    if (distanceY > 0)
                        percent = 0.02f;
                    else
                        percent = -0.02f;
                    WindowManager.LayoutParams lpa = getWindow()
                            .getAttributes();
                    mBrightness = mBrightness + percent;
                    if (mBrightness > 1.0f)
                        mBrightness = 1.0f;
                    else if (mBrightness <= 0.0f)
                        mBrightness = 0.0f;
                    lpa.screenBrightness = mBrightness;
                    // 设置屏幕亮度
                    getWindow().setAttributes(lpa);
                    updataVolumeBrightBar((int) (lpa.screenBrightness * 100),
                            R.drawable.bright);
                }

            }

            firstScroll = false;// 第一次scroll执行完成，修改标志
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            firstScroll = true;// 设定是触摸屏幕后第一次scroll的标志
            handler.removeMessages(CLOSE_VIDEO_LIST);
            return super.onDown(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            processClick(btn_play);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            processClick(btn_screen);
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            if (ll_top_control.getVisibility() == View.VISIBLE && ll_bottom_control.getVisibility() == View.VISIBLE) {
                // 隐藏操作
                hideControlLayout();
            } else {
                // 显示操作
                showControlLayout();
            }

            //也要关闭横屏的列表
            if (!stretch_flag && listview_video_show.getVisibility() == View.VISIBLE) {
                setVideoFullSreen();
            }

            return super.onSingleTapConfirmed(e);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setVideoSetting();
    }

    /**
     * 显示控制面板
     */
    private void showControlLayout() {

        // ViewPropertyAnimator.animate(ll_top_control).translationY(0)
        // .setDuration(100);
        // ViewPropertyAnimator.animate(ll_bottom_control).translationY(0)
        // .setDuration(100);
        ll_top_control.setVisibility(View.VISIBLE);
        ll_bottom_control.setVisibility(View.VISIBLE);
        handler.sendEmptyMessageDelayed(MESSAGE_HIDE_CONTROL, HIDE_CONTROL);
    }

    /**
     * 隐藏控制面板
     */
    private void hideControlLayout() {
        // ViewPropertyAnimator.animate(ll_top_control)
        // .translationY(-ll_top_control.getHeight()).setDuration(100);
        // ViewPropertyAnimator.animate(ll_bottom_control)
        // .translationY(ll_bottom_control.getHeight()).setDuration(100);
        handler.removeMessages(MESSAGE_HIDE_CONTROL);
        ll_top_control.setVisibility(View.GONE);
        ll_bottom_control.setVisibility(View.GONE);

    }

    /**
     * 更新播放按钮的背景图片
     */
    private void updatePlayBtnBg(boolean isPlay) {
        Drawable drawable;
        if (getScreenDirection()) {
            drawable = getResources().getDrawable(
                    isPlay ? R.drawable.btn_pause_video
                            : R.drawable.btn_play_video);
        } else {
            drawable = getResources().getDrawable(
                    isPlay ? R.drawable.btn_pause_small
                            : R.drawable.btn_play_small);
        }

        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                drawable.getMinimumHeight());
        btn_play.setCompoundDrawables(drawable, null, null, null);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            finishVideoCheck(true);

        }
        if (getScreenDirection()) {
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

	/*
     * 重力感应部分
	 */

    /**
     * 重力感应监听者
     */
    public class OrientationSensorListener implements SensorEventListener {
        private static final int _DATA_X = 0;
        private static final int _DATA_Y = 1;
        private static final int _DATA_Z = 2;

        public static final int ORIENTATION_UNKNOWN = -1;

        private Handler rotateHandler;

        public OrientationSensorListener(Handler handler) {
            rotateHandler = handler;
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }

        public void onSensorChanged(SensorEvent event) {

            if (sensor_flag != stretch_flag) // 只有两个不相同才开始监听行为
            {
                float[] values = event.values;
                int orientation = ORIENTATION_UNKNOWN;
                float X = -values[_DATA_X];
                float Y = -values[_DATA_Y];
                float Z = -values[_DATA_Z];
                float magnitude = X * X + Y * Y;
                // Don't trust the angle if the magnitude is small compared to
                // the y value
                if (magnitude * 4 >= Z * Z) {
                    // 屏幕旋转时
                    float OneEightyOverPi = 57.29577957855f;
                    float angle = (float) Math.atan2(-Y, X) * OneEightyOverPi;
                    orientation = 90 - (int) Math.round(angle);
                    // normalize to 0 - 359 range
                    while (orientation >= 360) {
                        orientation -= 360;
                    }
                    while (orientation < 0) {
                        orientation += 360;
                    }
                }
                if (rotateHandler != null) {
                    rotateHandler.obtainMessage(CHANGE_SREEN, orientation, 0)
                            .sendToTarget();
                }

            }
        }
    }

    public class OrientationSensorListener2 implements SensorEventListener {
        private static final int _DATA_X = 0;
        private static final int _DATA_Y = 1;
        private static final int _DATA_Z = 2;

        public static final int ORIENTATION_UNKNOWN = -1;

        public void onAccuracyChanged(Sensor arg0, int arg1) {
            // TODO Auto-generated method stub

        }

        public void onSensorChanged(SensorEvent event) {

            float[] values = event.values;

            int orientation = ORIENTATION_UNKNOWN;
            float X = -values[_DATA_X];
            float Y = -values[_DATA_Y];
            float Z = -values[_DATA_Z];

            /**
             * 这一段据说是 android源码里面拿出来的计算 屏幕旋转的 不懂 先留着 万一以后懂了呢
             */
            float magnitude = X * X + Y * Y;
            // Don't trust the angle if the magnitude is small compared to the y
            // value
            if (magnitude * 4 >= Z * Z) {
                // 屏幕旋转时
                float OneEightyOverPi = 57.29577957855f;
                float angle = (float) Math.atan2(-Y, X) * OneEightyOverPi;
                orientation = 90 - (int) Math.round(angle);
                // normalize to 0 - 359 range
                while (orientation >= 360) {
                    orientation -= 360;
                }
                while (orientation < 0) {
                    orientation += 360;
                }
            }

            if (orientation > 225 && orientation < 315) { // 横屏
                sensor_flag = false;
            } else if ((orientation > 315 && orientation < 360)
                    || (orientation > 0 && orientation < 45)) { // 竖屏
                sensor_flag = true;
            }

            if (stretch_flag == sensor_flag) { // 点击变成横屏 屏幕 也转横屏 激活
                // System.out.println("激活");
                sm.registerListener(listener, sensor,
                        SensorManager.SENSOR_DELAY_UI);

            }
        }
    }

    /**
     * 学习进度更新失败! 学习总时长更新失败! （这两个就要冲重传记录） 该课程不存在，无法记录学时!(既不重传，也不记录)
     *
     * @param //endTimes
     * @param //startTimes
     */
    // 计算并且上传
//	protected void calculateAndUpload(long startTimes, long endTimes) {
//
//		useTime = (endTimes - startTimes) / 1000 - 1;
//		// Toast.makeText(getApplicationContext(), useTime + "", 3000).show();
//		// 小于两秒不提交
//		if (useTime >= 1) {
//			try {
//				String useTimeString = mDbDao.getStudyProgress(userId,
//						lessonId, lessonItemId);
//				if (useTimeString != null) {
//					Long useTimes = Long.parseLong(useTimeString);
//					useTime += useTimes;
//				}
//				String url = Const.PATH + Const.PATH_LEARNINGPROGRESS;
//				HttpUtils httpUtil = new HttpUtils();
//				// 设置当前请求的缓存时间
//				httpUtil.configCurrentHttpCacheExpiry(0);
//				// 设置默认请求的缓存时间
//				httpUtil.configDefaultHttpCacheExpiry(0);
//				// 设置线程数
//				httpUtil.configRequestThreadPoolSize(1);
//				RequestParams params = new RequestParams();
//				lessonId = videoList.get(currentIndex).getLE_ID();
//				lessonItemId = videoList.get(currentIndex).getID();
//				params.addQueryStringParameter("userid", userId);
//				params.addQueryStringParameter("lessonId", lessonId);
//				params.addQueryStringParameter("lessonItemId", lessonItemId);
//				params.addQueryStringParameter("learningtimes", useTime + "");
//				httpUtil.send(HttpRequest.HttpMethod.GET, url, params,
//						new RequestCallBack<String>() {
//							@Override
//							public void onFailure(HttpException arg0,
//									String arg1) {
//								// 如果提交失败的话先存在数据库中
//								mDbDao.insertStudyProgress(new StudyProgressBean(
//										userId, lessonId, lessonItemId, useTime
//												+ ""));
//							}
//
//							@Override
//							public void onSuccess(ResponseInfo<String> res) {
//								Log.e("sen", res.result);
//								// pareseJason(res.result);
//							}
//						});
//			} catch (Exception e) {
//				// 如果提交失败的话先存在数据库中
//				mDbDao.insertStudyProgress(new StudyProgressBean(userId,
//						lessonId, lessonItemId, useTime + ""));
//			}
//		}
//	}
//
//	protected void pareseJason(String result) {
//		Log.e("sen", result);
//		JSONObject jsonObject;
//		try {
//			jsonObject = new JSONObject(result);
//			String msg = jsonObject.getString("msg");
//			String success = jsonObject.getString("success");
//			if ("true".equals(success) && "学习数据更新成功！".equals(msg)) {
//				Toast.makeText(getApplicationContext(), "更新学分成功！", 3000).show();
//			}
//			if ("false".equals(success)) {
//				if (!"学习进度更新失败!".equals(msg) || !"学习总时长更新失败!".equals(msg)
//						|| !"该课程不存在，无法记录学时!".equals(msg)) {
//					// 如果不是这些的话，那么就要删除原来的
//					// Toast.makeText(getApplicationContext(), useTime + "",
//					// 3000).show();
//					mDbDao.deleteStudyProgress(userId, lessonId, lessonItemId);
//				} else if ("学习进度更新失败!".equals(msg) || "学习总时长更新失败!".equals(msg)) {
//					mDbDao.insertStudyProgress(new StudyProgressBean(userId,
//							lessonId, lessonItemId, useTime + ""));
//				}
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}

    //使用cordova webview
    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    @Override
    public Object onMessage(String id, Object data) {
        if ("exit".equals(id)) {
            super.finish();
        }
        return null;
    }

    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {
        if (activityResultCallback != null) {
            activityResultCallback.onActivityResult(activityResultRequestCode, Activity.RESULT_CANCELED, null);
        }
        this.activityResultCallback = plugin;
    }

    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        setActivityResultCallback(command);
        try {
            startActivityForResult(intent, requestCode);
        } catch (RuntimeException e) {
            activityResultCallback = null;
            throw e;
        }
    }

}
