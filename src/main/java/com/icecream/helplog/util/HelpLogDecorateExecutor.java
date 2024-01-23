package com.icecream.helplog.util;

import java.util.Stack;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author andre.lan
 */
public class HelpLogDecorateExecutor extends ThreadPoolExecutor {
    private ThreadPoolExecutor executor;

    public HelpLogDecorateExecutor(ThreadPoolExecutor executor) {

        super(executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getKeepAliveTime(TimeUnit.MILLISECONDS),
                TimeUnit.MILLISECONDS,
                executor.getQueue(),
                executor.getThreadFactory(),
                executor.getRejectedExecutionHandler());
        this.executor = executor;
    }

    @Override
    public void execute(Runnable command) {
        Stack stackInfo = HelpLog.copyStack();
        executor.execute(() -> {
            try {
                // 将主线程的请求信息，设置到子线程中
                HelpLog.replaceHelper(stackInfo);
                // 执行子线程，这一步不要忘了
                command.run();
            } finally {
                // 线程结束，清空这些信息，否则可能造成内存泄漏
                HelpLog.remove();
            }
        });
    }
}