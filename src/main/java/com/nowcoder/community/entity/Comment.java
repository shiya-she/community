package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author sc
 * @date 2023-06-12下午 09:08
 */
@Data
public class Comment {
    private  int id;
    private  int userId;
    private  int entityId;
    private  int entityType;
    private  int targetId;
    private  String content;
    private  int status;
    private Date createTime;
}
