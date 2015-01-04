package com.mediedictionary.playerlibrary;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.mediedictionary.playerlibrary.PlayerView.OnChangeListener;

public class PlayerActivity extends Activity implements OnChangeListener, OnClickListener, OnSeekBarChangeListener, Callback {

	private static final int SHOW_PROGRESS = 0;
	private static final int ON_LOADED = 1;
	private static final int HIDE_OVERLAY = 2;

	private View rlLoading;
	private PlayerView mPlayerView;
	private String mUrl;
	private TextView tvTitle, tvBuffer, tvTime, tvLength;
	private SeekBar sbVideo;
	private ImageButton ibLock, ibFarward, ibBackward, ibPlay, ibSize;
	private View llOverlay, rlOverlayTitle;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUrl = getIntent().getStringExtra("url");
		if (TextUtils.isEmpty(mUrl)) {
			Toast.makeText(this, "error:no url in intent!", Toast.LENGTH_SHORT).show();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_player);

		mHandler = new Handler(this);

		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTime = (TextView) findViewById(R.id.tv_time);
		tvLength = (TextView) findViewById(R.id.tv_length);
		sbVideo = (SeekBar) findViewById(R.id.sb_video);
		sbVideo.setOnSeekBarChangeListener(this);
		ibLock = (ImageButton) findViewById(R.id.ib_lock);
		ibLock.setOnClickListener(this);
		ibBackward = (ImageButton) findViewById(R.id.ib_backward);
		ibBackward.setOnClickListener(this);
		ibPlay = (ImageButton) findViewById(R.id.ib_play);
		ibPlay.setOnClickListener(this);
		ibFarward = (ImageButton) findViewById(R.id.ib_forward);
		ibFarward.setOnClickListener(this);
		ibSize = (ImageButton) findViewById(R.id.ib_size);
		ibSize.setOnClickListener(this);

		llOverlay = findViewById(R.id.ll_overlay);
		rlOverlayTitle = findViewById(R.id.rl_title);

		rlLoading = findViewById(R.id.rl_loading);
		tvBuffer = (TextView) findViewById(R.id.tv_buffer);
		//使用步骤
		//第一步 ：通过findViewById或者new PlayerView()得到mPlayerView对象
		//mPlayerView= new PlayerView(PlayerActivity.this);
		mPlayerView = (PlayerView) findViewById(R.id.pv_video);

		//第二步：设置参数，毫秒为单位
		mPlayerView.setNetWorkCache(20000);

		//第三步:初始化播放器
		mPlayerView.initPlayer(mUrl);

		//第四步:设置事件监听，监听缓冲进度等
		mPlayerView.setOnChangeListener(this);

		//第五步：开始播放
		mPlayerView.start();

		//init view
		tvTitle.setText(mUrl);
		showLoading();
		hideOverlay();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (llOverlay.getVisibility() != View.VISIBLE) {
				showOverlay();
			} else {
				hideOverlay();
			}
		}
		return false;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		mPlayerView.changeSurfaceSize();
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onPause() {
		hideOverlay();
		mPlayerView.stop();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBufferChanged(float buffer) {
		if (buffer >= 100) {
			hideLoading();
		} else {
			showLoading();
		}
		tvBuffer.setText("正在缓冲中..." + (int) buffer + "%");
	}

	private void showLoading() {
		rlLoading.setVisibility(View.VISIBLE);

	}

	private void hideLoading() {
		rlLoading.setVisibility(View.GONE);
	}

	@Override
	public void onLoadComplet() {
		mHandler.sendEmptyMessage(ON_LOADED);
	}

	@Override
	public void onError() {
		Toast.makeText(getApplicationContext(), "Player Error Occur！", Toast.LENGTH_SHORT).show();
		finish();
	}

	@Override
	public void onEnd() {
		finish();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ib_lock:
			break;
		case R.id.ib_forward:
			mPlayerView.seek(10000);
			break;
		case R.id.ib_play:
			if (mPlayerView.isPlaying()) {
				mPlayerView.pause();
				ibPlay.setBackgroundResource(R.drawable.ic_play);
			} else {
				mPlayerView.play();
				ibPlay.setBackgroundResource(R.drawable.ic_pause);
			}
			break;

		case R.id.ib_backward:
			mPlayerView.seek(-10000);
			break;
		case R.id.ib_size:
			break;
		default:
			break;
		}
	}

	private void showOverlay() {
		rlOverlayTitle.setVisibility(View.VISIBLE);
		llOverlay.setVisibility(View.VISIBLE);
		mHandler.sendEmptyMessage(SHOW_PROGRESS);
		mHandler.removeMessages(HIDE_OVERLAY);
		mHandler.sendEmptyMessageDelayed(HIDE_OVERLAY, 5 * 1000);
	}

	private void hideOverlay() {
		rlOverlayTitle.setVisibility(View.GONE);
		llOverlay.setVisibility(View.GONE);
		mHandler.removeMessages(SHOW_PROGRESS);
	}

	private int setOverlayProgress() {
		if (mPlayerView == null) {
			return 0;
		}
		int time = (int) mPlayerView.getTime();
		int length = (int) mPlayerView.getLength();
		boolean isSeekable = mPlayerView.canSeekable() && length > 0;
		ibFarward.setVisibility(isSeekable ? View.VISIBLE : View.GONE);
		ibBackward.setVisibility(isSeekable ? View.VISIBLE : View.GONE);
		sbVideo.setMax(length);
		sbVideo.setProgress(time);
		if (time >= 0) {
			tvTime.setText(millisToString(time, false));
		}
		if (length >= 0) {
			tvLength.setText(millisToString(length, false));
		}
		return time;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser && mPlayerView.canSeekable()) {
			mPlayerView.setTime(progress);
			setOverlayProgress();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case SHOW_PROGRESS:
			setOverlayProgress();
			mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 20);
			break;
		case ON_LOADED:
			showOverlay();
			hideLoading();
			break;
		case HIDE_OVERLAY:
			hideOverlay();
			break;
		default:
			break;
		}
		return false;
	}

	private String millisToString(long millis, boolean text) {
		boolean negative = millis < 0;
		millis = java.lang.Math.abs(millis);
		int mini_sec = (int) millis % 1000;
		millis /= 1000;
		int sec = (int) (millis % 60);
		millis /= 60;
		int min = (int) (millis % 60);
		millis /= 60;
		int hours = (int) millis;

		String time;
		DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		format.applyPattern("00");

		DecimalFormat format2 = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		format2.applyPattern("000");
		if (text) {
			if (millis > 0)
				time = (negative ? "-" : "") + hours + "h" + format.format(min) + "min";
			else if (min > 0)
				time = (negative ? "-" : "") + min + "min";
			else
				time = (negative ? "-" : "") + sec + "s";
		} else {
			if (millis > 0)
				time = (negative ? "-" : "") + hours + ":" + format.format(min) + ":" + format.format(sec) + ":" + format2.format(mini_sec);
			else
				time = (negative ? "-" : "") + min + ":" + format.format(sec) + ":" + format2.format(mini_sec);
		}
		return time;
	}
}
