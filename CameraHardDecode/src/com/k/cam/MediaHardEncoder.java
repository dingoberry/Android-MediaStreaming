package com.k.cam;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import com.bbq.w.library.LogLib;

@TargetApi(16)
@SuppressWarnings("deprecation")
public class MediaHardEncoder implements MediaHardCore{

	private final static String ENCODER_TYPE = "Video/AVC";
	private MediaCodec mCodec;
	private int mColorFormat;

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
				LogLib.d("i=" + i + "; type:" + type);
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

		LogLib.d("codecInfo:" + codecInfo.getName());
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
		MediaFormat mediaFormat = MediaFormat.createVideoFormat(ENCODER_TYPE,
				w, h);
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
		mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
				colorFormat);
		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
		mediaCodec.configure(mediaFormat, null, null,
				MediaCodec.CONFIGURE_FLAG_ENCODE);
		return mediaCodec;
	}

	public int getColorFormat() {
		return mColorFormat;
	}

	public boolean build(int width, int height) {
		LogLib.d("MediaDataHardProc:build");
		boolean result;
		try {
			MediaCodec mediaCodec = resolveEncodeType(ENCODER_TYPE, width,
					height);
			if (mediaCodec == null) {
				return false;
			}

			mediaCodec.start();
			mCodec = mediaCodec;
			result = true;
		} catch (IOException e) {
			LogLib.w(e);
			result = false;
		}
		return result;
	}

	public void feedData(byte[] data) {
		LogLib.d("MediaDataHardProc:feedData");
		MediaCodec mediaCodec = mCodec;
		if (mediaCodec == null) {
			return;
		}
		
		ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
		int inputIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
		LogLib.d("MediaDataHardProc:inputIndex=" + inputIndex);
		LogLib.d("MediaDataHardProc:inputBuffers=" + inputBuffers.length);
		if (inputIndex >= 0) {
			ByteBuffer inBuffer = inputBuffers[inputIndex];
			inBuffer.clear();
			inBuffer.put(data, 0, data.length);
			mediaCodec.queueInputBuffer(inputIndex, 0, data.length, 0, 0);
		}
		
		BufferInfo bufferInfo = new BufferInfo();
		ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
		int outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
		LogLib.d("MediaDataHardProc:outputIndex=" + outputIndex);
		LogLib.d("MediaDataHardProc:outputBuffers=" + outputBuffers.length);
		while (outputIndex >= 0) {
			ByteBuffer outBuffer = outputBuffers[outputIndex];
			LogLib.d("outBuffer : " + outBuffer.capacity());
			mediaCodec.releaseOutputBuffer(outputIndex, false);
			outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
		}
	}

	public void stopEncode() {
		LogLib.d("MediaDataHardProc:stopEncode");
		
		MediaCodec mediaCodec = mCodec;
		if (mediaCodec != null) {
			mediaCodec.stop();
		}
	}
}
