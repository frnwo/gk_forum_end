package com.guangke.forum.mapper;

import com.guangke.forum.pojo.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //userId为0 表示查询所有帖子
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    //当只有一个参数并且该参数用在if里面，就必须为这个参数起别名
    int selectDiscussPostRows(@Param("userId")int userId);

    //插入帖子
    int insertDiscussPost(DiscussPost discussPost);
}
