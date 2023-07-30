package com.ps.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ps.client.UserClient;
import com.ps.pojo.*;
import com.ps.service.GoodsService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoodsImpl implements GoodsService {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserClient userClient;

    @Override
    public Goods createNewGoods(Goods goods) {
        goods.setGoods_id((new ObjectId()).toString());
        goods.setCreate_time(LocalDateTime.now().toString());
        goods.setUpdate_time(LocalDateTime.now().toString());
        return mongoTemplate.save(goods,"goods");
    }

    @Override
    public Goods updateOldGoods(Goods goods) {
        goods.setUpdate_time(LocalDateTime.now().toString());
        Query query = Query.query(Criteria.where("goods_id").is(goods.getGoods_id()));
        Update update=new Update();
        if(goods.getGoods_name()!=null) {
            update.set("goods_name",goods.getGoods_name());
        }
        if(goods.getGoods_image()!=null) {
            update.set("goods_image",goods.getGoods_image());
        }
        if(goods.getPrice()!=null) {
            update.set("price",goods.getPrice());
        }
        if(goods.getDeposit()!=null) {
            update.set("deposit",goods.getDeposit());
        }
        if(goods.getMin_weight()!=null) {
            update.set("min_weight",goods.getMin_weight());
        }
        if(goods.getMax_weight()!=null) {
            update.set("max_weight",goods.getMax_weight());
        }
        if(goods.getGoods_number()!=null) {
            update.set("goods_number",goods.getGoods_number());
        }
        if(goods.getGoods_type()!=null) {
            update.set("goods_type",goods.getGoods_type());
        }
        update.set("update_time",goods.getUpdate_time());
        mongoTemplate.updateFirst(query,update,"goods");
        return mongoTemplate.findOne(query,Goods.class,"goods");
    }

    @Override
    public Goods getGoodsById(String goodsId) {
        Query query = Query.query(Criteria.where("goods_id").is(goodsId));
        return mongoTemplate.findOne(query,Goods.class,"goods");
    }

    @Override
    public List<Goods> getGoodsByConditions(GetGoodsByConditions getGoodsByConditions) {
        Sort sort = null;
        if(getGoodsByConditions.getOrder_field()!=null){
            if(getGoodsByConditions.getOrder_type()==null) {
                sort = Sort.by(Sort.Order.desc(getGoodsByConditions.getOrder_field()));
            }
            else if(getGoodsByConditions.getOrder_type()==1){
                sort = Sort.by(Sort.Order.asc(getGoodsByConditions.getOrder_field()));
            }
            else if(getGoodsByConditions.getOrder_type()==-1){
                sort = Sort.by(Sort.Order.desc(getGoodsByConditions.getOrder_field()));
            }
        }

        Pageable pageable;
        if (sort != null) {
            pageable = PageRequest.of(getGoodsByConditions.getPage_num()-1, getGoodsByConditions.getPage_size(),sort);
        }
        else {
            pageable = PageRequest.of(getGoodsByConditions.getPage_num()-1, getGoodsByConditions.getPage_size());
        }

        if(getGoodsByConditions.getInput_condition()==null){
            Query query = new Query().with(pageable);
            return mongoTemplate.find(query,Goods.class,"goods");
        }

        List<String> list= new ArrayList<>();
        List<User> userList =new ObjectMapper().convertValue(
                userClient.getUseridByName(getGoodsByConditions.getInput_condition()).getData(),
                new TypeReference<List<User>>(){}
        );
        userList.forEach(user -> list.add(user.getUser_id()));
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("user_id").in(list),
                Criteria.where("goods_name").regex(getGoodsByConditions.getInput_condition()),
                Criteria.where("goods_type").regex(getGoodsByConditions.getInput_condition())
        );
        Query query = Query.query(criteria).with(pageable);
        return mongoTemplate.find(query, Goods.class,"goods");
    }

    @Override
    public List<Goods> getGoodsByGoodsType(GetGoodsByGoodsType getGoodsByGoodsType) {
        Pageable pageable = PageRequest.of(getGoodsByGoodsType.getPage_num()-1, getGoodsByGoodsType.getPage_size());
        if(getGoodsByGoodsType.getGoods_type()!=null) {
            Query query=Query.query(Criteria.where("goods_type").is(getGoodsByGoodsType.getGoods_type())).with(pageable);
            return mongoTemplate.find(query,Goods.class,"goods");
        }
        Query query = new Query().with(pageable);
        return mongoTemplate.find(query,Goods.class,"goods");
    }
}
