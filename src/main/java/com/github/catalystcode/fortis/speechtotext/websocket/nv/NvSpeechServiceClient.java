package com.github.catalystcode.fortis.speechtotext.websocket.nv;

import com.github.catalystcode.fortis.speechtotext.config.SpeechServiceConfig;
import com.github.catalystcode.fortis.speechtotext.lifecycle.MessageReceiver;
import com.github.catalystcode.fortis.speechtotext.telemetry.ConnectionTelemetry;
import com.github.catalystcode.fortis.speechtotext.websocket.MessageSender;
import com.github.catalystcode.fortis.speechtotext.websocket.SpeechServiceClient;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.util.concurrent.CountDownLatch;

import static com.github.catalystcode.fortis.speechtotext.utils.ProtocolUtils.newGuid;

public class NvSpeechServiceClient implements SpeechServiceClient {
    private final CountDownLatch socketCloseLatch;
    private WebSocket webSocket;

    public NvSpeechServiceClient(CountDownLatch socketCloseLatch) {
        this.socketCloseLatch = socketCloseLatch;
    }

    @Override
    public MessageSender start(SpeechServiceConfig config, MessageReceiver receiver) throws Exception {
        String connectionId = newGuid();
        ConnectionTelemetry telemetry = ConnectionTelemetry.forId(connectionId);

        WebSocketFactory factory = new WebSocketFactory();
        webSocket = factory.createSocket(config.getConnectionUrl(connectionId));
        webSocket.addListener(new NvMessageReceiver(socketCloseLatch, receiver, telemetry));
        webSocket.connect();
        telemetry.recordConnectionStarted();
        return new NvMessageSender(connectionId, webSocket);
    }

    @Override
    public void stop() throws Exception {
        webSocket.disconnect();
    }

    @Override
    public void awaitEnd() throws InterruptedException {
        socketCloseLatch.await();
    }
}
