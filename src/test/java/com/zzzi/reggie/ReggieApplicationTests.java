package com.zzzi.reggie;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;

//@SpringBootTest
class ReggieApplicationTests {

    //测试Jedis，在java代码中操作redis
    @Test
    public void testJedis() {
        //1. 获取连接
        Jedis jedis = new Jedis("localhost",6379);


        //2. 执行操作
        jedis.set("username","小张");

        System.out.println(jedis.get("username"));

        jedis.del("username");
        System.out.println(jedis.get("username"));

        jedis.hset("myhash","addr","beijing");
        jedis.hset("myhash","city","shunyi");
        System.out.println("jedis.hgetAll(\"myhash\") = " + jedis.hgetAll("myhash"));


        System.out.println("jedis.keys(\"*\") = " + jedis.keys("*"));
        //3. 关闭连接
        jedis.close();
    }

}
