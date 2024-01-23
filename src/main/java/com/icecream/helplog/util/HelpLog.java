package com.icecream.helplog.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.icecream.helplog.spi.JobLogService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author andre.lan
 */
@Slf4j
public class HelpLog {

    static String traceId = "lanjtraceId";

    private static ThreadLocal<LogObj> logLocal = ThreadLocal.withInitial(() -> {
        String uuid = RandomUtil.randomString(6);

        Stack stack = new Stack();
        stack.push(uuid);
        MDC.put(traceId, uuid);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        return new LogObj().setStack(stack).setStopWatch(stopWatch).setKeyInfoMillTimeMap(new ConcurrentHashMap<>());
    });

    /**
     * 输出普通信息
     *
     * @param msg 日志文本
     */
    public static void info(String msg, Object... var2) {

        for (int i = 0; i < var2.length; i++) {
            Object o = var2[i];
            if (o instanceof Number || o instanceof Throwable || o instanceof String) {
            } else if (o instanceof LocalDate) {
                var2[i] = ((LocalDate) o).format(DateTimeFormatter.ISO_LOCAL_DATE);
            } else if (o instanceof LocalDateTime) {
                var2[i] = ((LocalDateTime) o).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else {
                var2[i] = JsonUtil.toJson(o);
            }
        }

        String logMsg = StrUtil.format(" {} {} ", getStackInfo(), msg);

        log.info(logMsg, var2);
    }

    /**
     * 输出普通信息
     *
     * @param msg 日志文本
     */
    public static void error(String msg, Object... var2) {

        for (int i = 0; i < var2.length; i++) {
            Object o = var2[i];
            if (o instanceof Number || o instanceof Throwable || o instanceof String) {
            } else if (o instanceof LocalDate) {
                var2[i] = ((LocalDate) o).format(DateTimeFormatter.ISO_LOCAL_DATE);
            } else if (o instanceof LocalDateTime) {
                var2[i] = ((LocalDateTime) o).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else {
                var2[i] = JsonUtil.toJson(o);
            }
        }

        String logMsg = StrUtil.format(" {} {} ", getStackInfo(), msg);

        log.error(logMsg, var2);
    }

    public static void keyInfo(String msg, Object... var2) {
        info(msg, var2);
        jobLog(" {} {}" + msg,  joinKeyInfo(), getStackInfo(), var2);
    }

    /**
     * 获取堆栈信息
     * @return
     */
    static StackTraceElement getStackInfo() {
        StackTraceElement[] stacks = (new Throwable()).getStackTrace();
        for (StackTraceElement st: stacks) {
            if (!st.getClassName().equals(HelpLog.class.getName())) {
                return st;
            }
        }
        return stacks[1];
    }


    /**
     * 添加关键信息
     * @param keyInfo
     */
    public static void add(String keyInfo) {

        if (ObjectUtil.isEmpty(keyInfo)) {
            return;
        }

        logLocal.get().getStack().push(keyInfo);
        MDC.put(traceId, joinKeyInfo());

        logLocal.get().getKeyInfoMillTimeMap().put(keyInfo, System.currentTimeMillis());
        jobLog("{} {}",  joinKeyInfo(), getStackInfo());
        info("关键字开始");
    }

    @Getter
    @AllArgsConstructor
    public enum Tag {

        CONTINUE(0, "CONTINUE"),
        BREAK(1, "BREAK"),
        ;

        private Integer code;
        private String name;
    }


    public static <T> void forEach(Collection<T> collection, Function<T, Tag> function) {

        for (T each: collection) {
            String uuid = RandomUtil.randomString(6);
            HelpLog.add(uuid);
            try {
                Tag tag = function.apply(each);
                if (tag == Tag.BREAK) {
                    break;
                } else if (tag == Tag.CONTINUE) {
                    continue;
                }
            } finally {
                HelpLog.del(uuid);
            }
        }
    }

    @FunctionalInterface
    public interface NoThing {
        void run();
    }

    public static void block(String key, NoThing noThing) {

        if (null == key) {
            key = RandomUtil.randomString(6);
        }

        try {
            HelpLog.add(key);
            noThing.run();
        } catch (Exception e) {
            HelpLog.info("系统异常", e);
            throw e;
        } finally {
            HelpLog.del(key);
        }
    }

    public static <T> T block(String key, Supplier<T> supplier) {

        if (null == key) {
            key = RandomUtil.randomString(6);
        }

        try {
            HelpLog.add(key);
            T t = supplier.get();
            return t;
        } catch (Exception e) {
            HelpLog.info("系统异常", e);
            throw e;
        } finally {
            HelpLog.del(key);
        }
    }

    public static <T> void forEach(Collection<T> collection, Function<T, String> getFunction, Consumer<T> consumer) {

        for (T each: collection) {

            String key = getFunction.apply(each);
            HelpLog.add(key);
            try {
                consumer.accept(each);
            } finally {
                HelpLog.del(key);
            }
        }
    }

    public static <S, T> void forEach(Collection<T> collection, Function<T, String> getFunction, Function<T, Tag> function) {

        for (T each: collection) {

            String key = getFunction.apply(each);
            HelpLog.add(key);
            try {
                Tag tag = function.apply(each);
                if (tag == Tag.BREAK) {
                    break;
                } else if (tag == Tag.CONTINUE) {
                    continue;
                }
            } finally {
                HelpLog.del(key);
            }
        }
    }

    public static <T> void forEach(Collection<T> collection, Consumer<T> function) {

        for (T each: collection) {
            String uuid = RandomUtil.randomString(6);
            HelpLog.add(uuid);
            try {
                function.accept(each);
            } catch (Exception e) {
                HelpLog.info("系统异常", e);
                throw e;
            } finally {
                HelpLog.del(uuid);
            }
        }
    }

    public static <S, T> void forEachMap(Map<S, T> map, Consumer<Map.Entry<S, T>> function) {

        for (Map.Entry<S, T> entry: map.entrySet()) {
            S key = entry.getKey();
            String uuid;
            if (key instanceof String) {
                uuid = (String)key;
                HelpLog.add(uuid);
            } else if (key instanceof Number) {
                uuid = key.toString();
                HelpLog.add(uuid);
            } else {
                uuid = RandomUtil.randomString(6);
                HelpLog.add(uuid);
                HelpLog.info("key {}", key);
            }

            try {
                function.accept(entry);
            } catch (Exception e) {
                HelpLog.info("系统异常", e);
                throw e;
            } finally {
                HelpLog.del(uuid);
            }
        }
    }

    public static <S, T> void forEachMap(Map<S, T> map, Function<Map.Entry<S, T>, Tag> function) {

        for (Map.Entry<S, T> entry: map.entrySet()) {
            S key = entry.getKey();
            String uuid;
            if (key instanceof String) {
                uuid = (String)key;
                HelpLog.add(uuid);
            } else if (key instanceof Number) {
                uuid = key.toString();
                HelpLog.add(uuid);
            } else {
                uuid = RandomUtil.randomString(6);
                HelpLog.add(uuid);
                HelpLog.info("key {}", key);
            }

            try {
                Tag tag = function.apply(entry);
                if (tag == Tag.BREAK) {
                    break;
                } else if (tag == Tag.CONTINUE) {
                    continue;
                }
            } finally {
                HelpLog.del(uuid);
            }
        }
    }

    public static void update(String keyInfo) {
        del();
        add(keyInfo);
    }

    public static void del() {

        Stack<String> stack = logLocal.get()
                .getStack();
        if (stack.isEmpty()) {
            return;
        }
        String pop = stack.pop();
        Long startTime = logLocal.get().getKeyInfoMillTimeMap().get(pop);
        if (null != startTime) {

            Long runTime = System.currentTimeMillis() - startTime;

            jobLog("{} {} 执行时长 {}",  joinKeyInfo(), getStackInfo(), runTime);
            info("执行时长 {}", runTime);

            MDC.put(traceId, joinKeyInfo());
        }
    }

    static void jobLog(String appendLogPattern, Object ... appendLogArguments) {
        ServiceLoader<JobLogService> serviceLoaders = ServiceLoader.load(JobLogService.class);
        for (JobLogService jobLogService : serviceLoaders) {
            jobLogService.log(appendLogPattern, appendLogArguments);
        }
    }

    /**
     * 移除关键信息并打日志
     * @param keyInfo
     */
    public static void del(String keyInfo) {

        if (ObjectUtil.isEmpty(keyInfo)) {
            return;
        }

        Stack<String> stack = logLocal.get()
                .getStack();
        if (stack.isEmpty()) {
            return;
        }

        Long startTime = logLocal.get().getKeyInfoMillTimeMap().get(keyInfo);
        if (null != startTime) {

            Long runTime = System.currentTimeMillis() - startTime;
            info("执行时长 {}", runTime);

            String pop = stack.pop();
            MDC.put(traceId, joinKeyInfo());
            if (!pop.equals(keyInfo)) {
                del(keyInfo);
            }
        }
    }

    public static Stack copyStack() {
        Stack copyStack = new Stack();
        copyStack.push(joinKeyInfo());
        return copyStack;
    }

    public static void replaceHelper(Stack stack) {
        LogObj logObj = logLocal.get()
                .setStack(stack);
        MDC.put(traceId, joinKeyInfo());
    }

//    public static String defaultLogRequestId() {
//
//        String uuid = UUID.randomUUID()
//                .toString()
//                .replaceAll("-", "");
//        return uuid;
//    }

    public static String joinKeyInfo() {

        return CollUtil.join(logLocal.get().getStack(), "-");
    }

    public static <T> void run(NoThing noThing) {
        try {
            logLocal.get();
            noThing.run();
        } catch (Exception e) {
            HelpLog.info("系统异常", e);
            throw e;
        } finally {
            HelpLog.remove();
        }
    }

    public static <T> void run(String key, NoThing noThing) {
        try {
            HelpLog.add(key);
            noThing.run();
        } catch (Exception e) {
            HelpLog.info("系统异常", e);
            throw e;
        } finally {
            HelpLog.remove();
        }
    }

    public static void remove() {

        StopWatch stopWatch = logLocal.get().getStopWatch();
        stopWatch.stop();
        info("执行总时长 {}", stopWatch.getTotalTimeMillis());

        logLocal.remove();
    }

    @Data
    @Accessors(chain = true)
    public static class LogObj {
        Stack<String> stack;
        StopWatch stopWatch;
        Map<String, Long> keyInfoMillTimeMap;
    }
}