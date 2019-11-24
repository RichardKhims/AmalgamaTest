package ru.amalgama.test.Utils;

import com.sun.istack.internal.NotNull;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import ru.amalgama.test.Duration;
import ru.amalgama.test.JobInfo;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Quartz triggering utils methods
 */
public class TriggerUtils {
    /**
     * Creates Trigger from Job parameters
     * @param jobInfo
     * @return
     */
    public static Trigger from(JobInfo jobInfo) {
        TriggerBuilder trigger = TriggerBuilder.newTrigger();

        if (jobInfo.getDuration().getStart().isBefore(LocalDateTime.now())) {
            trigger.startNow();
        } else {
            trigger.startAt(Date.from(jobInfo.getDuration().getStart().atZone(ZoneId.systemDefault()).toInstant()));
        }

//        if (jobInfo.getDuration().getIntervalSec() > 0) {
//            trigger.endAt(Date.from(jobInfo.getDuration().getStart().plusSeconds(jobInfo.getDuration().getIntervalSec())
//                    .atZone(ZoneId.systemDefault()).toInstant()));
//        }

        int second = jobInfo.getDuration().getStart().getSecond();
        int minute = jobInfo.getDuration().getStart().getMinute();
        int hour = jobInfo.getDuration().getStart().getHour();
        String dayStr = cronDayOfWeek(jobInfo.getDuration().getStart().getDayOfWeek());
        int day = jobInfo.getDuration().getStart().getDayOfMonth();
        String monthStr = cronMonthOfYear(jobInfo.getDuration().getStart().getMonthValue());
        int year = jobInfo.getDuration().getStart().getYear();

        switch (jobInfo.getDuration().getPeriod()) {
            case EVERY_MINUTE:
                trigger.withSchedule(CronScheduleBuilder.cronSchedule(second + " 0/1 * ? * * *"));
                break;
            case EVERY_HOUR:
                trigger.withSchedule(CronScheduleBuilder.cronSchedule(second + " " + minute + " 0/1 ? * * *"));
                break;
            case EVERY_DAY:
                trigger.withSchedule(CronScheduleBuilder.cronSchedule(second + " " + minute + " " + hour + " ? * 1/1 *"));
                break;
            case EVERY_WEEK:
                trigger.withSchedule(CronScheduleBuilder.cronSchedule(second + " " + minute + " " + hour + " ? * " + dayStr + " *"));
                break;
            case EVERY_MONTH:
                trigger.withSchedule(CronScheduleBuilder.cronSchedule(second + " " + minute + " " + hour + " " + day + " 1/1 ? *"));
                break;
            case EVERY_YEAR:
                trigger.withSchedule(CronScheduleBuilder.cronSchedule(second + " " + minute + " " + hour + " " + day + " " + monthStr +" ? " + year + "/1"));
                break;
        }

        return trigger.build();
    }

    /**
     * @param job Job parameters
     * @param start
     * @param end
     * @return List of jobs that will be scheduled in period from start to end
     */
    public static List<JobInfo> triggeredInPeriod(@NotNull JobInfo job, @NotNull LocalDateTime start, @NotNull LocalDateTime end) {
        LocalDateTime actualDateTime = job.getDuration().getStart().plusSeconds(job.getDuration().getIntervalSec());

        if (actualDateTime.isAfter(end)) {
            return Collections.EMPTY_LIST;
        }

        switch (job.getDuration().getPeriod()) {
            case ONCE:
                if (actualDateTime.isAfter(start) && actualDateTime.isBefore(end)) {
                    return Collections.singletonList(job);
                }
            case EVERY_MINUTE:
                return getEveryMinutesJobs(job, start, end, 1);
            case EVERY_HOUR:
                return getEveryHoursJobs(job, start, end, 1);
            case EVERY_DAY:
                return getEveryDaysJobs(job, start, end, 1);
            case EVERY_WEEK:
                return getEveryDaysJobs(job, start, end, 7);
            case EVERY_MONTH:
                return getEveryMonthsJobs(job, start, end, 1);
            case EVERY_YEAR:
                return getEveryYearsJobs(job, start, end, 1);
        }

        return Collections.EMPTY_LIST;
    }

    private static List<JobInfo> getEveryMinutesJobs(JobInfo job, LocalDateTime start, LocalDateTime end, int minutes) {
        LocalDateTime actualDateTime = job.getDuration().getStart().plusSeconds(job.getDuration().getIntervalSec());

        List<JobInfo> result = new LinkedList<JobInfo>();

        actualDateTime = actualDateTime.withYear(start.getYear())
                .withMonth(start.getMonthValue())
                .withDayOfMonth(start.getDayOfMonth())
                .withHour(start.getHour())
                .withMinute(start.getMinute());

        if (actualDateTime.isBefore(job.getDuration().getStart())) {
            actualDateTime = actualDateTime.plusMinutes(minutes);
        }

        for (;actualDateTime.isBefore(end); actualDateTime = actualDateTime.plusMinutes(minutes)) {
            Duration duration = Duration.of(actualDateTime, job.getDuration());
            result.add(new JobInfo(duration, job.getJobClass()));
        }

        return result;
    }

