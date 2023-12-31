package io.antmedia.app;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranscriptionWebhookClient {
  private String streamId;
  protected static Logger logger = LoggerFactory.getLogger(EmotizeAudioFrameListener.class);
  private static String host = System.getenv("EMOTIZE_HOST");

  public TranscriptionWebhookClient(String streamId) {
    this.streamId = streamId;
  }

  public void sendRequest(String message) {
    try {
        URL url = new URL(host + "/webhooks/ant_media/transcription");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        String requestBody = String.format("{\"stream_id\": \"%s\", \"message\": %s}", streamId, message);

        byte[] messageBytes = requestBody.getBytes();
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
          os.write(messageBytes, 0, messageBytes.length);
        }

        logger.info("***emotizeplugin*** Obtaining webhook request. Code: " + connection.getResponseCode());
      } catch (Exception e) {
      logger.error("***emotizeplugin*** Failed to obtain request. Ex: " + e);
    }
  }
}
