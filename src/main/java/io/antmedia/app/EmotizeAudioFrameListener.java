package io.antmedia.app;

import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.javacpp.BytePointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.antmedia.plugin.api.IFrameListener;
import io.antmedia.plugin.api.StreamParametersInfo;

import java.util.Base64;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class EmotizeAudioFrameListener implements IFrameListener{

	protected static Logger logger = LoggerFactory.getLogger(EmotizeAudioFrameListener.class);

	private int audioFrameCount = 0;
	private AssemblyClient wssClient;

	public EmotizeAudioFrameListener(AssemblyClient wssClient) {
		this.wssClient = wssClient;
	}

	@Override
	public AVFrame onAudioFrame(String streamId, AVFrame audioFrame) {
		audioFrameCount++;
		logger.info("***emotizeplugin*** TRIGGERED audio frame listener");
		// sendAVFrameToWssClient(audioFrame);

		return audioFrame;
	}

	@Override
	public AVFrame onVideoFrame(String streamId, AVFrame videoFrame) {
		logger.info("***emotizeplugin*** TRIGGERED video frame listener");
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
		if (avFrame.data(0) != null || avFrame.linesize(0) != 0) {
			BytePointer data = avFrame.data(0);

			// Convert the data to a byte array
			byte[] dataBytes = new byte[avFrame.linesize(0)];
			data.get(dataBytes);

			// Encode the byte array to base64
			String base64Data = Base64.getEncoder().encodeToString(dataBytes);

			JsonObject json = new JsonObject();
			json.add("audio_data", new JsonPrimitive(base64Data));
			String jsonString = json.toString();

			wssClient.send(jsonString);
		} else {
			logger.info("***emotizeplugin*** EMPTY avFrame");
		}
	}
}
