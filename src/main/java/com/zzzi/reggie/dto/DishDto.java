package com.zzzi.reggie.dto;

import com.zzzi.reggie.entity.Dish;
import com.zzzi.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
//由于继承了Dish这个类，所以拥有dish类的所有属性
public class DishDto extends Dish {

    //一个菜品可能对应多种口味
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
