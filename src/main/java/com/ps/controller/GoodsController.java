package com.ps.controller;


import com.ps.client.UserClient;
import com.ps.pojo.*;
import com.ps.service.GoodsService;
import com.ps.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private UserClient userClient;
    @Autowired
    private GoodsService goodsService;

    @Value("${jwt.signKey}")
    private String signKey;

    @PostMapping("/createNewGoods")
    public Result createNewGoods(@RequestHeader String Authorization, @RequestBody Goods goods){
        Claims claims = JwtUtils.parseJWT(Authorization,signKey);
        String openId = (String) claims.get("openId");
        User user=new User();
        user.setUser_id(openId);
        if((boolean)userClient.isSellerSafe(user).getData()){
            if(goods.getGoods_id()==null){
                return Result.success(goodsService.createNewGoods(goods));
            }
            return Result.success(goodsService.updateOldGoods(goods));
        }
        return Result.error("发布商品资质未通过");
    }

    @PostMapping("/getGoodsById")
    public Result getGoodsById(@RequestBody Goods goods){
        return Result.success(goodsService.getGoodsById(goods.getGoods_id()));
    }

    @PostMapping("/getGoodsByConditions")
    public Result getGoodsByConditions(@RequestBody GetGoodsByConditions getGoodsByConditions){
        return Result.success(goodsService.getGoodsByConditions(getGoodsByConditions));
    }

    @PostMapping("/getGoodsByGoodsType")
    public Result getGoodsByGoodsType(@RequestBody GetGoodsByGoodsType getGoodsByGoodsType){
        return Result.success(goodsService.getGoodsByGoodsType(getGoodsByGoodsType));
    }
}
