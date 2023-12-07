package com.zzzi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzzi.reggie.common.BaseContext;
import com.zzzi.reggie.common.R;
import com.zzzi.reggie.entity.ShoppingCart;
import com.zzzi.reggie.mapper.ShoppingCartMapper;
import com.zzzi.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    //前端请求的购物车数据
    //根据当前登录的用户id查询到他的购物车数据
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(HttpServletRequest request) {
        log.info("当前用户的id为：{}", request.getSession().getAttribute("user"));

        Long userId = (Long) request.getSession().getAttribute("user");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);


        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }


    //加入购物车的功能
    //当购物车中已有当前数据时3，只用更新数据的数量即可，不用重新插入一条新的记录
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("加入购物车的数据为：{}", shoppingCart);

        //添加购物车数据之后，记录需要与当前用户的id进行关联，从而便于后续查询
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //判断当前菜品是否存在
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);

        //如果添加的是菜品
        if (shoppingCart.getDishId() != null) {
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {//添加的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        //说明当前购物车的记录不是首次添加，添加购物车时只用修改数量即可
        if (one != null) {
            Integer number = one.getNumber();
            one.setNumber(number + 1);

            shoppingCartService.updateById(one);
        } else {//当前购物车的记录是首次添加，直接新增
            shoppingCart.setNumber(1);
            //首次添加需要填充创建时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one = shoppingCart;
        }
        //返回更新或者新增的这一条数据
        return R.success(one);
    }

    //清空购物车
    //直接将当前用户的所有购物车数据删除即可
    @DeleteMapping("/clean")
    public R<String> clean(HttpServletRequest request){
        log.info("当前用户的id为：{}",request.getSession().getAttribute("user"));

        Long userId = (Long) request.getSession().getAttribute("user");

        //等于当前用户id的所有数据全部删除
        shoppingCartService.removeByUserId(userId);

        return R.success("清空购物车成功");
    }

    //减少购物车中物品的数量
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart,HttpServletRequest request){
        log.info("当前要删除的购物车数据为:{}",shoppingCart);
        //获取当前用户的id
        Long userId = (Long) request.getSession().getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);

        //按照条件拼接查询条件
        queryWrapper.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,shoppingCart.getDishId());
        queryWrapper.eq(shoppingCart.getSetmealId()!=null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());

        //查询出当前要操作的记录
        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(queryWrapper);
        //如果当前记录数量为1，那么直接删除
        if(shoppingCartServiceOne.getNumber()==1){
            //直接删除
            shoppingCartService.removeById(shoppingCartServiceOne);
        }else{  //如果当前记录数量大于1，那么就减小数量
            Integer number = shoppingCartServiceOne.getNumber();
            shoppingCartServiceOne.setNumber(number-1);

            //数量减一并更新
            shoppingCartService.updateById(shoppingCartServiceOne);
        }

        return R.success("更新购物车成功！");
    }

}
