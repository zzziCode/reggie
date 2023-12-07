package com.zzzi.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzzi.reggie.entity.ShoppingCart;

public interface ShoppingCartService extends IService<ShoppingCart> {
    public void removeByUserId(Long userId);
}
