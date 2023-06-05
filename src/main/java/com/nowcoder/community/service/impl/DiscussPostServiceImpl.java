package com.nowcoder.community.service.impl;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostServiceImpl  implements DiscussPostService {
    private final DiscussPostMapper discussPostMapper;

    public DiscussPostServiceImpl(DiscussPostMapper discussPostMapper) {
        this.discussPostMapper = discussPostMapper;
    }


    @Override
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId,offset,limit);
    }

    @Override
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

}
