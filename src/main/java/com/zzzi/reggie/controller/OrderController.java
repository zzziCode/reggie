package com.zzzi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzzi.reggie.common.BaseContext;
import com.zzzi.reggie.common.R;
import com.zzzi.reggie.entity.Orders;
import com.zzzi.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;


    //用户下单的处理方法
    //下单时直接从购物车的数据中查询到相应的订单数据然后操作即可
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("用户提交的订单为:{}", orders);
        orderService.submit(orders);
        return R.success("下单成功！");
    }

    //查看当前用户的订单
    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, currentId);
        orderService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);

    }

}
