package com.mediedictionary.playerlibrary;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.LibVlcUtil;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.FrameLayout;

public class PlayerView extends FrameLayout implements IVideoPlayer, android.os.Handler.Callback {

	private static final String TAG = "PlayerView";

	public interface OnChangeListener {
		public void OnLoadComplet();

		public void OnError();
	}

	private static final int SURFACE_SIZE = 0;

	private static final int SURFACE_BEST_FIT = 0;
	private static final int SURFACE_FIT_HORIZONTAL = 1;
	private static final int SURFACE_FIT_VERTICAL = 2;
	private static final int SURFACE_FILL = 3;
	private static final int SURFACE_16_9 = 4;
	private static final int SURFACE_4_3 = 5;
	private static final int SURFACE_ORIGINAL = 6;
	private int mCurrentSize = SURFACE_BEST_FIT;

	private LibVLC mLibVLC;

	private SurfaceView mSurface;
	private SurfaceView mSubtitlesSurface;

	private SurfaceHolder mSurfaceHolder;
	private SurfaceHolder mSubtitlesSurfaceHolder;

	private FrameLayout mSurfaceFrame;

	// size of the video
	private int mVideoHeight;
	private int mVideoWidth;
	private int mVideoVisibleHeight;
	private int mVideoVisibleWidth;
	private int mSarNum;
	private int mSarDen;

	private Handler mHandler;

	OnChangeListener mOnChangeListener;
	private boolean mCanSeek = false;

	public void setOnChangeListener(OnChangeListener listener) {
		mOnChangeListener = listener;
	}

	public PlayerView(Context context) {
		super(context);
		init();
	}

	public PlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public PlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	public void setDataSource(String url) {
		mLibVLC.getMediaList().clear();
		mLibVLC.getMediaList().add(url);
	}

	@SuppressLint({ "InlinedApi", "NewApi" })
	private void init() {
		try {
			mLibVLC = LibVLC.getExistingInstance();
			if (mLibVLC == null) {
				mLibVLC = LibVLC.getInstance();
				mLibVLC.init(getContext().getApplicationContext());
				EventHandler.getInstance().addHandler(eventHandler);
			}
		} catch (LibVlcException e) {
			Log.e(TAG, "init failed", e);
			return;
		}

		LayoutInflater.from(getContext()).inflate(R.layout.view_player, this);
		mHandler = new Handler(this);

		mSurface = (SurfaceView) findViewById(R.id.player_surface);
		mSurfaceHolder = mSurface.getHolder();
		mSurfaceHolder.addCallback(mSurfaceCallback);
		mSurfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);

		String chroma = "";// mSettings.getString("chroma_format", "");
		if (LibVlcUtil.isGingerbreadOrLater() && chroma.equals("YV12")) {
			mSurfaceHolder.setFormat(ImageFormat.YV12);
		} else if (chroma.equals("RV16")) {
			mSurfaceHolder.setFormat(PixelFormat.RGB_565);
		} else {
			mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
		}

