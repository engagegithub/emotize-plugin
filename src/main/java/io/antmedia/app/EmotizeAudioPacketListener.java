package io.antmedia.app;

import io.antmedia.plugin.api.IPacketListener;
import io.antmedia.plugin.api.StreamParametersInfo;

import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.javacpp.BytePointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class EmotizeAudioPacketListener implements IPacketListener{

  private int packetCount = 0;

	protected static Logger logger = LoggerFactory.getLogger(EmotizeAudioPacketListener.class);

	@Override
	public void writeTrailer(String streamId) {
		System.out.println("***emotizeplugin*** PacketListener.writeTrailer()");

	}

	@Override
	public AVPacket onVideoPacket(String streamId, AVPacket packet) {
		packetCount++;
		return packet;
	}

	@Override
	public AVPacket onAudioPacket(String streamId, AVPacket packet) {
		packetCount++;
    sendAVPacketToWssClient(packet);
		return packet;
	}

	@Override
	public void setVideoStreamInfo(String streamId, StreamParametersInfo videoStreamInfo) {
		logger.info("***emotizeplugin*** PacketListener.setVideoStreamInfo() for streamId:{}", streamId);
	}

	@Override
	public void setAudioStreamInfo(String streamId, StreamParametersInfo audioStreamInfo) {
		logger.info("***emotizeplugin***  PacketListener.setAudioStreamInfo() for streamId:{}", streamId);
	}

	public String getStats() {
		return "packets:"+packetCount;
	}

    private void sendAVPacketToWssClient(AVPacket packet) {
      // Encode the byte array to base64
      String base64Data = encodeToBase64(packet.data(), packet.size());

      JsonObject json = new JsonObject();
      json.add("audio_data", new JsonPrimitive(base64Data));
      String jsonString = json.toString();

      // wssClient.send(jsonString);
    }

    private static String encodeToBase64(BytePointer data, int size) {
      byte[] encodedData = new byte[size];
      data.position(0).get(encodedData);

      return Base64.getEncoder().encodeToString(encodedData);
    }
}
