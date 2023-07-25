package com.ps.service;

import com.ps.pojo.GetGoodsByConditions;
import com.ps.pojo.Goods;

import java.util.List;

public interface GoodsService {
    void createNewGoods(Goods goods);

    void updateOldGoods(Goods goods);

    Goods getGoodsById(String goodsId);

    List<Goods> getGoodsByConditions(GetGoodsByConditions getGoodsByConditions);
}