    private static List<JobInfo> getEveryHoursJobs(JobInfo job, LocalDateTime start, LocalDateTime end, int hours) {
        LocalDateTime actualDateTime = job.getDuration().getStart().plusSeconds(job.getDuration().getIntervalSec());

        List<JobInfo> result = new LinkedList<JobInfo>();

        actualDateTime = actualDateTime.withYear(start.getYear())
                .withMonth(start.getMonthValue())
                .withDayOfMonth(start.getDayOfMonth())
                .withHour(start.getHour());
        if (actualDateTime.isBefore(job.getDuration().getStart())) {
            actualDateTime = actualDateTime.plusHours(hours);
        }

        for (;actualDateTime.isBefore(end); actualDateTime = actualDateTime.plusHours(hours)) {
            Duration duration = Duration.of(actualDateTime, job.getDuration());
            result.add(new JobInfo(duration, job.getJobClass()));
        }

        return result;
    }

    private static List<JobInfo> getEveryDaysJobs(JobInfo job, LocalDateTime start, LocalDateTime end, int days) {
        LocalDateTime actualDateTime = job.getDuration().getStart().plusSeconds(job.getDuration().getIntervalSec());

        List<JobInfo> result = new LinkedList<JobInfo>();

        actualDateTime = actualDateTime.withYear(start.getYear())
                .withMonth(start.getMonthValue())
                .withDayOfMonth(start.getDayOfMonth());
        if (actualDateTime.isBefore(job.getDuration().getStart())) {
            actualDateTime = actualDateTime.plusDays(days);
        }

        for (;actualDateTime.isBefore(end); actualDateTime = actualDateTime.plusDays(days)) {
            Duration duration = Duration.of(actualDateTime, job.getDuration());
            result.add(new JobInfo(duration, job.getJobClass()));
        }

        return result;
    }

    private static List<JobInfo> getEveryMonthsJobs(JobInfo job, LocalDateTime start, LocalDateTime end, int months) {
        LocalDateTime actualDateTime = job.getDuration().getStart().plusSeconds(job.getDuration().getIntervalSec());

        List<JobInfo> result = new LinkedList<JobInfo>();

        actualDateTime = actualDateTime.withYear(start.getYear())
                .withMonth(start.getMonthValue());
        if (actualDateTime.isBefore(job.getDuration().getStart())) {
            actualDateTime = actualDateTime.plusMonths(months);
        }

        for (;actualDateTime.isBefore(end); actualDateTime = actualDateTime.plusMonths(months)) {
            Duration duration = Duration.of(actualDateTime, job.getDuration());
            result.add(new JobInfo(duration, job.getJobClass()));
        }

        return result;
    }

    private static List<JobInfo> getEveryYearsJobs(JobInfo job, LocalDateTime start, LocalDateTime end, int years) {
        LocalDateTime actualDateTime = job.getDuration().getStart().plusSeconds(job.getDuration().getIntervalSec());

        List<JobInfo> result = new LinkedList<JobInfo>();

        actualDateTime = actualDateTime.withYear(start.getYear());
        if (actualDateTime.isBefore(job.getDuration().getStart())) {
            actualDateTime = actualDateTime.plusYears(years);
        }

        for (;actualDateTime.isBefore(end); actualDateTime = actualDateTime.plusYears(years)) {
            Duration duration = Duration.of(actualDateTime, job.getDuration());
            result.add(new JobInfo(duration, job.getJobClass()));
        }

        return result;
    }

    private static String cronDayOfWeek(DayOfWeek day) {
        switch (day) {
            case MONDAY:
                return "MON";
            case TUESDAY:
                return "TUE";
            case WEDNESDAY:
                return "WED";
            case THURSDAY:
                return "THU";
            case FRIDAY:
                return "FRI";
            case SATURDAY:
                return "SAT";
            case SUNDAY:
                return "SUN";
        }

        return null;
    }

    private static String cronMonthOfYear(int month) {
        switch (month) {
            case 1:
                return "JAN";
            case 2:
                return "FEB";
            case 3:
                return "MAR";
            case 4:
                return "APR";
            case 5:
                return "MAY";
            case 6:
                return "JUN";
            case 7:
                return "JUL";
            case 8:
                return "AUG";
            case 9:
                return "SEP";
            case 10:
                return "OCT";
            case 11:
                return "NOV";
            case 12:
                return "DEC";
        }

        return null;
    }
}
