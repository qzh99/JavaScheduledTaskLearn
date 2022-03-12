package timer;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @Author Qin Zhenghua
 * @Date 2022/3/2 21:24
 */
public class TimerTest {
    public static void main(String[] args) {
        /**
         * Timer类是一个任务管理类，用来安排和取消任务，每个Timer实例都持有一个TaskQueue实例，TimerThread实例
         * TaskQueue用来存储任务，按下次执行时间排序，最小的在最签名；
         * TimerThread是定时任务执行类，它使用一个while死循环从TaskQueue队列中取出符合条件的任务执行
         * TimerTask 具体的任务，由用户实现
         */
        Timer timer = new Timer();
        TimerTask task1 = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Hello");
                // throw new RuntimeException("我裂开了");
            }
        };

        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Hi");
            }
        };

        //在指定时间只执行一次的任务
        timer.schedule(task1, new Date());
        //在指定延迟后只执行一次的任务
        timer.schedule(task2,2000L);
        //延迟3s后执行，后续重复执行，每次执行都是相对于前一次执行的实际执行时间安排 + 5s间隔
        timer.schedule(task1,3000L,5000L);
        //指定时间执行，后续重复执行，每次执行都是相对于初始执行的计划执行时间安排
        timer.schedule(task2,new Date(), 3000L);
        //延迟2s后执行，后续重复执行，每次执行都是相对于初始执行的计划执行时间安排
        timer.scheduleAtFixedRate(task1, 2000L, 3000L);
        //指定时间执行，每次执行都是相对于初始执行的计划执行时间安排
        timer.scheduleAtFixedRate(task1, new Date(), 3000L);

        /**
         * 注意
         */
        //timer定时任务是由TimerThread单线程执行的，若一个任务执行时间过长，会影响后续任务在准确的时间执行
        //若TimerTask任务中抛出异常，则会导致执行线程挂掉，后续所有任务都无法执行

        /**
         * 原理
         * timer实例持有一个队列和一个任务轮询实例
         * 队列：private final TaskQueue queue = new TaskQueue();
         * 任务轮询线程：private final TimerThread thread = new TimerThread(queue);
         *
         * 调用schedule等重载方法安排任务时都会存储到队列中，并排好序
         * 任务队列是一个小顶堆，按nextExecutionTime排序，越小越在前
         *
         * TimerThread是一个任务轮询线程
         * task = queue.getMin();
         * 从源码中可以看出它总是先取出队列中的第一个任务执行，如果这个任务执行时间超过第二个任务的开始时间，那么就会影响后续任务的执行
         * 还有TimerThread只捕获了InterruptedException中断异常，如果任务线程抛出其他异常而不处理，那么将导致轮询线程退出，后面所有任务都没得执行
         *
         */

        /**
         * 垃圾回收
         *     private final Object threadReaper = new Object() {
         *         protected void finalize() throws Throwable {
         *             synchronized(queue) {
         *                 thread.newTasksMayBeScheduled = false;
         *                 queue.notify(); // In case queue is empty.（如果队列为空。）
         *             }
         *         }
         *     };
         * 当轮训线程因为队列里没有任务而睡眠的时候，Timer 对象因为不再被引用而被垃圾回收了。这时候需要主动唤醒轮训线程，让它退出。
         * 当 Timer 被回收时，内部字段 threadPeaper 指向的对象也会被回收。所以 finalize 方法将会被调用，唤醒并终止 Timer 轮训线程。
         * 如果没有这个 threadPeaper 对象就可能会导致 JVM 里留下僵尸线程。
         *
         */

        /**
         * 关于purge()方法
         * 在任务运行之前会检查任务是不是已经被取消了，如果取消了，就从队列中移除。一旦任务开始运行 run()，
         * 对于单次任务来说它就无法被取消了，而循环任务将不会继续下次调度。如果任务没有机会得到执行（时间设置的太长），
         * 那么即使这个任务被取消了，它也会一直持续躺在任务队列中。设想如果你调度了一系列久远的任务，然后都取消了，这可能会成为一个内存泄露点。
         * 所以 Timer 还单独提供了一个 purge() 方法可以一次性清空所有的已取消的任务。
         *
         * 作者：老錢
         * 链接：https://juejin.cn/post/6844903741565435918
         * 来源：稀土掘金
         * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
         */
    }
}
