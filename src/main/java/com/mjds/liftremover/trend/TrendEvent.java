package com.mjds.liftremover.trend;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrendEvent {
    long time;
    Trend.TREND trend;
    Double altitude;
}
