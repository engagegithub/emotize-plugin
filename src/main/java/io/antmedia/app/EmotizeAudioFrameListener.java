package io.antmedia.app;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_PCM_S16LE;
import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_S16;
import static org.bytedeco.ffmpeg.global.avutil.av_get_bytes_per_sample;

import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avutil.AVDictionary;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;

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
	private int loggerCount = 0;
	private AssemblyClient wssClient;

	public EmotizeAudioFrameListener(AssemblyClient wssClient) {
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
				// Encode the byte array to base64
				String base64Data = Base64.getEncoder().encodeToString(byteData);

				JsonObject json = new JsonObject();
				json.add("audio_data", new JsonPrimitive(base64Data));
				String jsonString = json.toString();

				wssClient.send(jsonString);

				if (loggerCount < 50) {
					logger.info("***emotizeplugin*** audio data sent" + byteData);

					loggerCount++;
				}
			} else {
				logger.info("***emotizeplugin*** Empty audio data");
			}
		} catch (Exception e) {
			logger.error("***emotizeplugin*** Failed to sent AVFrame to Assembly. Ex: " + e);

			e.printStackTrace();
		}
	}

	// private void sendAVFrameToWssClient(AVFrame avFrame) {
	// 	if (avFrame == null || avFrame.data(0) == null || avFrame.linesize(0) == 0) {
	// 		logger.info("***emotizeplugin*** EMPTY avFrame");

	// 		return;
	// 	}

	// 	BytePointer data = avFrame.data(0);

	// 	int dataSize = avFrame.linesize(0);

	// 	// Convert the data to a byte array
	// 	byte[] dataBytes = new byte[dataSize];
	// 	data.get(dataBytes);

	// 	// Encode the byte array to base64
	// 	String base64Data = Base64.getEncoder().encodeToString(dataBytes);

	// 	JsonObject json = new JsonObject();
	// 	json.add("audio_data", new JsonPrimitive(base64Data));
	// 	String jsonString = json.toString();

	// 	wssClient.send(jsonString);

	// 	logger.info("***emotizeplugin*** Audio data sent to Assembly: " + jsonString);
	// }

	// public void sendAVFrameToWssClient(AVFrame avFrame) {
	// 	if (avFrame == null || avFrame.data(0) == null || avFrame.linesize(0) == 0) {
	// 		logger.info("***emotizeplugin*** EMPTY avFrame");

	// 		return;
	// 	}

	// 	try {
	// 		// Initialize FFmpeg libraries
	// 		avutil.av_log_set_level(avutil.AV_LOG_ERROR);

	// 		// Create a new AVCodecContext for encoding
	// 		AVCodec codec = avcodec.avcodec_find_encoder(AV_CODEC_ID_PCM_S16LE);
	// 		AVCodecContext codecContext = avcodec.avcodec_alloc_context3(codec);
	// 		codecContext.sample_rate(16000);
	// 		codecContext.sample_fmt(AV_SAMPLE_FMT_S16);
	// 		codecContext.channels(1);

	// 		// Open the codec context
	// 		int ret = avcodec.avcodec_open2(codecContext, codec, (AVDictionary) null);
	// 		if (ret < 0) {
	// 			logger.info("***emotizeplugin*** avcodec_open2() error " + ret + ": Could not open audio codec.");
	// 		}

	// 		// Create an AVPacket for the encoded output
	// 		AVPacket pkt = avcodec.av_packet_alloc();

	// 		// Initialize the encoder and send the frame
	// 		avcodec.avcodec_send_frame(codecContext, avFrame);

	// 		// Receive the encoded packet
	// 		avcodec.avcodec_receive_packet(codecContext, pkt);

	// 		// Encode the packet to Base64
	// 		String encodedData = encodeToBase64(pkt.data(), pkt.size());

	// 		JsonObject json = new JsonObject();
	// 		json.add("audio_data", new JsonPrimitive(encodedData));
	// 		String jsonString = json.toString();

	// 		wssClient.send(jsonString);

	// 		logger.info("***emotizeplugin*** Audio data sent to Assembly: " + jsonString);

	// 		// Clean up resources
	// 		avcodec.av_packet_unref(pkt);
	// 		avcodec.av_packet_free(pkt);
	// 		avcodec.avcodec_close(codecContext);
	// 		avcodec.avcodec_free_context(codecContext);
	// 	} catch (Exception e) {
	// 		logger.info("***emotizeplugin*** ERROR sending audio packet: " + e);
	// 	}
	// }

	// private static String encodeToBase64(BytePointer data, int size) {
	// 	byte[] encodedData = new byte[size];
	// 	data.position(0).get(encodedData);

	// 	return Base64.getEncoder().encodeToString(encodedData);
	// }
}
