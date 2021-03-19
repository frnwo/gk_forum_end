package com.guangke.forum.controller;


import com.alibaba.fastjson.JSONObject;
import com.guangke.forum.pojo.Comment;
import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.pojo.Message;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.CommentService;
import com.guangke.forum.service.DiscussPostService;
import com.guangke.forum.service.MessageService;
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

    @Autowired
    MessageService messageService;

//    添加完评论之后，需要重定向到帖子详情，因此需要传进来一个帖子id
    @PostMapping("/add")
    public Map<String,String> addComment(Comment comment) {
        Map<String,String> res = new HashMap<>();
        User u = hostHolder.get();
        if(u==null){
            res.put("tokenErr","1");
            return res;
        }
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        comment.setUserId(hostHolder.get().getId());
        commentService.addComment(comment);
        //对帖子评论而且该评论的发起者不是当前用户才要通知
        //帖子
        DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
        if(comment.getEntityType() == 1 && post.getUserId() != u.getId()){
            Message message = new Message();
            message.setFromId(SYSTEM_USER);
            message.setToId(post.getUserId());
            message.setConversationId(TOPIC_COMMENT);
            message.setCreateTime(new Date());
            //content字段是固定的三个数据,实体类型+实体id+实体所属的用户id,再加上扩展的数据
            Map<String, Object> content = new HashMap<>();
            content.put("userId", u.getId());
            content.put("entityType", 1);
            content.put("postId", post.getId());
            message.setContent(JSONObject.toJSONString(content));
            messageService.addMessage(message);
        }
        res.put("status","ok");
        return res;
    }
}
