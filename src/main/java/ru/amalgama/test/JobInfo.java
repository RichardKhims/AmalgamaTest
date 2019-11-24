package ru.amalgama.test;

import org.quartz.Job;

import java.util.Objects;

/**
 * Job parameters
 */
public class JobInfo {
    private Duration duration;
    private Class<? extends Job> jobClass;

    public JobInfo(Duration duration, Class<? extends Job> jobClass) {
        this.duration = duration;
        this.jobClass = jobClass;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Class<? extends Job> getJobClass() {
        return jobClass;
    }

    public void setJobClass(Class<? extends Job> jobClass) {
        this.jobClass = jobClass;
    }

    @Override
    public String toString() {
        return "JobInfo{" +
                "duration=" + duration +
                ", jobClass=" + jobClass +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobInfo jobInfo = (JobInfo) o;
        return duration.equals(jobInfo.duration) &&
                jobClass.equals(jobInfo.jobClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(duration, jobClass);
    }
}
