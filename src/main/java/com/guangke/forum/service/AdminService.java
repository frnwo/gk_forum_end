package com.guangke.forum.service;

import com.guangke.forum.mapper.LoginTicketMapper;
import com.guangke.forum.mapper.UserMapper;
import com.guangke.forum.pojo.LoginTicket;
import com.guangke.forum.pojo.User;
import com.guangke.forum.util.ForumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

}
