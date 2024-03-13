package com.roulette.websocket.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roulette.websocket.service.RewardStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.CopyOnWriteArrayList;


@Service
@ServerEndpoint("/websocket")
public class WebSocketHandler {

    private static final CopyOnWriteArrayList<Session> sessions = new CopyOnWriteArrayList<>();



    private static RewardStore rewardStore;

    @Autowired
    public void setRewardStore(RewardStore rewardStoreInstance) {
        if (rewardStore == null) {
            rewardStore = rewardStoreInstance;
        }
    }
    public static RewardStore getRewardStoreInstance() {
        return rewardStore;
    }

    ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        if (sessions.isEmpty()) {
            sessions.add(session);
        }
        else {
            for (Session ses : sessions) {
                sessions.remove(ses);
                ses.close();

            }
        }
        System.out.println("WebSocket connection opened: " + session.getId());


    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
            System.out.println("Message from client: " + message);
            JsonNode messageKeyValue = objectMapper.readTree(message);
            int key = messageKeyValue.get("index").asInt()+1;
            int value = messageKeyValue.get("value").asInt();
            int frequency = messageKeyValue.get("frequency").asInt();
            int totalSpincount = messageKeyValue.get("totalSpinCount").asInt();
            rewardStore.updateReward(key, value);
            rewardStore.updateWheelFrequency(key, frequency);
            rewardStore.updateRewardExpectation(key, (float) 0.0);
            rewardStore.getRewardSoFarWith(value);
            System.out.println("index " +key + " value " + value);
            System.out.println("wheel total " + key + " " + rewardStore.getReward(key));
            System.out.println("wheel expectation"+ key + " " + rewardStore.getRewardExpectation(key));
            System.out.println("wheel frequency "+ key + " " + rewardStore.getWheelFrequency(key) + "\n");
            System.out.println("wheel reward so far "+ key + " " + rewardStore.getRewardSoFar() + "\n");
            System.out.println("Total spins so far" + " " + totalSpincount + "\n");
            sendMessageToAll("Message received: " + message);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("WebSocket connection closed: " + session.getId());
    }

    public void sendMessageToAll(String message) throws IOException {
        for (Session session : sessions) {
            session.getBasicRemote().sendText(message);
        }

    }

    public void sendImageToAll(String imagePath) {
        try {
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Construct JSON message with the image data
            String jsonMessage = "Image" +"{\"image\":\"" + base64Image + "\"}";

            sendMessageToAll(jsonMessage);
        } catch (IOException e) {
            System.err.println("Error reading image file: " + e.getMessage());
        }
    }


    public void flushRewardStore() {
        rewardStore.clear(); // Assuming clear() method is available in your RewardStore class to clear all data

    }
}
