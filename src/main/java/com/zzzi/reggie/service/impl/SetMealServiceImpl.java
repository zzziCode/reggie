package com.zzzi.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzzi.reggie.common.CustomException;
import com.zzzi.reggie.dto.SetmealDto;
import com.zzzi.reggie.entity.Setmeal;
import com.zzzi.reggie.entity.SetmealDish;
import com.zzzi.reggie.mapper.SetMealMapper;
import com.zzzi.reggie.service.SetMealDishService;
import com.zzzi.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetMealServiceImpl extends ServiceImpl<SetMealMapper, Setmeal> implements SetMealService {

    @Autowired
    private SetMealDishService setMealDishService;

    /**
     * @author zzzi
     * @date 2023/12/4 13:53
     * 在这里需要操作两个表
     * 先将套餐进行保存，然后根据保存之后生成的套餐id
     * 填充套餐菜品表中的套餐id，之后再保存套餐菜品表的数据
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {

        //1. 先保存套餐，得到生成的id
        save(setmealDto);

        //2. 获取到套餐的id
        Long id = setmealDto.getId();

        //3. 填充所有套餐菜品中关联的套餐id
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(id);
        }

        //4. 保存所有的套餐菜品数据
        setMealDishService.saveBatch(setmealDishes);
    }

    //根据传递来的id查询一个SetmealDtoById返回
    @Override
    public SetmealDto getSetmealDtoById(Long id) {
        //先查询得到套餐
        Setmeal setmeal = getById(id);
        SetmealDto res = new SetmealDto();
        BeanUtils.copyProperties(setmeal, res);

        //再根据id查询得到套餐中的所有菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setMealDishService.list(queryWrapper);
        res.setSetmealDishes(setmealDishes);

        //将查询得到的结果返回
        return res;
    }

    //根据新传递而来的信息修改两个表
    @Override
    public void updateWithFlavors(SetmealDto setmealDto) {
        //1. 更新套餐信息
        updateById(setmealDto);

        //2. 删除原有的菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setMealDishService.remove(queryWrapper);

        //3. 插入新的菜品，插入时需要删除旧的id
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            //将菜品与当前的套餐关联上
            setmealDish.setSetmealId(setmealDto.getId());
            //将自己的旧id删除，防止重复
            setmealDish.setId(null);
        }

        setMealDishService.saveBatch(setmealDishes);
    }

    /**
     * @author zzzi
     * @date 2023/12/4 15:43
     * 根据传递而来的套餐id删除套餐表中的套餐以及套餐菜品表中的所有菜品
     */
    @Override
    //加上事务注解，当其中一个删除失败，其余的都无法删除
    @Transactional
    public void deleteWithDish(Long[] ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        int count = count(queryWrapper);
        queryWrapper.eq(Setmeal::getStatus, 1);
        count(queryWrapper);
        //当前有套餐处于起售状态，无法删除
        if (count > 0) {
            throw new CustomException("当前有套餐处于起售状态，无法删除");
        } else {//没有套餐处于起售状态，可以直接删除
            //删除套餐表中的所有套餐
            for (Long id : ids) {
                removeById(id);

                //删除套餐菜品关系表中的所有菜品
                LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
                //根据构造的条件删除
                //所有套餐id为当前待删除的id的套餐菜品关系表中的记录都要删除
                setMealDishService.remove(lambdaQueryWrapper);
            }
        }
    }
}
