package com.mjds.liftremover;

import com.garmin.fit.*;
import com.mjds.liftremover.exceptions.FitFileException;
import com.mjds.liftremover.processor.*;
import com.mjds.liftremover.trend.TrendEvent;
import lombok.extern.log4j.Log4j2;

import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class LiftRemover {

    private void saveUpdatedFile(List<Mesg> mesgsOut) {
        final String FILENAME = "test.fit";
        try {
            FileEncoder encode = new FileEncoder(new java.io.File(FILENAME), Fit.ProtocolVersion.V2_0);
            encode.write(mesgsOut);
            encode.close();
        } catch (FitRuntimeException e) {
            log.error("Error writing file {}", FILENAME);
            e.printStackTrace();
        }
        log.info("Encoded FIT file {}.", FILENAME);
    }

    private List<Mesg> removeLifts(FitAnalyzer fitAnalyzer) {
        List<Mesg> mesgsOut = new ArrayList<>();
        final Iterator<TrendEvent> timeTrendEventsIterator = fitAnalyzer.getTimeTrends().values().iterator();

        RecordProcessor recordProcessor = new RecordProcessor(timeTrendEventsIterator);
        SessionProcessor sessionProcessor = new SessionProcessor();
        LapProcessor lapProcessor = new LapProcessor();
        GeneralProcessor generalProcessor = new GeneralProcessor();

        for (Mesg mesg: fitAnalyzer.getMesgs()) {
            int mesgNum = mesg.getNum();
            switch (mesgNum) {
                case MesgNum.RECORD -> recordProcessor.process(mesg, mesgsOut);
                case MesgNum.SESSION -> sessionProcessor.process(mesg, mesgsOut, recordProcessor.getAccumulatedDistance());
                case MesgNum.LAP -> lapProcessor.process(mesg, mesgsOut, recordProcessor.getAccumulatedDistance());
                default -> generalProcessor.process(mesg, mesgsOut);
            }
        }

        return mesgsOut;
    }

    private void printTrendEvents(FitAnalyzer fitAnalyzer) {
        final SortedMap<Long, TrendEvent> timeTrends = fitAnalyzer.getTimeTrends();
        AtomicLong previousTime = new AtomicLong(timeTrends.get(timeTrends.firstKey()).getTime());
        AtomicReference<Double> previousAltitude = new AtomicReference<>(timeTrends.get(timeTrends.firstKey()).getAltitude());
        timeTrends.values().forEach(timeTrend -> {
            log.info("Trend: {}s, {}: {}",
                    timeTrend.getTime() - previousTime.get(),
                    timeTrend.getAltitude() - previousAltitude.get(),
                    timeTrend);
            previousTime.set(timeTrend.getTime());
            previousAltitude.set(timeTrend.getAltitude());
        });
    }

    private FitAnalyzer decodeFileInputStream(FileInputStream in, Decode decode) throws FitFileException {
        BufferedMesgBroadcaster mesgBroadcaster = new BufferedMesgBroadcaster(decode);
        FitAnalyzer fitAnalyzer = new FitAnalyzer();
        mesgBroadcaster.registerMesgBroadcastPlugin(fitAnalyzer);

        try {
            decode.read(in, mesgBroadcaster, mesgBroadcaster);
        } catch (FitRuntimeException e) {
            // If a file with 0 data size in its header has been encountered,
            // attempt to keep processing the file
            if (decode.getInvalidFileDataSize()) {
                decode.nextFile();
                decode.read(in, mesgBroadcaster, mesgBroadcaster);
            } else {
                log.error("Exception decoding file:", e);
                closeFileInputStream(in);
                return null;
            }
        }
        return fitAnalyzer;
    }

    private void closeFileInputStream(FileInputStream in) throws FitFileException {
        try {
            in.close();
        } catch (java.io.IOException e) {
            throw new FitFileException(e);
        }
    }

    private FileInputStream getFileInputStream(String fileName, Decode decode) throws FitFileException {
        FileInputStream in;
        try {
            in = new FileInputStream(fileName);
        } catch (java.io.IOException e) {
            throw new FitFileException(String.format("Error opening file %s [1]", fileName));
        }

        try {
            if (!decode.checkFileIntegrity(in)) {
                throw new FitFileException("FIT file integrity failed.");
            }
        } catch (RuntimeException e) {
            log.error("Exception Checking File Integrity:", e);
            log.error("Trying to continue...");
        } finally {
            closeFileInputStream(in);
        }

        try {
            in = new FileInputStream(fileName);
        } catch (java.io.IOException e) {
            throw new FitFileException(String.format("Error opening file %s [2]", fileName));
        }
        return in;
    }

    private void processFitFile(String fileName) throws FitFileException {
        Decode decode = new Decode();
        FileInputStream in = getFileInputStream(fileName, decode);
        FitAnalyzer fitAnalyzer = decodeFileInputStream(in, decode);
        if (fitAnalyzer == null) {
            return;
        }
        closeFileInputStream(in);
        log.info("Decoded FIT file {}.", fileName);
        printTrendEvents(fitAnalyzer);
        List<Mesg> mesgsOut = removeLifts(fitAnalyzer);
        saveUpdatedFile(mesgsOut);
    }

    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                log.warn("Usage: java -jar LiftRemover.jar <filename>");
                return;
            }
            LiftRemover liftRemover = new LiftRemover();
            liftRemover.processFitFile(args[0]);
        } catch (FitFileException e) {
            log.error("Processing error:", e);
        }
    }

}
