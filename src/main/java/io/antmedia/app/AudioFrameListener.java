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

public class AudioFrameListener implements IFrameListener{

	protected static Logger logger = LoggerFactory.getLogger(AudioFrameListener.class);

	private int audioFrameCount = 0;
	private AssemblyClient wssClient;

	public AudioFrameListener(AssemblyClient wssClient) {
		this.wssClient = wssClient;
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
		logger.info("SampleFrameListener.writeTrailer() for streamId:{}", streamId);
	}

	@Override
	public void setVideoStreamInfo(String streamId, StreamParametersInfo videoStreamInfo) {
		logger.info("SampleFrameListener.setVideoStreamInfo() for streamId:{}", streamId);
	}

	@Override
	public void setAudioStreamInfo(String streamId, StreamParametersInfo audioStreamInfo) {
		logger.info("SampleFrameListener.setAudioStreamInfo() for streamId:{}", streamId);
	}

	@Override
	public void start() {
		logger.info("SampleFrameListener.start()");
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
		}
	}
}
