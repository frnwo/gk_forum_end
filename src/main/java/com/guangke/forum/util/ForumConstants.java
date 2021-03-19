package com.guangke.forum.util;

public interface ForumConstants {
    /*
        默认登录凭证失效时间 12小时
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登录凭证失效时间 100天
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;
    /**
     * 1 :帖子实体
     */
    int ENTITY_TYPE_DISCUSSPOST = 1;

    /**
     * 2 ：评论实体
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 3: 用户实体
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 系统用户
     */
    int SYSTEM_USER = 1;

    /**
     * 帖子status : 0:正常 1：加精 2：删除
     */

    int STATUS_DELETE = 2;

    /**
     * 主题：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题: 点赞
     */
    String TOPIC_LIKE = "like";

}
