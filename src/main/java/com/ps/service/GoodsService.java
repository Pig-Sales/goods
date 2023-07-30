package com.ps.service;

import com.ps.pojo.GetGoodsByConditions;
import com.ps.pojo.GetGoodsByGoodsType;
import com.ps.pojo.Goods;

import java.util.List;

public interface GoodsService {
    Goods createNewGoods(Goods goods);

    Goods updateOldGoods(Goods goods);

    Goods getGoodsById(String goodsId);

    List<Goods> getGoodsByConditions(GetGoodsByConditions getGoodsByConditions);

    List<Goods> getGoodsByGoodsType(GetGoodsByGoodsType getGoodsByGoodsType);
}
