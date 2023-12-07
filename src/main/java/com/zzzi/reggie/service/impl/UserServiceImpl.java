package com.zzzi.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzzi.reggie.entity.User;
import com.zzzi.reggie.mapper.UserMapper;
import com.zzzi.reggie.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
