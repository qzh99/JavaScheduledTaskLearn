# Timer类注释
```java
/*
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.util;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A facility for threads to schedule tasks for future execution in a
 * background thread.  Tasks may be scheduled for one-time execution, or for
 * repeated execution at regular intervals.
 * 一个让线程安排计划任务将来在后台执行的工具。任务可执行一次或者定期重复执行。
 *
 * <p>Corresponding to each <tt>Timer</tt> object is a single background
 * thread that is used to execute all of the timer's tasks, sequentially.
 * Timer tasks should complete quickly.  If a timer task takes excessive time
 * to complete, it "hogs" the timer's task execution thread.  This can, in
 * turn, delay the execution of subsequent tasks, which may "bunch up" and
 * execute in rapid succession when (and if) the offending task finally
 * completes.
 * 每个Timer对象都是一个后台线程，它可以在所有定时任务中顺序执行。
 * 定时任务应该很快执行完。如果一个定时器任务花费了过多的时间来完成，它就会“占用”定时器的任务执行线程。
 * 反过来，这可以延迟后续任务的执行，这些任务可能会在（如果）有问题的任务最终完成时“聚集”并快速连续执行。
 *
 * <p>After the last live reference to a <tt>Timer</tt> object goes away
 * <i>and</i> all outstanding tasks have completed execution, the timer's task
 * execution thread terminates gracefully (and becomes subject to garbage
 * collection).  However, this can take arbitrarily long to occur.  By
 * default, the task execution thread does not run as a <i>daemon thread</i>,
 * so it is capable of keeping an application from terminating.  If a caller
 * wants to terminate a timer's task execution thread rapidly, the caller
 * should invoke the timer's <tt>cancel</tt> method.
 * 在对 Timer 对象的最后一个实时引用消失并且所有未完成的任务都完成执行后，
 * 计时器的任务执行线程优雅地终止（并成为垃圾回收的对象）
 * 但是，这可能需要任意长的时间才能发生。默认情况下，任务执行线程不作为守护线程运行，因此它能够防止应用程序终止。
 * 如果调用者想要快速终止定时器的任务执行线程，调用者应该调用定时器的取消方法
 *
 * <p>If the timer's task execution thread terminates unexpectedly, for
 * example, because its <tt>stop</tt> method is invoked, any further
 * attempt to schedule a task on the timer will result in an
 * <tt>IllegalStateException</tt>, as if the timer's <tt>cancel</tt>
 * method had been invoked.
 * 如果计时器的任务执行线程意外终止，例如，因为调用了它的 stop 方法，
 * 那么任何进一步尝试在计时器上安排任务都将导致 IllegalStateException，就像调用了计时器的取消方法一样。
 *
 * <p>This class is thread-safe: multiple threads can share a single
 * <tt>Timer</tt> object without the need for external synchronization.
 * 这个类是线程安全的：多个线程可以共享一个 Timer 对象而不需要外部同步。
 *
 * <p>This class does <i>not</i> offer real-time guarantees: it schedules
 * tasks using the <tt>Object.wait(long)</tt> method.
 * 此类不提供实时保证：它使用 Object.wait(long) 方法安排任务。
 *
 * <p>Java 5.0 introduced the {@code java.util.concurrent} package and
 * one of the concurrency utilities therein is the {@link
 * java.util.concurrent.ScheduledThreadPoolExecutor
 * ScheduledThreadPoolExecutor} which is a thread pool for repeatedly
 * executing tasks at a given rate or delay.  It is effectively a more
 * versatile replacement for the {@code Timer}/{@code TimerTask}
 * combination, as it allows multiple service threads, accepts various
 * time units, and doesn't require subclassing {@code TimerTask} (just
 * implement {@code Runnable}).  Configuring {@code
 * ScheduledThreadPoolExecutor} with one thread makes it equivalent to
 * {@code Timer}.
 * Java 5.0 引入了 java.util.concurrent 包，其中的并发实用程序之一是 ScheduledThreadPoolExecutor，它是一个线程池，
 * 用于以给定的速率或延迟重复执行任务。
 * 它实际上是 Timer/TimerTask 组合的更通用替代品，因为它允许多个服务线程，接受各种时间单位，
 * 并且不需要子类化 TimerTask（只需实现 Runnable）。 使用一个线程配置 ScheduledThreadPoolExecutor 使其等效于 Timer。
 *
 * <p>Implementation note: This class scales to large numbers of concurrently
 * scheduled tasks (thousands should present no problem).  Internally,
 * it uses a binary heap to represent its task queue, so the cost to schedule
 * a task is O(log n), where n is the number of concurrently scheduled tasks.
 * 实施说明：此类可扩展到大量并发计划任务（数千个应该没有问题）。在内部，它使用二进制堆来表示其任务队列，
 * 因此调度任务的成本为 O(log n)，其中 n 是并发调度的任务数。
 
 * <p>Implementation note: All constructors start a timer thread.
 * 实现说明：所有构造函数都启动一个计时器（任务）线程。
 * @author  Josh Bloch
 * @see     TimerTask
 * @see     Object#wait(long)
 * @since   1.3
 */

public class Timer {
    /**
     * The timer task queue.  This data structure is shared with the timer
     * thread.  The timer produces tasks, via its various schedule calls,
     * and the timer thread consumes, executing timer tasks as appropriate,
     * and removing them from the queue when they're obsolete.
     * 定时器任务队列。 此数据结构与计时器线程共享。 计时器通过其各种调度调用产生任务，计时器线程消费，
     * 适当地执行计时器任务，并在它们过时时将它们从队列中删除。
     */
    private final TaskQueue queue = new TaskQueue();

    /**
     * The timer thread.
     */
    private final TimerThread thread = new TimerThread(queue);

    /**
     * This object causes the timer's task execution thread to exit
     * gracefully when there are no live references to the Timer object and no
     * tasks in the timer queue.  It is used in preference to a finalizer on
     * Timer as such a finalizer would be susceptible to a subclass's
     * finalizer forgetting to call it.
     * 当没有对 Timer 对象的实时引用并且计时器队列中没有任务时，此对象会导致计时器的任务执行线程正常退出。 
     * 它优先于 Timer 上的终结器使用，因为这样的终结器容易受到子类的终结器忘记调用它的影响。
     */
    private final Object threadReaper = new Object() {
        protected void finalize() throws Throwable {
            synchronized(queue) {
                thread.newTasksMayBeScheduled = false;
                queue.notify(); // In case queue is empty.（如果队列为空。）
            }
        }
    };

    /**
     * This ID is used to generate thread names.
     */
    private final static AtomicInteger nextSerialNumber = new AtomicInteger(0);
    private static int serialNumber() {
        return nextSerialNumber.getAndIncrement();
    }

    /**
     * Creates a new timer.  The associated thread does <i>not</i>
     * {@linkplain Thread#setDaemon run as a daemon}.
     * 创建一个新的计时器。 关联的线程不作为守护程序运行。
     */
    public Timer() {
        this("Timer-" + serialNumber());
    }

    /**
     * Creates a new timer whose associated thread may be specified to
     * {@linkplain Thread#setDaemon run as a daemon}.
     * A daemon thread is called for if the timer will be used to
     * schedule repeating "maintenance activities", which must be
     * performed as long as the application is running, but should not
     * prolong the lifetime of the application.
     *
     * @param isDaemon true if the associated thread should run as a daemon.
     * 创建一个新的计时器，其关联的线程可以指定为作为守护进程运行。 如果计时器将用于安排重复的“维护活动”，
     * 则调用守护线程，只要应用程序正在运行，就必须执行该活动，但不应延长应用程序的生命周期。
     * run as a daemon：将此线程标记为守护线程或用户线程。 当唯一运行的线程都是守护线程时，Java 虚拟机退出。
     * 此方法必须在线程启动之前调用。
     */
    public Timer(boolean isDaemon) {
        this("Timer-" + serialNumber(), isDaemon);
    }

    /**
     * Creates a new timer whose associated thread has the specified name.
     * The associated thread does <i>not</i>
     * {@linkplain Thread#setDaemon run as a daemon}.
     *
     * @param name the name of the associated thread 关联线程的名称
     * @throws NullPointerException if {@code name} is null
     * @since 1.5
     */
    public Timer(String name) {
        thread.setName(name);
        thread.start();
    }

    /**
     * Creates a new timer whose associated thread has the specified name,
     * and may be specified to
     * {@linkplain Thread#setDaemon run as a daemon}.
     *
     * @param name the name of the associated thread
     * @param isDaemon true if the associated thread should run as a daemon
     * @throws NullPointerException if {@code name} is null
     * @since 1.5
     */
    public Timer(String name, boolean isDaemon) {
        thread.setName(name);
        thread.setDaemon(isDaemon);
        thread.start();
    }

    /**
     * Schedules the specified task for execution after the specified delay.
     * 安排指定任务在指定延迟后执行。
     * @param task  task to be scheduled.
     * @param delay delay in milliseconds before task is to be executed.任务执行前的延迟毫秒数。
     * @throws IllegalArgumentException if <tt>delay</tt> is negative, or
     *         <tt>delay + System.currentTimeMillis()</tt> is negative.
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     * 如果任务已被安排或取消，定时器被取消，或定时器线程终止。
     * @throws NullPointerException if {@code task} is null
     */
    public void schedule(TimerTask task, long delay) {
        if (delay < 0)
            throw new IllegalArgumentException("Negative delay.");
        sched(task, System.currentTimeMillis()+delay, 0);
    }

    /**
     * Schedules the specified task for execution at the specified time.  If
     * the time is in the past, the task is scheduled for immediate execution.
     * 安排指定任务在指定时间执行。 如果时间是过去，则安排任务立即执行。
     * @param task task to be scheduled.
     * @param time time at which task is to be executed.
     * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     * @throws NullPointerException if {@code task} or {@code time} is null
     */
    public void schedule(TimerTask task, Date time) {
        sched(task, time.getTime(), 0);
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>,
     * beginning after the specified delay.  Subsequent executions take place
     * at approximately regular intervals separated by the specified period.
     * 安排指定任务以重复固定延迟执行，在指定延迟后开始。 随后的执行以大约固定的时间间隔进行，间隔指定的时间段。
     *
     * <p>In fixed-delay execution, each execution is scheduled relative to
     * the actual execution time of the previous execution.  If an execution
     * is delayed for any reason (such as garbage collection or other
     * background activity), subsequent executions will be delayed as well.
     * In the long run, the frequency of execution will generally be slightly
     * lower than the reciprocal of the specified period (assuming the system
     * clock underlying <tt>Object.wait(long)</tt> is accurate).
     * 在固定延迟执行中，每次执行都是相对于前一次执行的实际执行时间安排的。
     * 如果由于任何原因（例如垃圾收集或其他后台活动）延迟执行，则后续执行也会延迟。
     * 从长远来看，执行频率一般会略低于指定周期的倒数（假设系统时钟底层 Object.wait(long) 是准确的）。
     *
     * <p>Fixed-delay execution is appropriate for recurring activities
     * that require "smoothness."  In other words, it is appropriate for
     * activities where it is more important to keep the frequency accurate
     * in the short run than in the long run.  This includes most animation
     * tasks, such as blinking a cursor at regular intervals.  It also includes
     * tasks wherein regular activity is performed in response to human
     * input, such as automatically repeating a character as long as a key
     * is held down.
     * 固定延迟执行适用于需要“顺利”的重复活动。换句话说，它适用于在短期内保持频率准确比在长期内更重要的活动。
     * 这包括大多数动画任务，例如定期闪烁光标。 它还包括响应人工输入执行常规活动的任务，例如只要按住一个键就自动重复一个字符。
     * @param task   task to be scheduled.
     * @param delay  delay in milliseconds before task is to be executed.
     * @param period time in milliseconds between successive task executions.连续任务执行之间的时间（以毫秒为单位）。
     * @throws IllegalArgumentException if {@code delay < 0}, or
     *         {@code delay + System.currentTimeMillis() < 0}, or
     *         {@code period <= 0}
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     * @throws NullPointerException if {@code task} is null
     */
    public void schedule(TimerTask task, long delay, long period) {
        if (delay < 0)
            throw new IllegalArgumentException("Negative delay.");
        if (period <= 0)
            throw new IllegalArgumentException("Non-positive period.");
        sched(task, System.currentTimeMillis()+delay, -period);
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>,
     * beginning at the specified time. Subsequent executions take place at
     * approximately regular intervals, separated by the specified period.
     * 从指定时间开始，安排指定任务以重复固定延迟执行。 随后的执行以大约固定的时间间隔进行，间隔指定的时间段。
     *
     * <p>In fixed-delay execution, each execution is scheduled relative to
     * the actual execution time of the previous execution.  If an execution
     * is delayed for any reason (such as garbage collection or other
     * background activity), subsequent executions will be delayed as well.
     * In the long run, the frequency of execution will generally be slightly
     * lower than the reciprocal of the specified period (assuming the system
     * clock underlying <tt>Object.wait(long)</tt> is accurate).  As a
     * consequence of the above, if the scheduled first time is in the past,
     * it is scheduled for immediate execution.
     * 在固定延迟执行中，每次执行都是相对于前一次执行的实际执行时间安排的。
     * 如果由于任何原因（例如垃圾收集或其他后台活动）延迟执行，则后续执行也会延迟。
     * 从长远来看，执行频率一般会略低于指定周期的倒数（假设系统时钟底层 Object.wait(long) 是准确的）。
     * 作为上述的结果，如果预定的第一次是过去的，则预定立即执行。
     *
     * <p>Fixed-delay execution is appropriate for recurring activities
     * that require "smoothness."  In other words, it is appropriate for
     * activities where it is more important to keep the frequency accurate
     * in the short run than in the long run.  This includes most animation
     * tasks, such as blinking a cursor at regular intervals.  It also includes
     * tasks wherein regular activity is performed in response to human
     * input, such as automatically repeating a character as long as a key
     * is held down.
     * 固定延迟执行适用于需要“顺利”的重复活动。换句话说，它适用于在短期内保持频率准确比在长期内更重要的活动。
     * 这包括大多数动画任务，例如定期闪烁光标。 它还包括响应人工输入执行常规活动的任务，例如只要按住一个键就自动重复一个字符。
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @throws IllegalArgumentException if {@code firstTime.getTime() < 0}, or
     *         {@code period <= 0}
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     * @throws NullPointerException if {@code task} or {@code firstTime} is null
     */
    public void schedule(TimerTask task, Date firstTime, long period) {
        if (period <= 0)
            throw new IllegalArgumentException("Non-positive period.");
        sched(task, firstTime.getTime(), -period);
    }

    /**
     * Schedules the specified task for repeated <i>fixed-rate execution</i>,
     * beginning after the specified delay.  Subsequent executions take place
     * at approximately regular intervals, separated by the specified period.
     * 安排指定任务以重复固定速率执行，在指定延迟后开始。 随后的执行以大约固定的时间间隔进行，间隔指定的时间段。
     *
     * <p>In fixed-rate execution, each execution is scheduled relative to the
     * scheduled execution time of the initial execution.  If an execution is
     * delayed for any reason (such as garbage collection or other background
     * activity), two or more executions will occur in rapid succession to
     * "catch up."  In the long run, the frequency of execution will be
     * exactly the reciprocal of the specified period (assuming the system
     * clock underlying <tt>Object.wait(long)</tt> is accurate).
     * 在固定速率执行中，每次执行都是相对于初始执行的计划执行时间安排的。
     * 如果由于任何原因（例如垃圾收集或其他后台活动）延迟了执行，则会快速连续发生两次或更多执行以“赶上”。
     * 从长远来看，执行频率将恰好是指定周期的倒数（假设 Object.wait(long) 底层的系统时钟是准确的）。
     *
     * <p>Fixed-rate execution is appropriate for recurring activities that
     * are sensitive to <i>absolute</i> time, such as ringing a chime every
     * hour on the hour, or running scheduled maintenance every day at a
     * particular time.  It is also appropriate for recurring activities
     * where the total time to perform a fixed number of executions is
     * important, such as a countdown timer that ticks once every second for
     * ten seconds.  Finally, fixed-rate execution is appropriate for
     * scheduling multiple repeating timer tasks that must remain synchronized
     * with respect to one another.
     * 固定速率执行适用于对绝对时间敏感的重复活动，例如每小时整点响铃，或每天在特定时间运行定期维护。
     * 它也适用于执行固定执行次数的总时间很重要的重复活动，例如倒计时计时器每秒滴答一次，持续 10 秒。
     * 最后，固定速率执行适用于调度多个必须彼此保持同步的重复计时器任务。
     *
     * @param task   task to be scheduled.
     * @param delay  delay in milliseconds before task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @throws IllegalArgumentException if {@code delay < 0}, or
     *         {@code delay + System.currentTimeMillis() < 0}, or
     *         {@code period <= 0}
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     * @throws NullPointerException if {@code task} is null
     */
    public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
        if (delay < 0)
            throw new IllegalArgumentException("Negative delay.");
        if (period <= 0)
            throw new IllegalArgumentException("Non-positive period.");
        sched(task, System.currentTimeMillis()+delay, period);
    }

    /**
     * Schedules the specified task for repeated <i>fixed-rate execution</i>,
     * beginning at the specified time. Subsequent executions take place at
     * approximately regular intervals, separated by the specified period.
     * 从指定时间开始，安排指定任务以重复固定速率执行。 随后的执行以大约固定的时间间隔进行，间隔指定的时间段。
     *
     * <p>In fixed-rate execution, each execution is scheduled relative to the
     * scheduled execution time of the initial execution.  If an execution is
     * delayed for any reason (such as garbage collection or other background
     * activity), two or more executions will occur in rapid succession to
     * "catch up."  In the long run, the frequency of execution will be
     * exactly the reciprocal of the specified period (assuming the system
     * clock underlying <tt>Object.wait(long)</tt> is accurate).  As a
     * consequence of the above, if the scheduled first time is in the past,
     * then any "missed" executions will be scheduled for immediate "catch up"
     * execution.
     * 在固定速率执行中，每次执行都是相对于初始执行的计划执行时间安排的。
     * 如果由于任何原因（例如垃圾收集或其他后台活动）延迟了执行，则会快速连续发生两次或更多执行以“赶上”。
     * 从长远来看，执行频率将恰好是指定周期的倒数（假设 Object.wait(long) 底层的系统时钟是准确的）。 
     * 由于上述原因，如果计划的第一次是在过去，那么任何“错过”的执行都将被安排立即“赶上”执行。
     *
     * <p>Fixed-rate execution is appropriate for recurring activities that
     * are sensitive to <i>absolute</i> time, such as ringing a chime every
     * hour on the hour, or running scheduled maintenance every day at a
     * particular time.  It is also appropriate for recurring activities
     * where the total time to perform a fixed number of executions is
     * important, such as a countdown timer that ticks once every second for
     * ten seconds.  Finally, fixed-rate execution is appropriate for
     * scheduling multiple repeating timer tasks that must remain synchronized
     * with respect to one another.
     * 固定速率执行适用于对绝对时间敏感的重复活动，例如每小时整点响铃，或每天在特定时间运行定期维护。
     * 它也适用于执行固定执行次数的总时间很重要的重复活动，例如倒计时计时器每秒滴答一次，持续 10 秒。
     * 最后，固定速率执行适用于调度多个必须彼此保持同步的重复计时器任务。
     *
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @throws IllegalArgumentException if {@code firstTime.getTime() < 0} or
     *         {@code period <= 0}
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     * @throws NullPointerException if {@code task} or {@code firstTime} is null
     */
    public void scheduleAtFixedRate(TimerTask task, Date firstTime,
                                    long period) {
        if (period <= 0)
            throw new IllegalArgumentException("Non-positive period.");
        sched(task, firstTime.getTime(), period);
    }

    /**
     * Schedule the specified timer task for execution at the specified
     * time with the specified period, in milliseconds.  If period is
     * positive, the task is scheduled for repeated execution; if period is
     * zero, the task is scheduled for one-time execution. Time is specified
     * in Date.getTime() format.  This method checks timer state, task state,
     * and initial execution time, but not period.
     * 调度指定的定时器任务在指定的时间以指定的周期执行，以毫秒为单位。
     * 如果 period 为正，则安排任务重复执行； 如果 period 为零，则任务计划为一次性执行。 
     * 时间以 Date.getTime() 格式指定。 此方法检查计时器状态、任务状态和初始执行时间，但不检查周期。
     *
     * @throws IllegalArgumentException if <tt>time</tt> is negative.
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     * @throws NullPointerException if {@code task} is null
     */
    private void sched(TimerTask task, long time, long period) {
        if (time < 0)
            throw new IllegalArgumentException("Illegal execution time.");

        // Constrain value of period sufficiently to prevent numeric
        // overflow while still being effectively infinitely large.
        // 充分约束 period 的值以防止数字溢出，同时仍然有效地无限大。
        if (Math.abs(period) > (Long.MAX_VALUE >> 1))
            period >>= 1;

        synchronized(queue) {
            if (!thread.newTasksMayBeScheduled)
                throw new IllegalStateException("Timer already cancelled.");

            synchronized(task.lock) {
                if (task.state != TimerTask.VIRGIN)
                    throw new IllegalStateException(
                        "Task already scheduled or cancelled");
                task.nextExecutionTime = time;
                task.period = period;
                task.state = TimerTask.SCHEDULED;
            }

            queue.add(task);
            if (queue.getMin() == task)
                queue.notify();
        }
    }

    /**
     * Terminates this timer, discarding any currently scheduled tasks.
     * Does not interfere with a currently executing task (if it exists).
     * Once a timer has been terminated, its execution thread terminates
     * gracefully, and no more tasks may be scheduled on it.
     * 终止此计时器，丢弃任何当前计划的任务。 不干扰当前正在执行的任务（如果存在）。 
     * 一旦定时器被终止，它的执行线程就会优雅地终止，并且不能在其上安排更多的任务。
     * <p>Note that calling this method from within the run method of a
     * timer task that was invoked by this timer absolutely guarantees that
     * the ongoing task execution is the last task execution that will ever
     * be performed by this timer.
     * 请注意，从由此计时器调用的定时任务的 run 方法中调用此方法绝对保证正在进行的任务是此计时器将执行的最后一个任务执行。
     * <p>This method may be called repeatedly; the second and subsequent
     * calls have no effect.
     * 该方法可能会被重复调用； 第二次和后续调用无效。
     */
    public void cancel() {
        synchronized(queue) {
            thread.newTasksMayBeScheduled = false;
            queue.clear();
            queue.notify();  // In case queue was already empty.如果队列已经是空的。
        }
    }

    /**
     * Removes all cancelled tasks from this timer's task queue.  <i>Calling
     * this method has no effect on the behavior of the timer</i>, but
     * eliminates the references to the cancelled tasks from the queue.
     * If there are no external references to these tasks, they become
     * eligible for garbage collection.
     * 从此定时任务队列中删除所有已取消的任务。 调用此方法对定时器的行为没有影响，但会从队列中消除对已取消任务的引用。 
     * 如果没有对这些任务的外部引用，它们就有条件被垃圾回收。
     * <p>Most programs will have no need to call this method.
     * It is designed for use by the rare application that cancels a large
     * number of tasks.  Calling this method trades time for space: the
     * runtime of the method may be proportional to n + c log n, where n
     * is the number of tasks in the queue and c is the number of cancelled
     * tasks.
     * 大多数程序不需要调用这个方法。 它专为取消大量任务的罕见应用程序而设计。 
     * 调用此方法是以时间换空间：该方法的运行时间可能与 n + c log n 成正比，其中 n 是队列中的任务数，c 是取消的任务数。
     * <p>Note that it is permissible to call this method from within a
     * a task scheduled on this timer.
     *  请注意，允许在此计时器上安排的任务中调用此方法。
     * @return the number of tasks removed from the queue. 从队列中删除的任务数。
     * @since 1.5
     */
     public int purge() {
         int result = 0;

         synchronized(queue) {
             for (int i = queue.size(); i > 0; i--) {
                 if (queue.get(i).state == TimerTask.CANCELLED) {
                     queue.quickRemove(i);
                     result++;
                 }
             }

             if (result != 0)
                 queue.heapify();
         }

         return result;
     }
}

/**
 * This "helper class" implements the timer's task execution thread, which
 * waits for tasks on the timer queue, executions them when they fire,
 * reschedules repeating tasks, and removes cancelled tasks and spent
 * non-repeating tasks from the queue.
 * 这个“帮助类”实现了定时器的任务执行线程，它等待定时器队列上的任务，在它们触发时执行它们，
 * 重新安排重复的任务，并从队列中移除取消的任务和花费的非重复任务。
 */
class TimerThread extends Thread {
    /**
     * This flag is set to false by the reaper to inform us that there
     * are no more live references to our Timer object.  Once this flag
     * is true and there are no more tasks in our queue, there is no
     * work left for us to do, so we terminate gracefully.  Note that
     * this field is protected by queue's monitor!
     * 收割者将此标志设置为 false 以通知我们没有更多对 Timer 对象的实时引用。 一旦这个标志为真并且我们的队列中没有更多的任务，      * 我们就没有工作要做了，所以我们优雅地终止。 请注意，此字段受队列监视器的保护！
     */
    boolean newTasksMayBeScheduled = true;

    /**
     * Our Timer's queue.  We store this reference in preference to
     * a reference to the Timer so the reference graph remains acyclic.
     * Otherwise, the Timer would never be garbage-collected and this
     * thread would never go away.
     * 我们的计时器队列。 我们优先存储此引用而不是对 Timer 的引用，因此引用图保持非循环。 
     * 否则，Timer 永远不会被垃圾收集，这个线程永远不会消失。
     */
    private TaskQueue queue;

    TimerThread(TaskQueue queue) {
        this.queue = queue;
    }

    public void run() {
        try {
            mainLoop();
        } finally {
            // Someone killed this Thread, behave as if Timer cancelled 有人杀死了这个线程，表现得好像定时器取消了
            synchronized(queue) {
                newTasksMayBeScheduled = false;
                queue.clear();  // Eliminate obsolete references 消除过时的引用
            }
        }
    }

    /**
     * The main timer loop.  (See class comment.) 主定时器循环。 （见课堂评论。）
     */
    private void mainLoop() {
        while (true) {
            try {
                TimerTask task;
                boolean taskFired;
                synchronized(queue) {
                    // Wait for queue to become non-empty 等待队列变为非空
                    while (queue.isEmpty() && newTasksMayBeScheduled)
                        queue.wait();
                    if (queue.isEmpty())
                        break; // Queue is empty and will forever remain; die 队列是空的，将永远存在； 死

                    // Queue nonempty; look at first evt and do the right thing
                    // 队列非空； 看第一个 evt 并做正确的事
                    long currentTime, executionTime;
                    task = queue.getMin();
                    synchronized(task.lock) {
                        if (task.state == TimerTask.CANCELLED) {
                            queue.removeMin();
                            continue;  // No action required, poll queue again无需任何操作，再次轮询队列
                        }
                        currentTime = System.currentTimeMillis();
                        executionTime = task.nextExecutionTime;
                        if (taskFired = (executionTime<=currentTime)) {
                            if (task.period == 0) { // Non-repeating, remove 不重复，删除
                                queue.removeMin();
                                task.state = TimerTask.EXECUTED;
                            } else { // Repeating task, reschedule 重复任务，重新安排
                                queue.rescheduleMin(
                                  task.period<0 ? currentTime   - task.period
                                                : executionTime + task.period);
                            }
                        }
                    }
                    if (!taskFired) // Task hasn't yet fired; wait 任务尚未触发； 等待
                        queue.wait(executionTime - currentTime);
                }
                if (taskFired)  // Task fired; run it, holding no locks 任务触发； 运行它，没有锁
                    task.run();
            } catch(InterruptedException e) {
            }
        }
    }
}

/**
 * This class represents a timer task queue: a priority queue of TimerTasks,
 * ordered on nextExecutionTime.  Each Timer object has one of these, which it
 * shares with its TimerThread.  Internally this class uses a heap, which
 * offers log(n) performance for the add, removeMin and rescheduleMin
 * operations, and constant time performance for the getMin operation.
 * 此类表示一个定时器任务队列：TimerTasks 的优先级队列，按 nextExecutionTime 排序。 
 * 每个 Timer 对象都有其中一个，它与 TimerThread 共享。 这个类在内部使用一个堆，
 * 它为 add、removeMin 和 rescheduleMin 操作提供 log(n) 性能，并为 getMin 操作提供恒定时间性能。
 */
class TaskQueue {
    /**
     * Priority queue represented as a balanced binary heap: the two children
     * of queue[n] are queue[2*n] and queue[2*n+1].  The priority queue is
     * ordered on the nextExecutionTime field: The TimerTask with the lowest
     * nextExecutionTime is in queue[1] (assuming the queue is nonempty).  For
     * each node n in the heap, and each descendant of n, d,
     * n.nextExecutionTime <= d.nextExecutionTime.
     * 优先级队列相当于平衡二叉堆：queue[n] 的两个子节点是 queue[2*n] 和 queue[2*n+1]。 
     * 优先级队列按 nextExecutionTime 字段排序：具有最小 nextExecutionTime 的 TimerTask 
     * 在 queue[1] 中（假设队列非空）。 对于堆中的每个节点 n，以及子节点 n、d，
     * 满足 n.nextExecutionTime <= d.nextExecutionTime
     */
    private TimerTask[] queue = new TimerTask[128];

    /**
     * The number of tasks in the priority queue.  (The tasks are stored in
     * queue[1] up to queue[size]).
     * 优先级队列中的任务数。 （任务存储在 queue[1] 到 queue[size]）。
     */
    private int size = 0;

    /**
     * Returns the number of tasks currently on the queue.
     * 返回当前队列中的任务数。
     */
    int size() {
        return size;
    }

    /**
     * Adds a new task to the priority queue.
     * 将新任务添加到优先级队列。
     */
    void add(TimerTask task) {
        // Grow backing store if necessary 必要时扩容
        if (size + 1 == queue.length)
            queue = Arrays.copyOf(queue, 2*queue.length);

        queue[++size] = task;
        fixUp(size);
    }

    /**
     * Return the "head task" of the priority queue.  (The head task is an
     * task with the lowest nextExecutionTime.)
     * 返回优先队列的“头任务”。 （头任务是 nextExecutionTime 最低的任务。）
     */
    TimerTask getMin() {
        return queue[1];
    }

    /**
     * Return the ith task in the priority queue, where i ranges from 1 (the
     * head task, which is returned by getMin) to the number of tasks on the
     * queue, inclusive.
     * 返回优先队列中的第 i 个任务，其中 i 的范围从 1（头任务，由 getMin 返回）到队列上的任务数，包含边界值。
     */
    TimerTask get(int i) {
        return queue[i];
    }

    /**
     * Remove the head task from the priority queue.从优先级队列中移除头任务。
     */
    void removeMin() {
        queue[1] = queue[size];
        queue[size--] = null;  // Drop extra reference to prevent memory leak 删除额外的引用以防止内存泄漏
        fixDown(1);
    }

    /**
     * Removes the ith element from queue without regard for maintaining
     * the heap invariant.  Recall that queue is one-based, so
     * 1 <= i <= size.
     * 从队列中删除第 i 个元素，而不考虑保持堆不变。 回想一下，队列是从一开始的，所以 1 <= i <= size。
     */
    void quickRemove(int i) {
        assert i <= size;

        queue[i] = queue[size];
        queue[size--] = null;  // Drop extra ref to prevent memory leak 删除额外的引用以防止内存泄漏
    }

    /**
     * Sets the nextExecutionTime associated with the head task to the
     * specified value, and adjusts priority queue accordingly.
     * 将头任务的 nextExecutionTime 设置为指定值，并相应地调整优先级队列。
     */
    void rescheduleMin(long newTime) {
        queue[1].nextExecutionTime = newTime;
        fixDown(1);
    }

    /**
     * Returns true if the priority queue contains no elements.如果优先级队列不包含任何元素，则返回 true。
     */
    boolean isEmpty() {
        return size==0;
    }

    /**
     * Removes all elements from the priority queue.从优先级队列中删除所有元素。
     */
    void clear() {
        // Null out task references to prevent memory leak 清空任务引用以防止内存泄漏
        for (int i=1; i<=size; i++)
            queue[i] = null;

        size = 0;
    }

    /**
     * Establishes the heap invariant (described above) assuming the heap
     * satisfies the invariant except possibly for the leaf-node indexed by k
     * (which may have a nextExecutionTime less than its parent's).
     * 建立堆不变量（如上所述）假设堆满足不变量，除了可能由 k 索引的叶节点（其 nextExecutionTime 可能小于其父节点）。
     * 
     * This method functions by "promoting" queue[k] up the hierarchy
     * (by swapping it with its parent) repeatedly until queue[k]'s
     * nextExecutionTime is greater than or equal to that of its parent.
     * 此方法通过重复“提升”队列[k] 向上层级（通过与其父级交换）直到 queue[k] 的 nextExecutionTime 大于或等于其父级。
     * 自己批准：
     * 添加节点都是往数组的最后一个加，对应到小顶堆，就是往右子树加节点。从右子树处理到左子树
     */
    private void fixUp(int k) {
        // 此处k为数组的最后一个元素的下标
        while (k > 1) {
            int j = k >> 1;// 取父节点，第一次既根节点
            // 如果根节点的nextExecutionTime小于队列最后一个nextExecutionTime说明已经排好序了
            if (queue[j].nextExecutionTime <= queue[k].nextExecutionTime)
                break;
            //交换位置
            TimerTask tmp = queue[j];  queue[j] = queue[k]; queue[k] = tmp;
            //继续下一节点的比较（从右子树，往左子树比较）
            k = j;
        }
    }

    /**
     * Establishes the heap invariant (described above) in the subtree
     * rooted at k, which is assumed to satisfy the heap invariant except
     * possibly for node k itself (which may have a nextExecutionTime greater
     * than its children's).
     * 在以 k 为根的子树中建立堆不变量（如上所述），假定它满足堆不变量，
     * 但节点 k 本身可能除外（其 nextExecutionTime 可能大于其子节点的）。
     *
     * This method functions by "demoting" queue[k] down the hierarchy
     * (by swapping it with its smaller child) repeatedly until queue[k]'s
     * nextExecutionTime is less than or equal to those of its children.
     * 该方法通过重复“降级”队列[k] 向下层级（通过将其与其较小的子级交换）来发挥作用，
     * 直到队列[k] 的 nextExecutionTime 小于或等于其子级的那些。
     * 自己批注：
     * 移除根节点（最小值）时调用该方法，移除时直接把数组最后一个元素赋值给数组第一个元素，并把最后一个元素置为null
     */
    private void fixDown(int k) {
        int j;
        while ((j = k << 1) <= size && j > 0) {
            if (j < size &&
                queue[j].nextExecutionTime > queue[j+1].nextExecutionTime)
                j++; // j indexes smallest kid （j 索引最小的节点）
            //如果根节点的数据比右子节点数据小说明排序完成
            if (queue[k].nextExecutionTime <= queue[j].nextExecutionTime)
                break;
            //交换位置
            TimerTask tmp = queue[j];  queue[j] = queue[k]; queue[k] = tmp;
            //下一轮
            k = j;
        }
    }

    /**
     * Establishes the heap invariant (described above) in the entire tree,
     * assuming nothing about the order of the elements prior to the call.
     * 在整个树中建立堆不变量（如上所述），不假设调用之前元素的顺序。
     */
    void heapify() {
        for (int i = size/2; i >= 1; i--)
            fixDown(i);
    }
}
```

![小顶堆（最小堆）_Jav_15](https://s8.51cto.com/images/blog/202105/31/01b0408655ed171ace676dca2368deda.png)