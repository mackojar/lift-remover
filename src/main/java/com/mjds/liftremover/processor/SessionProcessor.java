package com.mjds.liftremover.processor;

import com.garmin.fit.Mesg;
import com.garmin.fit.SessionMesg;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class SessionProcessor {

    public void process(Mesg summaryRecord, List<Mesg> mesgsOut, double totalDistance) {
        log.info("Update session distance to {}", totalDistance);
        summaryRecord.setFieldValue(SessionMesg.TotalDistanceFieldNum, totalDistance);
        mesgsOut.add(summaryRecord);
    }
}
