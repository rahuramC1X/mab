package com.roulette.websocket.service;

import com.roulette.websocket.handler.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;



@Service
public class UpperConfidenceBound {

    private final WebSocketHandler webSocketHandler;

    @Autowired
    public UpperConfidenceBound(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    public int ucbAlgorithm(int noOfTrials, double explorationFactor) throws IOException, IOException {
        int result;
        int totalReward = 0;

        for (int i = 1; i <= noOfTrials; i++) {
            double maxUpperBound = 0;
            int selectedWheel = -1;
            for (int j = 1; j <= 10; j++) { // Assuming 10 wheels
                double upperBound = calculateUpperBound(j, i, explorationFactor);
                if (upperBound > maxUpperBound) {
                    maxUpperBound = upperBound;
                    selectedWheel = j;
                }
            }
            result = selectedWheel;
            webSocketHandler.sendMessageToAll(String.valueOf(result));
            try {
                TimeUnit.SECONDS.sleep(1); // Wait for 1 second to receive messages
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            totalReward += webSocketHandler.getRewardStoreInstance().getRewardExpectation(result);
        }
        return totalReward; // Modify this accordingly, currently returning -1 for demonstration
    }

    private double calculateUpperBound(int wheelIndex, int trialNumber, double explorationFactor) {
        // Get rewards for the current wheel from the RewardStore or RewardExpectationMap
        double averageReward = webSocketHandler.getRewardStoreInstance().getRewardExpectation(wheelIndex);
        int wheelFrequency = webSocketHandler.getRewardStoreInstance().getWheelFrequency(wheelIndex);
        if (wheelFrequency > 0) {
            double explorationTerm = Math.sqrt(2 * Math.log(trialNumber) / wheelFrequency);
            return averageReward + explorationFactor * explorationTerm;
        } else {
            return Double.MAX_VALUE;
        }
    }
}

