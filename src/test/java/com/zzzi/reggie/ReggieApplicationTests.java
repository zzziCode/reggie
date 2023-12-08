package com.zzzi.reggie;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;
/**@author zzzi
 * @date 2023/12/7 19:02
 * HashMap中解决冲突的链表长度大于8时，会尝试将链表改造成红黑树，但是前提是数组的大小超过64
 * 没有超过64时会优先对数组进行扩容，扩容之后有了新的数组长度，冲突的情况就会变化
 * 当超过负载因子时就会扩容
 * 扩容涉及到重新哈希，
 */
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
