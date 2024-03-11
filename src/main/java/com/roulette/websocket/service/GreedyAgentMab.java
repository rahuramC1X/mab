package com.roulette.websocket.service;

import com.roulette.websocket.handler.WebSocketHandler;
import com.roulette.websocket.service.RewardStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Service
public class GreedyAgentMab {

    private final WebSocketHandler webSocketHandler;


    @Autowired
    public GreedyAgentMab(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    public int chooseGreedy(int noOfTrials) throws IOException {
        int totalRewards = 0;
        for (int i = 1; i <= 10; i++) {
            webSocketHandler.sendMessageToAll(String.valueOf(i));
            try {
                TimeUnit.SECONDS.sleep(1); // Wait for 5 seconds to receive messages
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            totalRewards += webSocketHandler.getRewardStoreInstance().getReward(i);
        }
        int result = findIndexWithLargestValue(webSocketHandler.getRewardStoreInstance().getRewardExpectationMap());
        totalRewards -= webSocketHandler.getRewardStoreInstance().getReward(result);
        for (int i = 11; i <= noOfTrials; i++) {
            webSocketHandler.sendMessageToAll(String.valueOf(result));
            try {
                TimeUnit.SECONDS.sleep(1); // Wait for 5 seconds to receive messages
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        totalRewards += webSocketHandler.getRewardStoreInstance().getReward(result);
        return totalRewards;
    }

    public static int findIndexWithLargestValue(HashMap<Integer, Float> map) {
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
        return maxIndex;
    }
}
