package com.zzzi.reggie.dto;

import com.zzzi.reggie.entity.Setmeal;
import com.zzzi.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    //分页查询时供前端使用
    private String categoryName;
}
