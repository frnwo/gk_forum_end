package com.guangke.forum.controller;

import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.pojo.Page;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.DiscussPostService;
import com.guangke.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HomeController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/index",method = RequestMethod.GET)
    //当前页：current  每页数据：limit
    public List<Map<String,Object>> index(Page page){
        page.setRows(discussPostService.findDiscussPostRows(0));

        List<DiscussPost> posts = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());
        List<Map<String,Object>> resList = new ArrayList<>();

        if(posts.size()!=0){
            for(DiscussPost p : posts){
                Map<String,Object> map = new HashMap<>();
                map.put("post",p);
                User user = userService.findUserById(p.getUserId());
                map.put("user",user);
                resList.add(map);
            }
        }
        Map pageInfo = new HashMap<>();
        pageInfo.put("totalPage",page.getTotal());
        pageInfo.put("currentPage",page.getCurrent());
        resList.add(pageInfo);
        return resList;
    }
}
