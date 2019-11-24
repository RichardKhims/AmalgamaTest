package ru.amalgama.test;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Job's time parameters
 */
public class Duration {
    private LocalDateTime start;
    private int intervalSec;
    private Period period;

    public Duration(LocalDateTime start, int sec) {
        this.start = start;
        setIntervalSec(sec);
        this.period = Period.ONCE;
    }

    public Duration(LocalDateTime start, int sec, Period period) {
        this.start = start;
        setIntervalSec(sec);
        this.period = period;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public int getIntervalSec() {
        return intervalSec;
    }

    public void setIntervalSec(int intervalSec) {
        if (intervalSec < 0) {
            throw new IllegalArgumentException("IntervalSec must be >= 0");
        }
        this.intervalSec = intervalSec;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public static Duration of(LocalDateTime end, Duration duration) {
        LocalDateTime start = end.minusSeconds(duration.getIntervalSec());
        return new Duration(start, duration.getIntervalSec(), duration.getPeriod());
    }

    @Override
    public String toString() {
        return "Duration{" +
                "start=" + start +
                ", intervalSec=" + intervalSec +
                ", period=" + period +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Duration duration = (Duration) o;
        return intervalSec == duration.intervalSec &&
                start.equals(duration.start) &&
                period == duration.period;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, intervalSec, period);
    }
}
