package TaskSchedulingSystem;

import java.util.*;
import java.util.concurrent.*;


class Task implements Comparable<Task> {
    private String name;
    private long executionTime; // in millis
    private Runnable action;

    public Task(String name, long delayMillis, Runnable action) {
        this.name = name;
        this.executionTime = System.currentTimeMillis() + delayMillis;
        this.action = action;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void run() {
        System.out.println("Executing: " + name + " at " + new Date());
        action.run();
    }

    @Override
    public int compareTo(Task other) {
        return Long.compare(this.executionTime, other.executionTime);
    }
}


class TaskScheduler {
    private static TaskScheduler instance;
    private PriorityBlockingQueue<Task> taskQueue;
    private ExecutorService workerPool;

    private TaskScheduler() {
        taskQueue = new PriorityBlockingQueue<>();
        workerPool = Executors.newFixedThreadPool(2);
        start();
    }

    public static synchronized TaskScheduler getInstance() {
        if (instance == null) {
            instance = new TaskScheduler();
        }
        return instance;
    }

    public void scheduleTask(Task task) {
        taskQueue.add(task);
        System.out.println("Scheduled: " + task);
    }

    private void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Task task = taskQueue.take();
                    long waitTime = task.getExecutionTime() - System.currentTimeMillis();
                    if (waitTime > 0) Thread.sleep(waitTime);
                    workerPool.submit(task::run);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}


public class SchedulerDemo {
    public static void main(String[] args) {
        TaskScheduler scheduler = TaskScheduler.getInstance();
        scheduler.scheduleTask(new Task("Send Email", 3000, () -> {
            System.out.println("Email sent successfully!");
        }));
        scheduler.scheduleTask(new Task("DB Backup", 5000, () -> {
            System.out.println("Database backup completed!");
        }));
        scheduler.scheduleTask(new Task("Push Notification", 2000, () -> {
            System.out.println("User notified!");
        }));
    }
}
