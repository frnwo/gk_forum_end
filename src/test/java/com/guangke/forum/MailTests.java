package com.guangke.forum;

import com.guangke.forum.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MailTests {

    @Autowired
    private MailClient mailClient;
    @Test
    public void testTextMail(){
        String content = "<span style='color:red'>zjh</span>"+"<h2>  欢迎你</h2>";
        mailClient.sendMail("2913114765@qq.com","Test",content);
    }


}
