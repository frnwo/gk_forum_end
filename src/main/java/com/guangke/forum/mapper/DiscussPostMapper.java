package com.guangke.forum.mapper;

import com.guangke.forum.pojo.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //userId为0 表示查询所有帖子  postArea:发帖的区域类型
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit,String postArea);

    //当只有一个参数并且该参数用在if里面，就必须为这个参数起别名
    int selectDiscussPostRows(@Param("userId")int userId,String postArea);

    //插入帖子
    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);
    DiscussPost selectDiscussPostById2(int id);

    int updateCommentCount(int postId, int count);

    int updateStatus(int id, int status);

    List<DiscussPost> search(String query);
    int updateType(int id, int type);
}
