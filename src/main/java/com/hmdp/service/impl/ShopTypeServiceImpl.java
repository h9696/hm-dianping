package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import static com.hmdp.utils.RedisConstants.CACHE_TYPE_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private IShopTypeService typeService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryList() {
        String key = CACHE_TYPE_KEY;
        //1、先尝试从redis中查询
        String shopTypeJson = stringRedisTemplate.opsForValue().get(key);
        //2、判断是否为空
        if(StrUtil.isNotBlank(shopTypeJson)){
            //3、不为空  则直接返回
            List<ShopType> shopTypeList = JSONUtil.toList(shopTypeJson, ShopType.class);
            Result.ok(shopTypeList);
        }

        //4、为空 则从数据库中查询
        List<ShopType> shopTypeList = typeService
                .query().orderByAsc("sort").list();

        //5、数据库中为空 直接返回错误信息
        if(shopTypeList == null){
            Result.fail("店铺类不存在....");
        }

        //6、存在 则直接写入redis   将店铺类型信息转为JSON字符串存入redis
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shopTypeList));

        //7、返回
        return Result.ok(shopTypeList);
    }
}