package com.mjds.liftremover.processor;

import com.garmin.fit.LapMesg;
import com.garmin.fit.Mesg;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class LapProcessor {

    public void process(Mesg summaryRecord, List<Mesg> mesgsOut, double totalDistance) {
        log.info("Update lap distance to {}", totalDistance);
        summaryRecord.setFieldValue(LapMesg.TotalDistanceFieldNum, totalDistance);
        mesgsOut.add(summaryRecord);
    }
}
