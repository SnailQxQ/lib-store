package com.turbine.tnd;

import com.turbine.tnd.bean.Resource;
import com.turbine.tnd.bean.User;
import com.turbine.tnd.dao.ResourceDao;
import com.turbine.tnd.dao.UserDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    @Autowired
    ResourceDao redao;

    @Test
    public void Test01(){
        User user = new User();
        user.setId(1);
        user.setSequence("d196a7737480848dacab13e95d36014e");

        int i = udao.updateUser(user);
        System.out.println(i);
    }
    @Test
    public void Test06(){
        User user = new User();
        user.setId(1);
        user.setPassword("safasfsafas");
        user.setSequence("d196a7737480848dacab13e95d36014e");

        User user1 = udao.inquireByPsw(user);
        System.out.println();
    }

    @Test
    public void Test02(){
        User user = new User();
        user.setId(1);
        user.setUserName("admin");
        user.setPassword("bc2545840fd3dfeba81c01611b2c35cf");
        System.out.println(udao.inquireByPsw(user));
        char s = '你';
    }

    @Test
    public void test3(){
        Resource re = new Resource();
        re.setFileName(UUID.randomUUID().toString());
        re.setSize(12312);
        re.setType_id(1);
        re.setLocation("sadasdas");

        System.out.println(re.toString());
        redao.addResource(re);
    }
    @Test
    @Transactional //测试完自动回滚
    @Rollback(value = true)
    public void test4(){
        redao.addReourceType(UUID.randomUUID().toString(),1);
    }

    @Test
    public void test6(){
        String name = "73f03127332a493072fbec6318695b13";
        Resource resource = redao.inquireByName(name);
        System.out.println(resource);
    }
}
