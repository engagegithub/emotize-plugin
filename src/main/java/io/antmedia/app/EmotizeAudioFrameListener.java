package io.antmedia.app;

import org.bytedeco.ffmpeg.avutil.AVFrame;

import org.bytedeco.javacpp.BytePointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.antmedia.plugin.api.IFrameListener;
import io.antmedia.plugin.api.StreamParametersInfo;

import com.assemblyai.api.RealtimeTranscriber;

public class EmotizeAudioFrameListener implements IFrameListener{

	protected static Logger logger = LoggerFactory.getLogger(EmotizeAudioFrameListener.class);

	private int audioFrameCount = 0;
	private RealtimeTranscriber transcriber;

	public EmotizeAudioFrameListener(RealtimeTranscriber transcriber) {
		this.transcriber = transcriber;
	}

	@Override
	public AVFrame onAudioFrame(String streamId, AVFrame audioFrame) {
		audioFrameCount++;
		sendAVFrameToWssClient(audioFrame);

		return audioFrame;
	}

	@Override
	public AVFrame onVideoFrame(String streamId, AVFrame videoFrame) {
		return videoFrame;
	}

	@Override
	public void writeTrailer(String streamId) {
		logger.info("***emotizeplugin*** EmotizeAudioFrameListener.writeTrailer() for streamId:{}", streamId);
	}

	@Override
	public void setVideoStreamInfo(String streamId, StreamParametersInfo videoStreamInfo) {
		logger.info("***emotizeplugin*** EmotizeAudioFrameListener.setVideoStreamInfo() for streamId:{}", streamId);
	}

	@Override
	public void setAudioStreamInfo(String streamId, StreamParametersInfo audioStreamInfo) {
		logger.info("***emotizeplugin*** EmotizeAudioFrameListener.setAudioStreamInfo() for streamId:{}", streamId);
	}

	@Override
	public void start() {
		logger.info("***emotizeplugin*** EmotizeAudioFrameListener.start()");
	}

	public String getStats() {
		return "audio frames:"+audioFrameCount+"\t";
	}

	private void sendAVFrameToWssClient(AVFrame avFrame) {
		try {
			BytePointer buffer = avFrame.data(0);
			int linesize = avFrame.linesize(0);
			byte[] audioData = new byte[linesize];

			if (buffer != null && !buffer.isNull() && linesize > 0) {
				buffer.get(audioData, 0, linesize);

				logger.info("***emotizeplugin*** transcriber: " + transcriber);
				transcriber.sendAudio(audioData);
			} else {
				logger.info("***emotizeplugin*** Empty audio data");
			}
		} catch (Exception e) {
			e.printStackTrace();
			// logger.error("***emotizeplugin*** Failed to sent AVFrame to Assembly. Ex: " + e);
		}
	}
}
