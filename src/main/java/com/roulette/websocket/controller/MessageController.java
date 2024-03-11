package com.roulette.websocket.controller;

import com.roulette.websocket.handler.WebSocketHandler;
import com.roulette.websocket.service.EpsilonGreedy;
import com.roulette.websocket.service.GreedyAgentMab;
import com.roulette.websocket.service.RewardStore;
import com.roulette.websocket.service.UpperConfidenceBound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private final WebSocketHandler webSocketHandler;

    @Autowired
    private GreedyAgentMab greedyAgent;

    @Autowired
    private EpsilonGreedy epsilonGreedy;

    @Autowired
    private RewardStore rewardStore;

    @Autowired
    private UpperConfidenceBound upperConfidenceBound;



    @Autowired
    public MessageController(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
//        this.greedyAgent = greedyAgent;
    }

    @GetMapping("/spin/{id}")
    public String sendMessageToWebSocket(@PathVariable String id) throws IOException {
        webSocketHandler.sendMessageToAll(id);
        return "Message sent to WebSocket: " + id;
    }

    @GetMapping("/spin/greedy/{numberOfTrials}")
    public int greedySpin(@PathVariable  int numberOfTrials) throws IOException {
        webSocketHandler.sendMessageToAll("start");
        for (int i = 0; i <= 10; i++) {
            webSocketHandler.getRewardStoreInstance().updateReward(i, 0);
            webSocketHandler.getRewardStoreInstance().updateWheelFrequency(i, 0);
            webSocketHandler.getRewardStoreInstance().updateRewardExpectation(i,(float)0.0);
        }
        int result = this.greedyAgent.chooseGreedy(numberOfTrials);
        System.out.println("Wheel from spin greedy " + result);
        webSocketHandler.getRewardStoreInstance().clear();
        webSocketHandler.sendMessageToAll("end");
        return result;
    }


    @GetMapping("/spin/epsilon/{numberOfTrials}/{epsilon}")
    public int epsilonGreedy(@PathVariable  int numberOfTrials, @PathVariable float epsilon ) throws IOException {
        webSocketHandler.sendMessageToAll("start");
        for (int i = 0; i <= 10; i++) {
            webSocketHandler.getRewardStoreInstance().updateReward(i, 0);
            webSocketHandler.getRewardStoreInstance().updateWheelFrequency(i, 0);
            webSocketHandler.getRewardStoreInstance().updateRewardExpectation(i,(float)0.0);
        }
        int result = this.epsilonGreedy.epsilonGreedy(numberOfTrials, epsilon);
        System.out.println("Wheel from spin epsilon " + result);
        webSocketHandler.getRewardStoreInstance().clear();
        webSocketHandler.sendMessageToAll("end");
        return result;
    }

    @GetMapping("/spin/ucb/{numberOfTrials}/{explorationFactor}")
    public int ubcAlgorithm(@PathVariable  int numberOfTrials, @PathVariable float explorationFactor) throws IOException {
        webSocketHandler.sendMessageToAll("start");
        for (int i = 0; i <= 10; i++) {
            webSocketHandler.getRewardStoreInstance().updateReward(i, 0);
            webSocketHandler.getRewardStoreInstance().updateWheelFrequency(i, 0);
            webSocketHandler.getRewardStoreInstance().updateRewardExpectation(i,(float)0.0);
        }
        int result = this.upperConfidenceBound.ucbAlgorithm(numberOfTrials, explorationFactor);
        System.out.println("Wheel from spin ucb " + result);
        webSocketHandler.sendMessageToAll("end");
        webSocketHandler.getRewardStoreInstance().clear();

        return result;
    }

    @GetMapping("/spin/flush")
    public void flush() throws IOException {
        webSocketHandler.flushRewardStore();
    }




}
