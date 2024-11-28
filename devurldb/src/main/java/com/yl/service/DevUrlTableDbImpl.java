package com.yl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.yl.DevUrlFilter;
import com.yl.entity.DevUrlTable;
import com.yl.mapper.DevUrlTableMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DevUrlTableDbImpl implements DevUrlTableService {

    static ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 10, 3, TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
            r -> new Thread(r, "dev-url_" + r.hashCode()),
            new ThreadPoolExecutor.CallerRunsPolicy());

    @Autowired
    @Lazy
    private DevUrlTableDbImpl self;

    @Autowired
    private DevUrlTableMapper devUrlTableMapper;

    @Override
    public void doing() {

        String uri = DevUrlFilter.urlThreadLocal.get();
        Set<String> tables = DevUrlFilter.tableThreadLocal.get();
        log.info("header {} tables {}", uri, tables);
        for (String table : tables) {
            String finalUri = uri;
            executor.submit(() -> self.save(finalUri, table));
        }
        DevUrlFilter.tableThreadLocal.remove();
    }

    public void save(String uri, String table) {

        DevUrlTable devUrlTable = new DevUrlTable();
        devUrlTable.setUri(uri);
        devUrlTable.setTables(table);
        devUrlTable.setCreateTime(LocalDateTime.now());
        Integer count = devUrlTableMapper
                .selectCount(new LambdaQueryWrapper<DevUrlTable>().eq(DevUrlTable::getUri, uri)
                        .eq(DevUrlTable::getTables, table));
        if (count == 0) {
            devUrlTableMapper.insert(devUrlTable);
        }
    }
}
