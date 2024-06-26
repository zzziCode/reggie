package com.zzzi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzzi.reggie.common.R;
import com.zzzi.reggie.entity.User;
import com.zzzi.reggie.service.UserService;
import com.zzzi.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    //处理前端用户登录的方法
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> map, HttpSession session) {
        log.info("用户登录的信息为：{}", map);

        //根据手机号获取到当时生成的验证码，与现在接收到的验证码进行验证对比
        //log.info("用户session为:{}", session.getAttribute(map.get("phone")));

        //从redis中获取保存的验证码
        //String validCode = (String) session.getAttribute(map.get("phone"));
        String phone = map.get("phone");
        Object validCode = redisTemplate.opsForValue().get(phone);
        if (validCode != null && validCode.equals(map.get("code"))) {
            //一旦登陆成功，就删除redis中缓存的验证码
            redisTemplate.delete(phone);

            //如果是新用户，还需要自动注册，也就是将其保存到用户表中
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);

            User user = userService.getOne(queryWrapper);

            //没查询到，说明是一个新用户
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                userService.save(user);

            }
            //将已登录的用户id保存到session中
            session.setAttribute("user", user.getId());

            return R.success(user);
        } else
            return R.error("验证码错误");
    }

    //前端生成验证码的方法
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        log.info("用户的信息为：{}", user);

        String phone = user.getPhone();

        //传递了手机号才发送验证码
        if (phone != null) {
            //生成验证码，将其保存到session中
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            log.info("生成的验证码为：{}", code);

            //将生成的验证码及其对应的手机号形成一个键值对保存到session中
            //session.setAttribute(user.getPhone(), code);

            //将验证码保存到redis中，并且设置过期时间为五分钟
            redisTemplate.opsForValue().set(phone, code, 1, TimeUnit.MINUTES);
            return R.success("验证码生成成功");
        }
        return R.error("验证码发送失败");
    }
}
