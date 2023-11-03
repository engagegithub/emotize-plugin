package io.antmedia.app;

import java.net.URI;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssemblyClient extends WebSocketClient {
  protected static Logger logger = LoggerFactory.getLogger(AssemblyClient.class);
  private static final int CLOSE_CODE_RECONNECT = 4031;

  private TranscriptionWebhookClient webhookClient;

  public AssemblyClient(URI serverUri, Draft draft) {
    super(serverUri, draft);
  }

  public AssemblyClient(URI serverURI) {
    super(serverURI);
  }

  public AssemblyClient(URI serverUri, Map<String, String> httpHeaders, TranscriptionWebhookClient webhookClient) {
    super(serverUri, httpHeaders);

    this.webhookClient = webhookClient;
  }

  @Override
  public void onOpen(ServerHandshake handshakedata) {
    logger.info("***emotizeplugin*** Connected to AssemblyAI WebSocket");
  }

  @Override
  public void onMessage(String message) {
    if (message.contains("SessionBegins")) {
      logger.info("***emotizeplugin*** Real time transcription is starting");
    } else if (message.contains("FinalTranscript")) {
      logger.info("***emotizeplugin*** Get transcript from AssemblyAI");
      webhookClient.sendRequest(message);
    } else if (!message.contains("PartialTranscript")) {
      logger.info("***emotizeplugin*** received: " + message);
    }
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    // The close codes are documented in class org.java_websocket.framing.CloseFrame
    logger.info("***emotizeplugin*** Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);

    if (code == CLOSE_CODE_RECONNECT) {
      logger.info("***emotizeplugin*** Reconnecting to WebSocket...");

      this.reconnect(); // Reconnect if the close code is 4031
    }
  }

  @Override
  public void onError(Exception ex) {
    logger.info("***emotizeplugin*** error: " + ex);
  }
}
