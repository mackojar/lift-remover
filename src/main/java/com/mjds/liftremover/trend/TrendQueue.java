package com.mjds.liftremover.trend;

import java.util.ArrayDeque;

public class TrendQueue extends ArrayDeque<TimeAltitude> {

    private static final int STEPS = 30;
    private static final Double ALTITUDE_DIFF = 5.0;

    @Override
    public boolean add(TimeAltitude e) {
        if (this.size() == STEPS) {
            this.poll();
        }
        return super.add(e);
    }

    public boolean isUp() {
        return (getLast().altitude() - getFirst().altitude() > ALTITUDE_DIFF);
    }

    public boolean isFull() {
        return this.size() == STEPS;
    }

    public boolean isDown() {
        return (getFirst().altitude() - getLast().altitude() > ALTITUDE_DIFF);
    }

    private int compare(TimeAltitude ta1, TimeAltitude ta2) {
        return Double.compare(ta1.altitude(), ta2.altitude());
    }

    public TimeAltitude getMin() {
        return this.stream().min(this::compare).get();
    }

    public TimeAltitude getMax() {
        return this.stream().max(this::compare).get();
    }

}
