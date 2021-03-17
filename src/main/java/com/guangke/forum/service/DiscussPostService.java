package com.guangke.forum.service;

import com.guangke.forum.mapper.DiscussPostMapper;
import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,String postArea){
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,postArea);
    }
    public int findDiscussPostRows(int userID,String postArea){
        return discussPostMapper.selectDiscussPostRows(userID,postArea);
    }

//    发布帖子
    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        //转义html标记,例如将<script>这里的< 和 > 转义
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        //过滤敏感词
        post.setContent(sensitiveFilter.filter(post.getContent()));
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        return discussPostMapper.insertDiscussPost(post);
    }
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }
    public int updateCommentCount(int postId, int count) {
        return discussPostMapper.updateCommentCount(postId, count);
    }
    public int updateStatus(int postId, int status) {
        return discussPostMapper.updateStatus(postId, status);
    }
    public List<DiscussPost> search(String query){
        return discussPostMapper.search(query);
    }
}

