package com.chuhui.btcontrol.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description 线程池管理类
 * @Author GaleLiu
 * @Time 2019/05/28
 */
public class ThreadPoolManager {
    private volatile static ThreadPoolManager instance;

    private ThreadPoolExecutor mThreadPool;
    // 线程池维护线程的最少数量
    private static final int SIZE_CORE_POOL = 5;
    // 线程池维护线程的最大数量
    private static final int SIZE_MAX_POOL = 8;

    private ThreadPoolManager(){
    }

    public static ThreadPoolManager getInstance(){
        if(instance == null){
            synchronized (ThreadPoolManager.class){
                if(instance == null){
                    instance = new ThreadPoolManager();
                }
            }
        }
        return instance;
    }

    public ThreadPoolExecutor createThreadPool(){
        return createThreadPool(SIZE_CORE_POOL,SIZE_MAX_POOL,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
    }

    /**
     * 创建线程池
     * @param corePoolSize 池中所保存的线程数，包括空闲线程。
     * @param maximumPoolSize 池中允许的最大线程数。
     * @param keepAliveTime 当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间。
     * @param unit 参数的时间单位。
     * @param workQueue 执行前用于保持任务的队列。此队列仅由保持 execute 方法提交的 Runnable 任务。
     * @return
     */
    private ThreadPoolExecutor createThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue){
        if(mThreadPool != null){
            throw new RuntimeException("线程池已存在，不能再调用此方法！");
        }
        mThreadPool = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit,workQueue);
        return mThreadPool;
    }

    /**
     * 添加任务
     * @param task
     */
    public void addTask(Runnable task){
        if(task != null){
            mThreadPool.execute(task);
        }
    }
}