		mSubtitlesSurface = (SurfaceView) findViewById(R.id.subtitles_surface);
		mSubtitlesSurfaceHolder = mSubtitlesSurface.getHolder();
		mSubtitlesSurfaceHolder.setFormat(PixelFormat.RGBA_8888);
		mSubtitlesSurface.setZOrderMediaOverlay(true);
		mSurfaceHolder.addCallback(mSurfaceCallback);
		mSubtitlesSurfaceHolder.addCallback(mSubtitlesSurfaceCallback);
	}

	private final SurfaceHolder.Callback mSurfaceCallback = new Callback() {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (format == PixelFormat.RGBX_8888)
				Log.d(TAG, "Pixel format is RGBX_8888");
			else if (format == PixelFormat.RGB_565)
				Log.d(TAG, "Pixel format is RGB_565");
			else if (format == ImageFormat.YV12)
				Log.d(TAG, "Pixel format is YV12");
			else
				Log.d(TAG, "Pixel format is other/unknown");
			if (mLibVLC != null) {
				mLibVLC.attachSurface(holder.getSurface(), PlayerView.this);
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mLibVLC != null)
				mLibVLC.detachSurface();
		}
	};

	private final SurfaceHolder.Callback mSubtitlesSurfaceCallback = new Callback() {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (mLibVLC != null)
				mLibVLC.attachSubtitlesSurface(holder.getSurface());
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mLibVLC != null)
				mLibVLC.detachSubtitlesSurface();
		}
	};

	@Override
	public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
		if (width * height == 0)
			return;

		// store video size
		mVideoHeight = height;
		mVideoWidth = width;
		mVideoVisibleHeight = visible_height;
		mVideoVisibleWidth = visible_width;
		mSarNum = sar_num;
		mSarDen = sar_den;
		Message msg = mHandler.obtainMessage(SURFACE_SIZE);
		mHandler.sendMessage(msg);
	}

	@Override
	public boolean handleMessage(Message arg0) {
		switch (arg0.what) {
		case SURFACE_SIZE:
			changeSurfaceSize();
			break;

		default:
			break;
		}
		return false;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void changeSurfaceSize() {
		int sw;
		int sh;

		// get screen size
		sw = getWidth();
		sh = getHeight();

		double dw = sw, dh = sh;
		boolean isPortrait;

		isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

		if (sw > sh && isPortrait || sw < sh && !isPortrait) {
			dw = sh;
			dh = sw;
		}

		// sanity check
		if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
			Log.e(TAG, "Invalid surface size");
			return;
		}

		// compute the aspect ratio
		double ar, vw;
		if (mSarDen == mSarNum) {
			/* No indication about the density, assuming 1:1 */
			vw = mVideoVisibleWidth;
			ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
		} else {
			/* Use the specified aspect ratio */
			vw = mVideoVisibleWidth * (double) mSarNum / mSarDen;
			ar = vw / mVideoVisibleHeight;
		}

		// compute the display aspect ratio
		double dar = dw / dh;

		switch (mCurrentSize) {
		case SURFACE_BEST_FIT:
			if (dar < ar)
				dh = dw / ar;
			else
				dw = dh * ar;
			break;
		case SURFACE_FIT_HORIZONTAL:
			dh = dw / ar;
			break;
		case SURFACE_FIT_VERTICAL:
			dw = dh * ar;
			break;
		case SURFACE_FILL:
			break;
		case SURFACE_16_9:
			ar = 16.0 / 9.0;
			if (dar < ar)
				dh = dw / ar;
			else
				dw = dh * ar;
			break;
		case SURFACE_4_3:
			ar = 4.0 / 3.0;
			if (dar < ar)
				dh = dw / ar;
			else
				dw = dh * ar;
			break;
		case SURFACE_ORIGINAL:
			dh = mVideoVisibleHeight;
			dw = vw;
			break;
		}

		SurfaceView surface;
		SurfaceView subtitlesSurface;
		SurfaceHolder surfaceHolder;
		SurfaceHolder subtitlesSurfaceHolder;
		FrameLayout surfaceFrame;

		surface = mSurface;
		subtitlesSurface = mSubtitlesSurface;
		surfaceHolder = mSurfaceHolder;
		subtitlesSurfaceHolder = mSubtitlesSurfaceHolder;
		surfaceFrame = mSurfaceFrame;

		// force surface buffer size
		surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
		subtitlesSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

		// set display size
		android.view.ViewGroup.LayoutParams lp = surface.getLayoutParams();
		lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
		lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
		surface.setLayoutParams(lp);
		subtitlesSurface.setLayoutParams(lp);

		// set frame size (crop if necessary)
		lp = surfaceFrame.getLayoutParams();
		lp.width = (int) Math.floor(dw);
		lp.height = (int) Math.floor(dh);
		surfaceFrame.setLayoutParams(lp);

		surface.invalidate();
		subtitlesSurface.invalidate();
	}

	public void start() {
		mLibVLC.playIndex(0);
		mSurface.setKeepScreenOn(true);
	};

	public void play() {
		mLibVLC.play();
		mSurface.setKeepScreenOn(false);
	};

	public void pause() {
		mLibVLC.pause();
		mSurface.setKeepScreenOn(false);
	};

	public void stop() {
		mLibVLC.stop();
		mSurface.setKeepScreenOn(false);
	}

	public long getTime() {
		return mLibVLC.getTime();
	}

	public long getLength() {
		return mLibVLC.getLength();
	}

	public void setTime(long time) {
		mLibVLC.setTime(time);
	}

	public void setNetWorkCache(int time) {
		mLibVLC.setNetworkCaching(time);
	}

	public String pathToUrl(String path) {
		return LibVLC.PathToURI(path);
	}

	public boolean canSeekable() {
		return true;//mCanSeek;
	}

	public boolean isPlaying() {
		return mLibVLC.isPlaying();
	}

	public boolean isSeekable() {
		return mLibVLC.isSeekable();
	}

	public int getPlayerState() {
		return mLibVLC.getPlayerState();
	}

	public int getVolume() {
		return mLibVLC.getVolume();
	}

	public void setVolume(int volume) {
		mLibVLC.setVolume(volume);
	}

	public void seek(int delta) {
		// unseekable stream
		if (mLibVLC.getLength() <= 0 /*|| !mCanSeek*/)
			return;

		long position = mLibVLC.getTime() + delta;
		if (position < 0)
			position = 0;
		mLibVLC.setTime(position);
	}

	private Handler eventHandler = new Handler(new android.os.Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			//if (exited)
			//	return;
			switch (msg.getData().getInt("event")) {
			case EventHandler.MediaParsedChanged:
				Log.d(TAG, "MediaParsedChanged");
				break;
			case EventHandler.MediaPlayerPlaying:
				Log.d(TAG, "MediaPlayerPlaying");
				if (mOnChangeListener != null) {
					mOnChangeListener.OnLoadComplet();
				}
				break;
			case EventHandler.MediaPlayerPaused:
				Log.d(TAG, "MediaPlayerPaused");
				break;
			case EventHandler.MediaPlayerStopped:
				Log.d(TAG, "MediaPlayerStopped");
				break;
			case EventHandler.MediaPlayerEndReached:
				Log.d(TAG, "MediaPlayerEndReached");
				break;
			case EventHandler.MediaPlayerVout:
				break;
			case EventHandler.MediaPlayerPositionChanged:
				if (!mCanSeek) {
					mCanSeek = true;
				}
				break;
			case EventHandler.MediaPlayerEncounteredError:
				Log.d(TAG, "MediaPlayerEncounteredError");
				if (mOnChangeListener != null) {
					mOnChangeListener.OnError();
				}
				break;
			case EventHandler.HardwareAccelerationError:
				Log.d(TAG, "HardwareAccelerationError");
				break;
			case EventHandler.MediaPlayerTimeChanged:
				// avoid useless error logs
				break;
			default:
				Log.d(TAG, String.format("Event not handled (0x%x)", msg.getData().getInt("event")));
				break;
			}
			return false;
		}
	});
}
