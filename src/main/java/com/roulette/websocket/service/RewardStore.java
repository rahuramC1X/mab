package com.roulette.websocket.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Component
@Service
@Scope("singleton")
public class RewardStore {
    private final HashMap<Integer, Integer> rewardStore = new HashMap<>();
    private final HashMap<Integer, Float> rewardExp = new HashMap<>();

    public int reward_so_far = 0;
    private final HashMap<Integer, Integer> wheelFrequency = new HashMap<>();
    public synchronized void updateReward(int index, int value) {
        if(rewardStore.containsKey(index)) {
            value += rewardStore.get(index);
        }
        rewardStore.put(index, value);
    }

    public synchronized Integer getReward(int index) {
        return rewardStore.get(index);
    }

    public synchronized Integer getRewardSoFarWith(int value) {
        this.reward_so_far += value;
        return  this.reward_so_far;
    }

    public synchronized Integer getRewardSoFar() {
        return this.reward_so_far;
    }


    public synchronized void updateRewardExpectation(int index, float v) {
        rewardExp.put(index, (float) (rewardStore.get(index)/(float) wheelFrequency.get(index)));
    }

    public synchronized void updateWheelFrequency(int index, int frequency) {
        wheelFrequency.put(index,frequency);
    }

    public synchronized int  getWheelFrequency(int index) {
        return wheelFrequency.get(index);
    }

    public synchronized Float getRewardExpectation(int index) {
        return rewardExp.get(index);
    }

    public HashMap<Integer, Integer> getRewardMap() {
        return this.rewardStore;
    }

    public HashMap<Integer, Float> getRewardExpectationMap() {
        return this.rewardExp;
    }

    public HashMap<Integer, Integer> getWheelFrequencyMap() { return this.wheelFrequency ;}

    public void clear() {
        rewardStore.clear(); // Assuming rewardMap is the HashMap in your RewardStore class
        rewardExp.clear(); // Assuming frequencyMap is another HashMap in your RewardStore class
        wheelFrequency.clear();
    }
}
