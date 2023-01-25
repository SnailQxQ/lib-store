package com.turbine.tnd;

import com.turbine.tnd.bean.*;
import com.turbine.tnd.dao.*;
import com.turbine.tnd.dto.ShareResourceDTO;
import com.turbine.tnd.service.FileService;
import com.turbine.tnd.service.UserService;
import com.turbine.tnd.utils.MD5Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    UserService us;
    @Autowired
    ResourceDao redao;
    @Autowired
    FileService fs;

    @Autowired
    UserResourceDao urdao;

    @Autowired
    UserShareResourceDao usrDao;
    @Autowired
    public RedisTemplate redisTemplate;
    @Autowired
    public FolderDao fdao;



    @Test
    public void  testRedis(){
        redisTemplate.opsForValue().set("ntxz","568");
        System.out.println(redisTemplate.opsForValue().get("ntxz"));
    }

    @Test
    public void t1(){
        fs.removeOverDueTempFile();
    }

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
    //新建文件夹
    @Test
    public void test7() {
        Message message = us.mkdirFolder(0, "admin", "新建文件夹");
        System.out.println(message);
    }
    //查询文件夹
    @Test
    public void test8() {
        List<User> users = udao.inquireById(1);
        users.forEach(System.out::println);
        System.out.println("+====================");
        List<User> users2 = udao.inquireById();
        users2.forEach(System.out::println);

    }

    @Test
    public void test9(){
        System.out.println("==== test9 ====");
        us.recoverFolder(43,"admin");
    }

    @Test
    public void test10(){
        String shareName =  "32cca5f8079ac8d39de54e72d0a33675";
        int userId = 1;
        usrDao.delelteShareResourceByRName(shareName,userId);
        //UserResource userResource = urdao.inquireUserResourceById(97);

    }

}
