package io.antmedia.plugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import io.antmedia.AntMediaApplicationAdapter;
import io.antmedia.app.EmotizeAudioFrameListener;
import io.antmedia.app.EmotizeAudioPacketListener;
import io.antmedia.app.TranscriptionWebhookClient;
import io.antmedia.plugin.api.IStreamListener;
import io.vertx.core.Vertx;

@Component(value="plugin.emotizeplugin")
public class EmotizePlugin implements ApplicationContextAware, IStreamListener{

	public static final String BEAN_NAME = "web.handler";
	protected static Logger logger = LoggerFactory.getLogger(EmotizePlugin.class);
	private static String apiToken = System.getenv("ASSEMBLY_API_TOKEN");

	private Vertx vertx;
	private EmotizeAudioFrameListener frameListener;
	private TranscriptionWebhookClient webhookClient;
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		vertx = (Vertx) applicationContext.getBean("vertxCore");

		AntMediaApplicationAdapter app = getApplication();
		app.addStreamListener(this);
	}

	public boolean register(String streamId) {
		logger.info("***emotizeplugin*** start initializing components");

		AntMediaApplicationAdapter app = getApplication();

		if (apiToken == null) {
			logger.info("***emotizeplugin*** ASSEMBLY_API_TOKEN environment variable is not set.");

			return false;
		}

		webhookClient = new TranscriptionWebhookClient(streamId);
		frameListener = new EmotizeAudioFrameListener(webhookClient);

		app.addFrameListener(streamId, frameListener);

		return true;
	}

	public AntMediaApplicationAdapter getApplication() {
		return (AntMediaApplicationAdapter) applicationContext.getBean(AntMediaApplicationAdapter.BEAN_NAME);
	}

	public String getStats() {
		return frameListener.getStats() + "\t";
	}

	@Override
	public void streamStarted(String streamId) {
		logger.info("***************emotizeplugin Stream Started: {} ***************", streamId);
	}

	@Override
	public void streamFinished(String streamId) {
		logger.info("***************emotizeplugin Stream Finished: {} ***************", streamId);
	}

	@Override
	public void joinedTheRoom(String roomId, String streamId) {
		logger.info("***************emotizeplugin Stream Id:{} joined the room:{} ***************", streamId, roomId);
	}

	@Override
	public void leftTheRoom(String roomId, String streamId) {
		logger.info("***************emotizeplugin Stream Id:{} left the room:{} ***************", streamId, roomId);
	}
}
