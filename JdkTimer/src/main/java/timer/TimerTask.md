```java
/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * A task that can be scheduled for one-time or repeated execution by a Timer.
 * 可以由 Timer 安排一次或重复执行的任务。
 * @author  Josh Bloch
 * @see     Timer
 * @since   1.3
 */

public abstract class TimerTask implements Runnable {
    /**
     * This object is used to control access to the TimerTask internals.
     * 此对象用于控制对 TimerTask 内部的访问。
     */
    final Object lock = new Object();

    /**
     * The state of this task, chosen from the constants below.
     * 此任务的状态，从以下常量中选择。
     */
    int state = VIRGIN;

    /**
     * This task has not yet been scheduled.此任务尚未安排。
     */
    static final int VIRGIN = 0;

    /**
     * This task is scheduled for execution.  If it is a non-repeating task,
     * it has not yet been executed.
     * 该任务已经被安排等待执行。 如果是非重复任务，则尚未执行。
     */
    static final int SCHEDULED   = 1;

    /**
     * This non-repeating task has already executed (or is currently
     * executing) and has not been cancelled.
     * 这个非重复任务已经执行（或当前正在执行）并且没有被取消。
     */
    static final int EXECUTED    = 2;

    /**
     * This task has been cancelled (with a call to TimerTask.cancel).
     * 此任务已被取消（通过调用 TimerTask.cancel）。
     */
    static final int CANCELLED   = 3;

    /**
     * Next execution time for this task in the format returned by
     * System.currentTimeMillis, assuming this task is scheduled for execution.
     * For repeating tasks, this field is updated prior to each task execution.
     * 此任务的下一个执行时间，采用 System.currentTimeMillis 返回的格式，假设此任务已安排执行。 
     * 对于重复任务，此字段会在每次任务执行之前更新。
     */
    long nextExecutionTime;

    /**
     * Period in milliseconds for repeating tasks.  A positive value indicates
     * fixed-rate execution.  A negative value indicates fixed-delay execution.
     * A value of 0 indicates a non-repeating task.
     * 重复任务的周期（以毫秒为单位）。 正值表示固定利率（频率）执行。 负值表示固定延迟执行。 0 表示非重复任务。
     */
    long period = 0;

    /**
     * Creates a new timer task.
     */
    protected TimerTask() {
    }

    /**
     * The action to be performed by this timer task.此计时器任务要执行的操作。
     */
    public abstract void run();

    /**
     * Cancels this timer task.  If the task has been scheduled for one-time
     * execution and has not yet run, or has not yet been scheduled, it will
     * never run.  If the task has been scheduled for repeated execution, it
     * will never run again.  (If the task is running when this call occurs,
     * the task will run to completion, but will never run again.)
     * 取消此定时任务。 如果任务已安排为一次性执行但尚未运行，或者尚未安排，则它永远不会运行。 
     * 如果任务已被安排重复执行，它将永远不会再次运行。
     * （如果此调用发生时任务正在运行，则任务将运行到完成，但永远不会再次运行。）
     *
     * <p>Note that calling this method from within the <tt>run</tt> method of
     * a repeating timer task absolutely guarantees that the timer task will
     * not run again.
     * 请注意，从可重复执行的定时任务的 run 方法中调用此方法绝对保证计时器任务不会再次运行。
     * 
     * <p>This method may be called repeatedly; the second and subsequent
     * calls have no effect.
     * 该方法可能会被重复调用； 第二次和后续调用无效。
     *
     * @return true if this task is scheduled for one-time execution and has
     *         not yet run, or this task is scheduled for repeated execution.
     *         Returns false if the task was scheduled for one-time execution
     *         and has already run, or if the task was never scheduled, or if
     *         the task was already cancelled.  (Loosely speaking, this method
     *         returns <tt>true</tt> if it prevents one or more scheduled
     *         executions from taking place.)
     * 返回：
     * 如果此任务已安排为一次性执行但尚未运行，或者此任务已安排为重复执行，则为 true。 
     * 如果任务被调度为一次性执行并且已经运行，或者如果任务从未被调度，或者如果任务已经被取消，则返回 false。 
     *（简单地说，如果该方法阻止了一个或多个预定执行的发生，则该方法返回 true。）
     */
    public boolean cancel() {
        synchronized(lock) {
            boolean result = (state == SCHEDULED);
            state = CANCELLED;
            return result;
        }
    }

    /**
     * Returns the <i>scheduled</i> execution time of the most recent
     * <i>actual</i> execution of this task.  (If this method is invoked
     * while task execution is in progress, the return value is the scheduled
     * execution time of the ongoing task execution.)
     *
     * <p>This method is typically invoked from within a task's run method, to
     * determine whether the current execution of the task is sufficiently
     * timely to warrant performing the scheduled activity:
     * <pre>{@code
     *   public void run() {
     *       if (System.currentTimeMillis() - scheduledExecutionTime() >=
     *           MAX_TARDINESS)
     *               return;  // Too late; skip this execution.
     *       // Perform the task
     *   }
     * }</pre>
     * This method is typically <i>not</i> used in conjunction with
     * <i>fixed-delay execution</i> repeating tasks, as their scheduled
     * execution times are allowed to drift over time, and so are not terribly
     * significant.
     * 返回此任务最近一次实际执行的计划执行时间。
     *（如果在任务执行过程中调用此方法，则返回值是当前任务执行的预定执行时间。）
     *
     * @return the time at which the most recent execution of this task was
     *         scheduled to occur, in the format returned by Date.getTime().
     *         The return value is undefined if the task has yet to commence
     *         its first execution.
     * 此方法通常从任务的 run 方法中调用，以确定任务的当前执行是否足够及时以保证执行计划的活动：
     * @see Date#getTime()
     */
    public long scheduledExecutionTime() {
        synchronized(lock) {
            return (period < 0 ? nextExecutionTime + period
                               : nextExecutionTime - period);
        }
    }
}
```