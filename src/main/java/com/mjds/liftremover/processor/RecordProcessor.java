package com.mjds.liftremover.processor;

import com.garmin.fit.*;
import com.mjds.liftremover.trend.Trend;
import com.mjds.liftremover.trend.TrendEvent;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Iterator;
import java.util.List;

@Log4j2
public class RecordProcessor {

    @Getter
    private double accumulatedDistance = 0.0;
    private double previousDistance = 0.0;
    private final Iterator<TrendEvent> timeTrendEventsIterator;
    private TrendEvent trendEvent;
    private Trend.TREND trend;

    public RecordProcessor(Iterator<TrendEvent> timeTrendEventsIterator) {
        this.timeTrendEventsIterator = timeTrendEventsIterator;
        this.trendEvent = timeTrendEventsIterator.next();
        this.trend = trendEvent.getTrend();
    }

    private void addStartStopEvent(List<Mesg> mesgsOut, Trend.TREND currentTrend, Trend.TREND newTrend, Mesg currentMesg) {
        EventMesg eventMesg = new EventMesg();
        eventMesg.addField(currentMesg.getField(RecordMesg.TimestampFieldNum));
        eventMesg.setEvent(Event.TIMER);
        eventMesg.setData(0L);
        if (currentTrend == Trend.TREND.DOWN && newTrend == Trend.TREND.UP) {
            eventMesg.setEventType(EventType.STOP);
            mesgsOut.add(eventMesg);
        } else if (currentTrend == Trend.TREND.UP && newTrend == Trend.TREND.DOWN) {
            eventMesg.setEventType(EventType.START);
            mesgsOut.add(eventMesg);
        }
    }

    public void process(Mesg recordMesg, List<Mesg> mesgsOut) {
        Double distance = recordMesg.getFieldDoubleValue(RecordMesg.DistanceFieldNum);
        double distanceStep = 0.0;
        if (distance != null) {
            distanceStep = distance - previousDistance;
            previousDistance = distance;
        }
        Long time = recordMesg.getFieldLongValue(RecordMesg.TimestampFieldNum);
        if (time == trendEvent.getTime()) {
            addStartStopEvent(mesgsOut, trend, trendEvent.getTrend(), recordMesg);
            trend = trendEvent.getTrend();
            trendEvent = timeTrendEventsIterator.hasNext() ? timeTrendEventsIterator.next() : trendEvent;
        }
        if (trend == Trend.TREND.DOWN) {
            if (distance != null) {
                accumulatedDistance += distanceStep;
                recordMesg.setFieldValue(RecordMesg.DistanceFieldNum, accumulatedDistance);
                log.info("Distance: {} -> {}", distance, accumulatedDistance);
            }
            mesgsOut.add(recordMesg);
        }
    }

}
