package com.viki.accelero;

public class BatteryEvent {
    public int level;
    public boolean isCharging;
    public boolean isFull;

    public BatteryEvent(int level, boolean isCharging, boolean full) {
        this.level = level;
        this.isCharging = isCharging;
        this.isFull= full;
    }
}
