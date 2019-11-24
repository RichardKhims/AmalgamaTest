import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.SchedulerException;
import ru.amalgama.test.Duration;
import ru.amalgama.test.Period;
import ru.amalgama.test.TaskManager;

import java.time.LocalDateTime;
import java.time.Month;

public class TaskManagerUnitTest {
    private TaskManager taskManager;

    @Before
    public void setUp() throws Exception {
        taskManager = new TaskManager();
    }

    @Test(expected = IllegalArgumentException.class)
    public void initDurationWithIncorrectParams() {
        Duration duration = new Duration(LocalDateTime.now(), -1, Period.ONCE);
    }

    @Test(expected = IllegalStateException.class)
    public void addTaskWithRanTask() throws SchedulerException {
        taskManager.addTask(new Duration(LocalDateTime.now(), 10, Period.EVERY_MINUTE), TestJob.class);
        taskManager.start(Exception::printStackTrace);
        taskManager.addTask(new Duration(LocalDateTime.now(), 10, Period.EVERY_MINUTE), TestJob.class);
    }

    @Test
    public void initDurationSuccess() {
        LocalDateTime dateTime = LocalDateTime.of(2019, Month.NOVEMBER, 24, 18, 52, 25);

        Duration duration = Duration.of(dateTime, new Duration(LocalDateTime.now(), 10));

        Assert.assertNotNull(duration);
        Assert.assertEquals(15, duration.getStart().getSecond());
    }

    @Test
    public void checkGetJobsBetween() throws SchedulerException {
        LocalDateTime periodStart = LocalDateTime.now();
        LocalDateTime periodEnd = periodStart.plusHours(1);

        LocalDateTime jobStart = periodStart.minusSeconds(1);

        taskManager.addTask(new Duration(jobStart, 30, Period.EVERY_MINUTE), TestJob.class);
        taskManager.addTask(new Duration(jobStart, 30, Period.EVERY_HOUR), TestJob.class);
        taskManager.addTask(new Duration(jobStart, 30, Period.EVERY_MONTH), TestJob.class);

        Assert.assertEquals(62, taskManager.getJobsBetween(periodStart, periodEnd).size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkGetJobsBetweenIncorrectParameters() throws SchedulerException {
        LocalDateTime periodStart = LocalDateTime.now();
        LocalDateTime periodEnd = periodStart.plusHours(1);

        LocalDateTime jobStart = periodStart.minusSeconds(1);

        taskManager.addTask(new Duration(jobStart, 30, Period.EVERY_MINUTE), TestJob.class);
        taskManager.addTask(new Duration(jobStart, 30, Period.EVERY_HOUR), TestJob.class);
        taskManager.addTask(new Duration(jobStart, 30, Period.EVERY_MONTH), TestJob.class);

        taskManager.getJobsBetween(periodEnd, periodStart);
    }

    @Ignore
    public void checkRunSuccessful() throws InterruptedException, SchedulerException {
        taskManager.addTask(new Duration(LocalDateTime.now(), 10, Period.EVERY_MINUTE), TestJob.class);
        taskManager.start(Exception::printStackTrace);
        Thread.sleep(3 * 60 * 1000);
    }
}
