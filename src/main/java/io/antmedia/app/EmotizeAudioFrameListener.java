package io.antmedia.app;

import static org.bytedeco.ffmpeg.global.avutil.av_get_bytes_per_sample;

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
	private int loggerCount = 0;
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
			int samples = avFrame.nb_samples();
			int data_size = av_get_bytes_per_sample(avFrame.format()) * samples;
			BytePointer buffer = avFrame.data(0);
			byte[] byteData = new byte[data_size];

			if (buffer != null) {
				buffer.get(byteData);

				transcriber.sendAudio(byteData);
				if (loggerCount < 50) {
					logger.info("***emotizeplugin*** audio data sent" + byteData);

					loggerCount++;
				}
			} else {
				logger.info("***emotizeplugin*** Empty audio data");
			}
		} catch (Exception e) {
			e.printStackTrace();
			// logger.error("***emotizeplugin*** Failed to sent AVFrame to Assembly. Ex: " + e);
		}
	}
}
