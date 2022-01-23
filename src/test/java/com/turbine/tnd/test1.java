package com.turbine.tnd;

import com.turbine.tnd.TndApplicationTests;
import com.turbine.tnd.bean.User;
import com.turbine.tnd.dao.UserDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author 邱信强
 * @Description
 * @date 2022/1/19 18:16
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TndApplication.class)
public class test1 {

    @Autowired
    UserDao udao;

    @Test
    public void Test01(){
        User user = new User();
        user.setId(1);
        user.setSequence("d196a7737480848dacab13e95d36014e");

        udao.updateUser(user);
    }

    @Test
    public void Test02(){
        User user = new User();
        user.setId(1);
        user.setUserName("admin");
        user.setPassword("bc2545840fd3dfeba81c01611b2c35cf");
        System.out.println(udao.selectByPsw(user));
        char s = '你';
    }
}
