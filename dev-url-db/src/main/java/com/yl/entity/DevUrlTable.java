package com.yl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author andre.lan
 */
@Data
@TableName("DEV_URL_TABLE")
public class DevUrlTable {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    private String uri;
    private String tables;
    private String crossPath;
    private LocalDateTime createTime;
}
