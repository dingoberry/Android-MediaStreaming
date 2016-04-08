package com.k.cam;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Build;

import com.bbq.w.library.LogLib;
import com.bbq.w.library.ThreadUtils;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MediaHardDecoder implements MediaHardCore, Runnable {

	private final static String TAG = "MediaHardDecoder";
	private final static boolean DEBUG = Configuration.DEBUG;

	private LinkedList<byte[]> mDataQueue = new LinkedList<>();
	private AtomicBoolean mIsDecoding = new AtomicBoolean();
	private MediaCodec mCodec;

	public boolean startDecode(int width, int height, int colorFormat) {
		if (!mIsDecoding.compareAndSet(false, true)) {
			LogLib.d(TAG, "decoding is running, so return.");
			return false;
		}

		boolean result;
		try {
			MediaCodec codec = MediaCodec.createDecoderByType(ENCODER_TYPE);
			MediaFormat format = MediaFormat.createVideoFormat(ENCODER_TYPE,
					width, height);
			format.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
			format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
			format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
			format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
			codec.configure(format, null, null,
					MediaCodec.CONFIGURE_FLAG_ENCODE);
			codec.start();
			mCodec = codec;

			ThreadUtils.runAloneThread(this);
			result = true;
		} catch (IOException e) {
			result = false;
			if (DEBUG) {
				LogLib.w(TAG, e);
			}
		}

		return result;
	}

	public void stopDecode() {
		if (DEBUG) {
			LogLib.d(TAG, "stopDecode");
		}

		mIsDecoding.set(false);
		MediaCodec codec = mCodec;
		if (codec != null) {
			codec.stop();
		}

	}

	public void enqueueData(byte[] data) {
		if (mIsDecoding.get()) {
			mDataQueue.push(data);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		MediaCodec codec = mCodec;
		if (codec == null) {
			LogLib.d(TAG, "mediacodec is null");
			return;
		}

		try {
			BufferInfo info = new BufferInfo();
			while (mIsDecoding.get()) {
				int index = codec.dequeueInputBuffer(TIMEOUT_USEC);
				if (index < 0) {
					continue;
				}
				byte[] data = mDataQueue.poll();
				if (data == null) {
					continue;
				}

				ByteBuffer buffer = codec.getInputBuffers()[index];
				buffer.clear();
				buffer.put(data);
				codec.queueInputBuffer(index, 0, data.length, 0, 0);
				
				index = codec.dequeueOutputBuffer(info, TIMEOUT_USEC);
				if (index >= 0) {
					buffer = codec.getOutputBuffers()[index];
					LogLib.d(TAG, "decode size = " + info.size);
					codec.releaseOutputBuffer(index, false);
					codec.flush();
				}
			}
		} catch (IllegalStateException e) {
			LogLib.w(TAG, e);
		}
	}
}
