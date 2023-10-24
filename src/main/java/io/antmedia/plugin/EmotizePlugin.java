package io.antmedia.plugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import io.antmedia.AntMediaApplicationAdapter;
import io.antmedia.app.AssemblyClient;
import io.antmedia.app.AudioFrameListener;
import io.antmedia.app.WebhookClient;
import io.antmedia.muxer.MuxAdaptor;
import io.antmedia.plugin.api.IFrameListener;
import io.antmedia.plugin.api.IStreamListener;
import io.vertx.core.Vertx;

@Component(value="plugin.emotizeplugin")
public class EmotizePlugin implements ApplicationContextAware, IStreamListener{

	public static final String BEAN_NAME = "web.handler";
	protected static Logger logger = LoggerFactory.getLogger(EmotizePlugin.class);
	private static String apiToken = System.getenv("ASSEMBLY_API_TOKEN");

	private Vertx vertx;
	private AudioFrameListener frameListener;
	private AssemblyClient wssClient;
	private WebhookClient webhookClient;
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		vertx = (Vertx) applicationContext.getBean("vertxCore");

		AntMediaApplicationAdapter app = getApplication();
		app.addStreamListener(this);
	}

	public MuxAdaptor getMuxAdaptor(String streamId)
	{
		AntMediaApplicationAdapter application = getApplication();
		MuxAdaptor selectedMuxAdaptor = null;

		if(application != null)
		{
			List<MuxAdaptor> muxAdaptors = application.getMuxAdaptors();
			for (MuxAdaptor muxAdaptor : muxAdaptors)
			{
				if (streamId.equals(muxAdaptor.getStreamId()))
				{
					selectedMuxAdaptor = muxAdaptor;
					break;
				}
			}
		}

		return selectedMuxAdaptor;
	}

	public void start(String streamId, String host) {
		AntMediaApplicationAdapter app = getApplication();

		if (apiToken == null) {
			logger.info("ASSEMBLY_API_TOKEN environment variable is not set.");
		}
		try {
			URI wssUri = new URI("wss://api.assemblyai.com/v2/realtime/ws?sample_rate=16000");
			Map<String, String> httpHeaders = new HashMap<String, String>();
			httpHeaders.put("Authorization", apiToken);

			this.webhookClient = new WebhookClient(host, streamId);
			this.wssClient = new AssemblyClient(wssUri, httpHeaders, webhookClient);
			wssClient.connect();

			this.frameListener = new AudioFrameListener(wssClient);

			app.addFrameListener(streamId, frameListener);
		} catch (URISyntaxException e) {
			logger.error("URI syntax error: " + e.getMessage());
			return;
		}
	}

	public AntMediaApplicationAdapter getApplication() {
		return (AntMediaApplicationAdapter) applicationContext.getBean(AntMediaApplicationAdapter.BEAN_NAME);
	}

	public IFrameListener createCustomBroadcast(String streamId) {
		AntMediaApplicationAdapter app = getApplication();
		return app.createCustomBroadcast(streamId);
	}

	public String getStats() {
		return frameListener.getStats() + "\t";
	}

	@Override
	public void streamStarted(String streamId) {
		logger.info("*************** Stream Started: {} ***************", streamId);
	}

	@Override
	public void streamFinished(String streamId) {
		logger.info("*************** Stream Finished: {} ***************", streamId);
	}

	@Override
	public void joinedTheRoom(String roomId, String streamId) {
		logger.info("*************** Stream Id:{} joined the room:{} ***************", streamId, roomId);
	}

	@Override
	public void leftTheRoom(String roomId, String streamId) {
		logger.info("*************** Stream Id:{} left the room:{} ***************", streamId, roomId);
	}
}
