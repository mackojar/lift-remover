package com.mjds.liftremover.trend;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Trend {

    public enum TREND {
        UNKNOWN, DOWN, UP
    }

    private TREND trend = TREND.UNKNOWN;

    private final TrendQueue queue = new TrendQueue();

    private TrendEvent mapToTrendEvent(TimeAltitude timeAltitude, TREND trend) {
        return TrendEvent.builder()
                .time(timeAltitude.time())
                .altitude(timeAltitude.altitude())
                .trend(trend)
                .build();
    }

    /**
     * Checks if trend has just changed
     * @param time
     * @param altitude
     * @return min/max if local min/max was found
     */
    public TrendEvent getMinMax(Long time, Double altitude) {
        queue.add(new TimeAltitude(time, altitude));
        if (queue.isUp() && trend == TREND.DOWN) {
            TimeAltitude minTimeAltitude = queue.getMin();
            trend = TREND.UP;
            log.info("Trend: moving up: {}, {}", minTimeAltitude.time(), minTimeAltitude.altitude());
            return mapToTrendEvent(minTimeAltitude, trend);
        } else if (queue.isDown() && trend == TREND.UP) {
            TimeAltitude maxTimeAltitude = queue.getMax();
            trend = TREND.DOWN;
            log.info("Trend: moving down: {}, {}", maxTimeAltitude.time(), maxTimeAltitude.altitude());
            return mapToTrendEvent(maxTimeAltitude, trend);
        } else if (trend == TREND.UNKNOWN && queue.isFull()) {
            if (queue.isUp()) {
                trend = TREND.UP;
                log.info("Trend: initial moving up");
                return mapToTrendEvent(queue.getFirst(), trend);
            } else if (queue.isDown()) {
                trend = TREND.DOWN;
                log.info("Trend: initial moving down");
                return mapToTrendEvent(queue.getFirst(), trend);
            }
        }
        return null;
    }
}
