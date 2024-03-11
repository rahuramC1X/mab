package com.roulette.websocket.service;

import com.roulette.websocket.handler.WebSocketHandler;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Service
public class EpsilonGreedy {

    private final WebSocketHandler webSocketHandler;


    @Autowired
    public EpsilonGreedy(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    public int epsilonGreedy(int noOfTrials, float epsilon) throws IOException {
        int totalReward = 0;
        int result;
        for (int i = 1; i <= noOfTrials; i++) {
            double randomNumber = Math.random();
            if (randomNumber < epsilon) {
                result = chooseRandomAction();
            }
            else {
                result = findIndexWithLargestValue(webSocketHandler.getRewardStoreInstance().getRewardExpectationMap());
            }

            // Send the chosen action to the WebSocket
            webSocketHandler.sendMessageToAll(String.valueOf(result));

            // Wait for 1 second to receive messages
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        for (int i = 1; i <= 10; i++) {
            Integer reward = webSocketHandler.getRewardStoreInstance().getReward(i);
            if (reward != null) {
                totalReward += reward;
            } else {
                // Handle null case here (optional)
                System.out.println("Reward for wheel " + i + " is null");
            }
        }

        return totalReward ;
    }

    public static int findIndexWithLargestValue(HashMap<Integer, Float> map) throws IOException {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("Map is null or empty");
        }

        // Initialize variables to keep track of the maximum value and its corresponding index
        float maxValue = Float.MIN_VALUE;
        int maxIndex = -1;

        // Iterate over the entries in the map
        for (Map.Entry<Integer, Float> entry : map.entrySet()) {
            // Get the value associated with the current key
            float value = entry.getValue();

            // Check if the current value is greater than the maximum value found so far
            if (value > maxValue) {
                // Update the maximum value and its corresponding index
                maxValue = value;
                maxIndex = entry.getKey();
            }
        }

        // Return the index with the largest value
        if (maxIndex == -1)
        {
            return chooseRandomAction();
        }
        return maxIndex;
    }

    private static int chooseRandomAction() throws IOException {
        // Generate a random index between 1 and 9 (inclusive)
        return new Random().nextInt(10) + 1;
    }
}
