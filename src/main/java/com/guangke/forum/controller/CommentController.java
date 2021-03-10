package com.guangke.forum.controller;


import com.guangke.forum.pojo.Comment;
import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.service.CommentService;
import com.guangke.forum.service.DiscussPostService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/comment")
public class CommentController implements ForumConstants {

    @Autowired
    CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    DiscussPostService discussPostService;

//    添加完评论之后，需要重定向到帖子详情，因此需要传进来一个帖子id
    @PostMapping("/add")
    public Map<String,String> addComment(Comment comment) {
        Map<String,String> res = new HashMap<>();
        if(hostHolder.get()==null){
            res.put("tokenErr","1");
            return res;
        }
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        comment.setUserId(hostHolder.get().getId());
        commentService.addComment(comment);
//        Integer entityUserId = null;
//        if (comment.getEntityType() == ENTITY_TYPE_DISCUSSPOST) {
//            DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
//            entityUserId = post.getUserId();
//        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
//            Comment comment1 = commentService.findCommentById(comment.getEntityId());
//            entityUserId = comment1.getUserId();
//        }
        res.put("status","ok");
        return res;
    }
}
