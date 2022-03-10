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
         *
         * 小顶堆，按nextExecutionTime排序，越小越在前
         * private final TaskQueue queue = new TaskQueue();
         *
         * 任务轮询线程
         * private final TimerThread thread = new TimerThread(queue);
         */
    }
}
