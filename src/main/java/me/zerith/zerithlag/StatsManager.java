package me.zerith.zerithlag;

public class StatsManager {

    private long totalCleared = 0;
    private long lastClearAmount = 0;
    private long lastClearTime = -1;

    public void recordClear(int amount) {
        totalCleared += amount;
        lastClearAmount = amount;
        lastClearTime = System.currentTimeMillis();
    }

    public long getTotalCleared() { return totalCleared; }
    public long getLastClearAmount() { return lastClearAmount; }

    public String getLastClearTimeFormatted() {
        if (lastClearTime == -1) return "Nunca";
        long secondsAgo = (System.currentTimeMillis() - lastClearTime) / 1000;
        if (secondsAgo < 60) return "hace " + secondsAgo + "s";
        long minutes = secondsAgo / 60;
        return "hace " + minutes + "m";
    }
}
