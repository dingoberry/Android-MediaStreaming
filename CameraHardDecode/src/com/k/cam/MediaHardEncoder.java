package com.k.cam;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import com.bbq.w.library.LogLib;
import com.bbq.w.library.ThreadUtils;

@TargetApi(16)
@SuppressWarnings("deprecation")
public class MediaHardEncoder implements MediaHardCore, Runnable {

	private final static String TAG = "MediaHardEncoder";
	private static final boolean DEBUG = Configuration.DEBUG;

	private MediaCodec mCodec;
	private EncodeDataReceiver mDataReceiver;

	private int mGeneratedIndex;
	private int mColorFormat;
	private AtomicBoolean mIsEncoding = new AtomicBoolean();

	private MediaCodec resolveEncodeType(String encodeType, int w, int h)
			throws IOException {
		MediaCodecInfo codecInfo = null;
		boolean found = false;
		Outer: for (int i = 0, count = MediaCodecList.getCodecCount(); i < count; i++) {
			codecInfo = MediaCodecList.getCodecInfoAt(i);
			if (!codecInfo.isEncoder()) {
				continue;
			}

			for (String type : codecInfo.getSupportedTypes()) {
				if (DEBUG) {
					LogLib.d(TAG, "i=" + i + "; type:" + type);
				}
				if (encodeType.equalsIgnoreCase(type)) {
					found = true;
					encodeType = type;
					break Outer;
				}
			}
		}

		if (!found) {
			return null;
		}
		if (DEBUG) {
			LogLib.d(TAG, "codecInfo:" + codecInfo.getName());
		}
		int[] colorFormats = codecInfo.getCapabilitiesForType(encodeType).colorFormats;

		int colorFormat = Integer.MAX_VALUE;
		for (int format : colorFormats) {
			if (format < colorFormat) {
				colorFormat = format;
			}
		}
		if (colorFormat == Integer.MAX_VALUE) {
			return null;
		}

		mColorFormat = colorFormat;
		MediaCodec mediaCodec = MediaCodec.createEncoderByType(encodeType);
		MediaFormat mediaFormat = MediaFormat.createVideoFormat(encodeType,
				w, h);
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
		mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
		mediaCodec.configure(mediaFormat, null, null,
				MediaCodec.CONFIGURE_FLAG_ENCODE);
		return mediaCodec;
	}
	
	public int getColorFormat() {
		return mColorFormat;
	}

	public void setEncodeDataReceiver(EncodeDataReceiver receiver) {
		mDataReceiver = receiver;
	}

	public boolean startEncode(int width, int height) {
		if (!mIsEncoding.compareAndSet(false, true)) {
			return false;
		}
		if (DEBUG) {
			LogLib.d(TAG, "MediaDataHardProc:build");
		}
		boolean result;

		try {
			MediaCodec mediaCodec = resolveEncodeType(ENCODER_TYPE, width,
					height);
			if (mediaCodec == null) {
				mIsEncoding.set(false);
				return false;
			}

			mGeneratedIndex = 0;

			mediaCodec.start();
			mCodec = mediaCodec;
			ThreadUtils.runAloneThread(this);
			result = true;
		} catch (IOException e) {
			if (DEBUG) {
				LogLib.w(TAG, e);
			}
			mIsEncoding.set(false);
			result = false;
		}
		return result;
	}

	private static long computePresentationTime(int frameIndex) {
//		return 132 + frameIndex * 1000000 / FRAME_RATE;
		return 0;
	}

	public synchronized void feedData(byte[] data) {
		MediaCodec mediaCodec = mCodec;
		if (mediaCodec == null) {
			return;
		}

		ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
		int inputIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
		if (DEBUG) {
			LogLib.d(TAG, "MediaDataHardProc:inputIndex=" + inputIndex);
			LogLib.d(TAG, "MediaDataHardProc:inputBuffers="
					+ inputBuffers.length);
		}
		if (inputIndex >= 0) {
			ByteBuffer inBuffer = inputBuffers[inputIndex];
			inBuffer.clear();
			inBuffer.put(data);
			mediaCodec.queueInputBuffer(inputIndex, 0, data.length,
					computePresentationTime(mGeneratedIndex), 0);
			mGeneratedIndex++;
		}
	}

	public void stopEncode() {
		if (DEBUG) {
			LogLib.d(TAG, "MediaDataHardProc:stopEncode");
		}

		mIsEncoding.set(false);
		MediaCodec mediaCodec = mCodec;
		if (mediaCodec != null) {
			mediaCodec.stop();
		}
	}

	@Override
	public void run() {
		try {
			BufferInfo bufferInfo = new BufferInfo();
			MediaCodec mediaCodec = mCodec;
			if (mediaCodec == null) {
				return;
			}

			int index;
			ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();

			byte[] data;
			while (mIsEncoding.get()) {
				if (DEBUG) {
					LogLib.d(TAG, "run:" + mIsEncoding.get());
				}
				index = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
				if (DEBUG) {
					LogLib.d(TAG, "output buffer:" + index);
				}
				if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					outputBuffers = mediaCodec.getOutputBuffers();
				} else if (index >= 0) {
					EncodeDataReceiver receiver = mDataReceiver;
					if (receiver != null) {
						ByteBuffer buffer = outputBuffers[index];
						buffer.position(bufferInfo.offset);
						buffer.limit(bufferInfo.offset + bufferInfo.size);

						data = new byte[bufferInfo.size];
						buffer.get(data);
						receiver.receiveData(data);
					}
					mediaCodec.releaseOutputBuffer(index, false);
					synchronized (this) {
						mediaCodec.flush();
					}
				}
			}
		} catch (IllegalStateException e) {
			LogLib.w(TAG, e);
		}
	}

	public interface EncodeDataReceiver {
		void receiveData(byte[] data);
	}
}
