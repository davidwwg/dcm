package image.medical.idcm.decode;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DecodeThreadPool extends ThreadPoolExecutor {

    private static BlockingQueue<Runnable> bq          = null;
    private static DecodeThreadPool        tp          = null;
    private final BlockingQueue<Runnable>  rejectTasks = new LinkedBlockingQueue<Runnable>();

    public DecodeThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public DecodeThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public DecodeThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public DecodeThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        // 如果當前线程池没有空闲线程并拒绝了这个任务的话就会把任务放到拒绝队列里面
        super.setRejectedExecutionHandler(new RejectedExecutionHandler() {

            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                // Log.d(this.getClass().getName()+"|"+Thread.currentThread().getId()," === "+"Now Busy Reject"
                // );
                rejectTasks.offer(r);
            }
        });
    }

    public static DecodeThreadPool getInstance() {
        if (tp == null) {
            bq = new SynchronousQueue<Runnable>();
            // bq = new LinkedBlockingQueue<Runnable>();
            // 核心线程数
            int coreThreadNum = Runtime.getRuntime().availableProcessors() * 2;
            // 最大线程数
            int maxThreadNum = Runtime.getRuntime().availableProcessors() * 10;
            int maxLiveTime = 60;
            tp = new DecodeThreadPool(coreThreadNum, maxThreadNum, maxLiveTime, TimeUnit.SECONDS, bq);
        }
        return tp;
    }

    public void excuteTask(Runnable task) {
        super.execute(task);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
    }

    /**
     * 有线程执行完的话就从被拒绝的任务里面取任务出来做（但是不一定成功）
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        // Log.d(this.getClass().getName()+"|"+Thread.currentThread().getId()," === "+"AfterExecute Map=== "
        // + threadWithClass.toString()+"=== classMap === " +
        // nowExeNames.toString());
        Runnable rejectedTask = rejectTasks.poll();
        if (rejectedTask != null) {
            // Log.d(this.getClass().getName()+"|"+Thread.currentThread().getId()," === "+"Begin Execute Reject Task");
            super.execute(rejectedTask);
        }

    }

}
