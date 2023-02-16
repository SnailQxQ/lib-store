package com.turbine.tnd;

import com.turbine.tnd.bean.*;
import com.turbine.tnd.dao.*;
import com.turbine.tnd.dto.PublicResourceDTO;
import com.turbine.tnd.dto.RNavigationDTO;
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

import java.io.*;
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
    public void testRedis() {
        redisTemplate.opsForValue().set("ntxz", "568");
        System.out.println(redisTemplate.opsForValue().get("ntxz"));
    }

    @Test
    public void t1() {
        fs.removeOverDueTempFile();
    }

    @Test
    public void Test01() {
        User user = new User();
        user.setId(1);
        user.setSequence("d196a7737480848dacab13e95d36014e");

        int i = udao.updateUser(user);
        System.out.println(i);
    }

    @Test
    public void Test06() {
        User user = new User();
        user.setId(1);
        user.setPassword("safasfsafas");
        user.setSequence("d196a7737480848dacab13e95d36014e");

        User user1 = udao.inquireByPsw(user);
        System.out.println();
    }

    @Test
    public void Test02() {
        User user = new User();
        user.setId(1);
        user.setUserName("admin");
        user.setPassword("bc2545840fd3dfeba81c01611b2c35cf");
        System.out.println(udao.inquireByPsw(user));
        char s = '你';
    }

    @Test
    public void test3() {
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
    public void test4() {
        redao.addReourceType(UUID.randomUUID().toString(), 1);
    }

    @Test
    public void test6() {
        String name = "73f03127332a493072fbec6318695b13";
        Resource resource = redao.inquireByName(name);
        System.out.println(resource);
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
    public void test9() {
        System.out.println("==== test9 ====");
    }

    @Test
    public void test10() {

        //String path = "/static/2023/JANUARY/31/a50079ffe8ead3248251e480147f5e37.mp4";
        String oPath = "d:/temp/123.mp4";
        String tPath = "d:/temp/output.m3u8";
        //必须单独写成参数的形式才能执行，直接add command 会显示文件找不到
        //空格也需要分开传
        ProcessBuilder builder = new ProcessBuilder("ffmpeg", "-i", oPath, "-c:v", "libx264", "-c:a", "aac", "-strict", "-2"
                , "-f", "hls", "-hls_list_size", "2", "-hls_time", "3", tPath);
        File file = new File(oPath);
        if (!file.exists()) System.out.println(oPath + ": 文件不存在");
        //String command = "ffmpeg -i "+file.getAbsolutePath()+" -c:v libx264 -c:a aac -strict -2 -f hls -hls_list_size 2 -hls_time 3 " + tPath;
        //builder.command(command);
        builder.redirectErrorStream(true);
        Process process = null;
        try {
            process = builder.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
