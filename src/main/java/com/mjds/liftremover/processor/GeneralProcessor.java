package com.mjds.liftremover.processor;

import com.garmin.fit.Mesg;
import java.util.List;

public class GeneralProcessor {

    public void process(Mesg mesg, List<Mesg> mesgsOut) {
        mesgsOut.add(mesg);
    }
}
