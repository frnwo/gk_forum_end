package com.guangke.forum.controller;


import com.guangke.forum.pojo.Comment;
import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.pojo.Page;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.CommentService;
import com.guangke.forum.service.DiscussPostService;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;


@RequestMapping("/discuss")
@RestController
public class DiscussPostController implements ForumConstants {

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Value("${forum.path.domain}")
    private  String domain;

    @Value("${server.servlet.context-path}")
    private  String context;

    @Value("${forum.path.uploadImage}")
    private  String uploadPath;

    @Autowired
    private CommentService commentService;

    @PostMapping("/add")
    public Map addDiscussPost(String title,String content,String postArea, MultipartFile[] files) {
        System.out.println(files);
        Map<String,String> res = new HashMap<>();
        User user = hostHolder.get();
        //当user为空时，说明还未登录，返回浏览器403的无权限信息，中断处理
        if (user == null) {
            res.put("err","您尚未登录");
            return res;
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setPostArea(postArea);
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());
        discussPost.setUserId(user.getId());
        //将图片存到本地硬盘，并生成url放进post_img这个字段，以分号隔开
        if(files.length!=0){
            StringBuilder sb = new StringBuilder();
            for(MultipartFile image : files){
                //借用userController.uploadImg方法返回文件的访问路径
                String postImgUrl = UserController.uploadImg(domain,context,uploadPath,image);
                sb.append(postImgUrl+";");
            }
            //将所有图片的路径一起存进去
            discussPost.setPostImg(sb.toString());
        }
        discussPostService.addDiscussPost(discussPost);
        res.put("status","ok");
        return res;
    }

    @GetMapping("/detail/{postId}")
    public Map<String,Object> getDiscussPostDetail(@PathVariable("postId") int postId, Page page) {
        Map<String,Object> res = new HashMap<>();
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        res.put("post", post);
        //作者
        User user = userService.findUserById(post.getUserId());
        res.put("user", user);

        //当前用户对该帖子的点赞状态,如果没登录，就放回0，显示赞
//        int likeStatus = hostHolder.get() == null ? 0 : likeService.findLikeStatus(hostHolder.get().getId(), ENTITY_TYPE_DISCUSSPOST, postId);
//        model.addAttribute("likeStatus", likeStatus);
        /**
         * 分页
         */

        //每页5条评论
        page.setLimit(5);
        page.setRows(commentService.getCommentCount(ENTITY_TYPE_DISCUSSPOST, postId));
        //根据postId查询出评论
        List<Comment> commentList = commentService.getCommentList(
                ENTITY_TYPE_DISCUSSPOST, postId, page.getOffset(), page.getLimit());

        //因为评论里需要显示用户名，但是comment表没有该字段，所有需要改装一下
        List<Map<String, Object>> cvoList = new ArrayList<>();
        for (Comment comment : commentList) {
            //一则评论一个map
            Map<String, Object> cvoMap = new HashMap<>();

            //评论
            cvoMap.put("comment", comment);

            //评论者
            User commentUser = userService.findUserById(comment.getUserId());
            cvoMap.put("user", commentUser);

//            //评论的点赞数量  小注意事项：基本类型不存在引用，java 会复制基本类型的值
//            likeCount = likeService.findLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
//            cvoMap.put("likeCount", likeCount);

//            //当前用户对该评论的点赞状态
//            likeStatus = hostHolder.get() == null ? 0 : likeService.findLikeStatus(hostHolder.get().getId(), ENTITY_TYPE_COMMENT, comment.getId());
//            cvoMap.put("likeStatus", likeStatus);

            /**
             * 根据评论id查询出其所有回复,不管这个回复有没有目的用户id(target_id)
             */
            List<Comment> replyList = commentService.getCommentList(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
            //因为回复里需要显示用户名，所以需要改装一下
            List<Map<String, Object>> rvoList = new ArrayList<>();

            for (Comment reply : replyList) {
                //一则回复一个map
                Map<String, Object> rvoMap = new HashMap<>();
                //回复
                rvoMap.put("reply", reply);
                //回复者
                rvoMap.put("replyUser", userService.findUserById(reply.getUserId()));
                //回复的目的用户
                User targetUser = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                rvoMap.put("targetUser", targetUser);

                //回复的点赞数量
//                likeCount = likeService.findLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
//                rvoMap.put("likeCount", likeCount);

                //当前用户对该回复的点赞状态
//                likeStatus = hostHolder.get() == null ? 0 : likeService.findLikeStatus(hostHolder.get().getId(), ENTITY_TYPE_COMMENT, comment.getId());
//                rvoMap.put("likeStatus", likeStatus);
                rvoList.add(rvoMap);
            }
            cvoMap.put("replies", rvoList);
            cvoMap.put("replyCount", rvoList.size());
            cvoList.add(cvoMap);
        }
        //帖子的所有评论集合
        res.put("comments", cvoList);
        res.put("totalPage",page.getTotal());
        return res;
    }

    //帖子置顶
//    @PostMapping("/top")
//    @ResponseBody
//    public String setTop(int postId) {
//        discussPostService.updateType(postId, TYPE_TOP);
//        //触发发帖，更新es
//        Event event = new Event()
//                .setTopic(TOPIC_PUBLISH)
//                //搜索帖子时需要显示作者
//                .setUserId(hostHolder.get().getId())
//                .setEntityType(ENTITY_TYPE_DISCUSSPOST)
//                .setEntityId(postId);
//        eventProducer.fireEvent(TOPIC_PUBLISH, event);
//
//        return ForumUtils.getJSONString(0);
//    }

//    //帖子加精
//    @PostMapping("/wonderful")
//    @ResponseBody
//    public String setWonderful(int postId) {
//        discussPostService.updateStatus(postId, STATUS_WONDERFUL);
//        //触发发帖，更新es
//        Event event = new Event()
//                .setTopic(TOPIC_PUBLISH)
//                //搜索帖子时需要显示作者
//                .setUserId(hostHolder.get().getId())
//                .setEntityType(ENTITY_TYPE_DISCUSSPOST)
//                .setEntityId(postId);
//        eventProducer.fireEvent(TOPIC_PUBLISH, event);
//
//        //对帖子加精
//        String redisKey = RedisKeyUtils.getPostScoreKey();
//        redisTemplate.opsForSet().add(redisKey, postId);
//
//        return ForumUtils.getJSONString(0);
//    }

    //帖子删除
//    @PostMapping("/delete")
//    @ResponseBody
//    public String setDelete(int postId) {
//        discussPostService.updateStatus(postId, STATUS_DELETE);
//        //触发发帖，更新es
//        Event event = new Event()
//                .setTopic(TOPIC_DELETE)
//                //搜索帖子时需要显示作者
//                .setUserId(hostHolder.get().getId())
//                .setEntityType(ENTITY_TYPE_DISCUSSPOST)
//                .setEntityId(postId);
//        eventProducer.fireEvent(TOPIC_PUBLISH, event);
//        return ForumUtils.getJSONString(0);
//    }


}
