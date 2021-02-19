package com.guangke.forum;

import com.guangke.forum.mapper.DiscussPostMapper;
import com.guangke.forum.pojo.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import java.util.List;

@SpringBootTest
public class MapperTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void discussPostTest(){
        List<DiscussPost> posts = discussPostMapper.selectDiscussPosts(0,0,10);
        for(DiscussPost post : posts){
            System.out.println(post);
        }
        int rows = discussPostMapper.selectDiscussPostRows(132);
        System.out.println(rows);
    }


}
