package ru.amalgama.test;

import com.sun.istack.internal.NotNull;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.amalgama.test.Utils.TriggerUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * Scheduling jobs class
 */
public class TaskManager {

    private List<JobInfo> jobs = new LinkedList<>();
    private SchedulerFactory sf = new StdSchedulerFactory();
    private Scheduler sched = sf.getScheduler();

    public TaskManager() throws SchedulerException {
    }

    /**
     * Adds job to scheduling list
     * @param duration Job's time parameters
     * @param jobClass Job to schedule
     * @throws SchedulerException
     */
    public void addTask(Duration duration, Class<? extends Job> jobClass) throws SchedulerException {
        if (sched.isStarted()) {
            throw new IllegalStateException("Can not add task into started TaskManager");
        }
        jobs.add(new JobInfo(duration, jobClass));
    }

    /**
     * @param start
     * @param end
     * @return Job list that will scheduled in period from start to end. Sorted by scheduling time.
     */
    public List<JobInfo> getJobsBetween(@NotNull LocalDateTime start, @NotNull LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date is after end date");
        }

        removeScheduledJobs();

        List<JobInfo> result = new ArrayList<>();
        jobs.forEach(job -> result.addAll(TriggerUtils.triggeredInPeriod(job, start, end)));

        return result.stream().sorted((o1, o2) -> {
            if (o1.equals(o2)) {
                return 0;
            }
            if (o1.getDuration().getStart().plusSeconds(o1.getDuration().getIntervalSec())
                    .isBefore(o2.getDuration().getStart().plusSeconds(o2.getDuration().getIntervalSec()))) {
                        return -1;
            } else {
                        return 1;
            }
        }).collect(Collectors.toList());
    }

    /**
     * Starts schedulling all hobs
     * @param onException Every exception handler
     * @throws SchedulerException
     */
    public void start(Consumer<Exception> onException) throws SchedulerException {
        jobs.forEach(jobInfo -> {
            try {
                runTask(jobInfo);
            } catch (Exception e) {
                onException.accept(e);
            }
        });
        sched.start();
    }

    /**
     * Schedules job
     * @param jobInfo Job parameters
     * @throws SchedulerException
     */
    private void runTask(@NotNull JobInfo jobInfo) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(jobInfo.getJobClass())
                .withIdentity(jobInfo.getJobClass().getName(), Scheduler.DEFAULT_GROUP)
                .build();

        sched.scheduleJob(jobDetail, TriggerUtils.from(jobInfo));
    }

    /**
     * Removes all scheduled jobs for current time
     */
    private void removeScheduledJobs() {
        List<JobInfo> jobsToRemove = jobs.stream()
                .filter(jobInfo -> jobInfo.getDuration().getPeriod() == Period.ONCE)
                .filter(jobInfo -> LocalDateTime.now().isAfter(jobInfo.getDuration().getStart().plusSeconds(jobInfo.getDuration().getIntervalSec()))
        ).collect(Collectors.toList());

        jobs.removeAll(jobsToRemove);
    }
}
