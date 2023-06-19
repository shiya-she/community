package com.nowcoder.community.entity;


import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 *  message
 */

@Data
public class Message implements Serializable {
    private Integer id;

    private Integer fromId;

    private Integer toId;

    private String conversationId;

    private String content;

    private Integer status;

    private Date createTime;

}