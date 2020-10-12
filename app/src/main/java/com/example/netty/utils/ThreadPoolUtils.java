package com.example.netty.utils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jinxl on 2018-08-13.
 */

public class ThreadPoolUtils {

    /**
     * 全是核心线程，无非核心线程，
     * 也没有超时机制，任务大小也是没有限制的，数量固定，即使是空闲状态，
     * 线程不会被回收，除非线程池被关闭，
     * 任务队列采用了无界的阻塞队列LinkedBlockingQueue，
     * 执行execute方法的时候，运行的线程没有达到corePoolSize就创建核心线程执行任务，
     * 否则就阻塞在任务队列中，有空闲线程的时候去取任务执行。由于该线程池线程数固定，
     * 且不被回收，线程与线程池的生命周期同步
     * 适用于任务量比较固定但耗时长的任务。
     * @param poolName 线程池名称
     * @param nThreads 线程数量
     * @return
     */
    public static ExecutorService newFixedThreadPool(String poolName, int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),new NamedThreadFactory(poolName));
    }

    /**
     * 无核心线程，全是非核心线程，
     * 线程可以无限创建，
     * 当线程池中的线程都处于活动状态的时候，线程池会创建新的线程来处理新任务，
     * 否则会用空闲的线程来处理新任务，
     * 这类线程池的空闲线程都是有超时机制的，时长为60秒，超过60秒的空闲线程就会被回收，所以几乎不会占用什么系统资源。
     * 任务队列采用的是SynchronousQueue，这个队列是无法插入任务的，一有任务立即执行，
     * 所以CachedThreadPool比较适合任务量大但耗时少的任务。
     * @param poolName 线程池名称
     * @return
     */
    public static ExecutorService newCachedThreadPool(String poolName) {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),new NamedThreadFactory(poolName));
    }

    /**
     * 核心线程数量是固定的，非核心线程没有限制，
     * 非核心线程一闲置就会被回收，核心线程是不会回收的，
     * 当运行的线程数没有达到corePoolSize的时候，
     * 就新建线程去DelayedWorkQueue中取ScheduledFutureTask然后才去执行任务，
     * 否则就把任务添加到DelayedWorkQueue，DelayedWorkQueue会将任务排序，
     * 按新建一个非核心线程顺序执行，执行完线程就回收，然后循环。
     * 任务队列采用的DelayedWorkQueue是个无界的队列，延时执行队列任务。
     * 适用于执行定时任务和具体固定周期的重复任务。
     * @param poolName
     * @param corePoolSize
     * @return
     */
    public static ScheduledExecutorService newScheduledThreadPool(String poolName, int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize,new NamedThreadFactory(poolName));
    }

    /**
     * 只有一个核心线程，
     * 其任务队列是LinkedBlockingQueue，这是个无界的阻塞队列，
     * 因为线程池里只有一个线程，就确保所有的任务都在同一个线程中顺序执行，
     * 这样就不需要处理线程同步的问题。
     * 适用于多个任务顺序执行的场景。
     * @param poolName 线程池名称
     * @return
     */
    public static ExecutorService newSingleThreadExecutor(String poolName) {
        return new FinalizableDelegatedExecutorService
                (new ThreadPoolExecutor(1, 1,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>(),new NamedThreadFactory(poolName)));
    }

    private static class FinalizableDelegatedExecutorService
            extends DelegatedExecutorService {
        FinalizableDelegatedExecutorService(ExecutorService executor) {
            super(executor);
        }
        protected void finalize() {
            super.shutdown();
        }
    }

    /**
     * A wrapper class that exposes only the ExecutorService methods
     * of an ExecutorService implementation.
     */
    private static class DelegatedExecutorService
            extends AbstractExecutorService {
        private final ExecutorService e;
        DelegatedExecutorService(ExecutorService executor) { e = executor; }
        public void execute(Runnable command) { e.execute(command); }
        public void shutdown() { e.shutdown(); }
        public List<Runnable> shutdownNow() { return e.shutdownNow(); }
        public boolean isShutdown() { return e.isShutdown(); }
        public boolean isTerminated() { return e.isTerminated(); }
        public boolean awaitTermination(long timeout, TimeUnit unit)
                throws InterruptedException {
            return e.awaitTermination(timeout, unit);
        }
        public Future<?> submit(Runnable task) {
            return e.submit(task);
        }
        public <T> Future<T> submit(Callable<T> task) {
            return e.submit(task);
        }
        public <T> Future<T> submit(Runnable task, T result) {
            return e.submit(task, result);
        }
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
                throws InterruptedException {
            return e.invokeAll(tasks);
        }
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                             long timeout, TimeUnit unit)
                throws InterruptedException {
            return e.invokeAll(tasks, timeout, unit);
        }
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
                throws InterruptedException, ExecutionException {
            return e.invokeAny(tasks);
        }
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                               long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return e.invokeAny(tasks, timeout, unit);
        }
    }

    // 命名线程工厂
    static class NamedThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NamedThreadFactory(String name) {

            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            if (null == name || name.isEmpty()) {
                name = "pool";
            }

            namePrefix = name + "-" + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
