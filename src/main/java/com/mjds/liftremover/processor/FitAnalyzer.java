package com.mjds.liftremover.processor;

import com.garmin.fit.Field;
import com.garmin.fit.Mesg;
import com.garmin.fit.MesgBroadcastPlugin;
import com.garmin.fit.RecordMesg;
import com.mjds.liftremover.trend.Trend;
import com.mjds.liftremover.trend.TrendEvent;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Log4j2
public class FitAnalyzer implements MesgBroadcastPlugin {

    @Getter
    private final List<Mesg> mesgs = new ArrayList<>();
    @Getter
    private final SortedMap<Long, TrendEvent> timeTrends = new TreeMap<>();
    private final Trend trend = new Trend();

    @Override
    public void onBroadcast(List<Mesg> list) {
        log.warn("---> When it is called? List of events: {}", list);
    }

    @Override
    public void onIncomingMesg(Mesg mesg) {
        printMessage(mesg);
        addMesg(mesg);
    }

    private void printMessage(Mesg mesg) {
        String msg = mesg.getFields().stream()
                .map(this::printField)
                .collect(Collectors.joining(", "));
        log.info("Record: {}: {}", mesg.getNum(), msg);
    }

    private String printField(Field field) {
        return String.format("%s: %s", field.getName(), field.getValue());
    }

    private void addMesg(Mesg mesg) {
        mesgs.add(mesg);
        Long time = mesg.getFieldLongValue(RecordMesg.TimestampFieldNum);
        Double altitude = mesg.getFieldDoubleValue(RecordMesg.EnhancedAltitudeFieldNum);
        if (time != null && altitude != null) {
            TrendEvent trendEvent = trend.getMinMax(time, altitude);
            if (trendEvent != null) {
                timeTrends.put(trendEvent.getTime(), trendEvent);
            }
        }
    }

}
