package com.zzzi.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzzi.reggie.entity.OrderDetail;
import com.zzzi.reggie.mapper.OrderDetailMapper;
import com.zzzi.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
