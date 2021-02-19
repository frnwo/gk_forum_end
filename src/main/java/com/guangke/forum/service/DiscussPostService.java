package com.guangke.forum.service;

import com.guangke.forum.mapper.DiscussPostMapper;
import com.guangke.forum.pojo.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit){
        return discussPostMapper.selectDiscussPosts(userId,offset,limit);
    }
    public int findDiscussPostRows(int userID){
        return discussPostMapper.selectDiscussPostRows(userID);
    }
}
