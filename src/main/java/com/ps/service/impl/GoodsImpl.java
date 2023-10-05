package com.ps.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ps.client.UserClient;
import com.ps.pojo.*;
import com.ps.service.GoodsService;
import com.ps.utils.CacheClient;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ps.utils.RedisConstants.CACHE_GOODS_KEY;
import static com.ps.utils.RedisConstants.CACHE_GOODS_TTL;

@Service
public class GoodsImpl implements GoodsService {

    private final MongoTemplate mongoTemplate;
    private final UserClient userClient;
    private final CacheClient cacheClient;

    @Autowired
    public GoodsImpl(MongoTemplate mongoTemplate, UserClient userClient, CacheClient cacheClient,StringRedisTemplate stringRedisTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.userClient = userClient;
        this.cacheClient = cacheClient;
    }

    @Override
    public Goods createNewGoods(Goods goods) {
        goods.setGoods_id((new ObjectId()).toString());
        goods.setCreate_time(LocalDateTime.now().toString());
        goods.setUpdate_time(LocalDateTime.now().toString());
        return mongoTemplate.save(goods, "goods");
    }

    @Override
    public Goods updateOldGoods(Goods goods) {
        goods.setUpdate_time(LocalDateTime.now().toString());
        Query query = Query.query(Criteria.where("goods_id").is(goods.getGoods_id()));
        Update update = new Update();
        if (goods.getGoods_name() != null) {
            update.set("goods_name", goods.getGoods_name());
        }
        if (goods.getGoods_image() != null) {
            update.set("goods_image", goods.getGoods_image());
        }
        if (goods.getPrice() != null) {
            update.set("price", goods.getPrice());
        }
        if (goods.getMin_weight() != null) {
            update.set("min_weight", goods.getMin_weight());
        }
        if (goods.getMax_weight() != null) {
            update.set("max_weight", goods.getMax_weight());
        }
        if (goods.getGoods_number() != null) {
            update.set("goods_number", goods.getGoods_number());
        }
        if (goods.getGoods_type() != null) {
            update.set("goods_type", goods.getGoods_type());
        }
        update.set("update_time", goods.getUpdate_time());
        mongoTemplate.updateFirst(query, update, "goods");
        //删除缓存，解决双写一致
        cacheClient.stringRedisTemplate.delete(CACHE_GOODS_KEY+goods.getGoods_id());
        return this.getGoodsById(goods.getGoods_id());
    }

    @Override
    public Goods getGoodsById(String goodsId) {
        //老代码
//        Query query= Query.query(Criteria.where("goods_id").is(goodsId));
//        return mongoTemplate.findOne(query,Goods.class,"goods");
        //解决缓存穿透↓
        return cacheClient.queryWithPassThrough(
                CACHE_GOODS_KEY,
                goodsId,
                Goods.class,
                id -> mongoTemplate.findOne(Query.query(Criteria.where("goods_id").is(id)), Goods.class, "goods"),
                CACHE_GOODS_TTL,
                TimeUnit.MINUTES
        );
    }

    @Override
    public List<Goods> getGoodsByConditions(GetGoodsByConditions getGoodsByConditions) {
        Sort sort = null;
        if (getGoodsByConditions.getOrder_field() != null) {
            if (getGoodsByConditions.getOrder_type() == null) {
                sort = Sort.by(Sort.Order.desc(getGoodsByConditions.getOrder_field()));
            } else if (getGoodsByConditions.getOrder_type() == 1) {
                sort = Sort.by(Sort.Order.asc(getGoodsByConditions.getOrder_field()));
            } else if (getGoodsByConditions.getOrder_type() == -1) {
                sort = Sort.by(Sort.Order.desc(getGoodsByConditions.getOrder_field()));
            }
        }

        Pageable pageable;
        if (sort != null) {
            pageable = PageRequest.of(getGoodsByConditions.getPage_num() - 1, getGoodsByConditions.getPage_size(), sort);
        } else {
            pageable = PageRequest.of(getGoodsByConditions.getPage_num() - 1, getGoodsByConditions.getPage_size());
        }

        if (getGoodsByConditions.getInput_condition() == null) {
            Query query = new Query().with(pageable);
            return time_condition_screen(getGoodsByConditions, query);
        }

        List<String> list = new ArrayList<>();
        User user1 = new User();
        user1.setUsername(getGoodsByConditions.getInput_condition());
        List<User> userList = new ObjectMapper().convertValue(
                userClient.getUseridByName(user1).getData(),
                new TypeReference<List<User>>() {
                }
        );
        userList.forEach(user -> list.add(user.getUser_id()));
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("user_id").in(list),
                Criteria.where("goods_name").regex(getGoodsByConditions.getInput_condition()),
                Criteria.where("goods_type").regex(getGoodsByConditions.getInput_condition())
        );
        Query query = Query.query(criteria).with(pageable);
        return time_condition_screen(getGoodsByConditions, query);
    }

    private List<Goods> time_condition_screen(GetGoodsByConditions getGoodsByConditions, Query query) {
        List<Goods> res = mongoTemplate.find(query, Goods.class, "goods");
        String time_condition = getGoodsByConditions.getTime_condition();
        if (time_condition != null) {
            for (int i = 0; i < res.size(); ) {
                if (time_condition.compareTo(res.get(i).getSeller_willing_start_time()) < 0 || time_condition.compareTo(res.get(i).getSeller_willing_end_time()) > 0) {
                    res.remove(i);
                } else {
                    i++;
                }
            }
        }
        return res;
    }

    @Override
    public List<Goods> getGoodsByGoodsType(GetGoodsByGoodsType getGoodsByGoodsType) {
        Pageable pageable = PageRequest.of(getGoodsByGoodsType.getPage_num() - 1, getGoodsByGoodsType.getPage_size());
        if (getGoodsByGoodsType.getGoods_type() != null) {
            Query query = Query.query(Criteria.where("goods_type").is(getGoodsByGoodsType.getGoods_type())).with(pageable);
            return mongoTemplate.find(query, Goods.class, "goods");
        }
        Query query = new Query().with(pageable);
        return mongoTemplate.find(query, Goods.class, "goods");
    }

    @Override
    public boolean updateGoodsNumber(String goodsId, Integer goodsNumber) {
        Query query = Query.query(Criteria.where("goods_id").is(goodsId));
        Goods goods = mongoTemplate.findOne(query, Goods.class, "goods");
        assert goods != null;
        int i = goods.getGoods_number() + goodsNumber;
        if (i >= 0) {
            Update update = new Update();
            update.set("goods_number", i);
            mongoTemplate.updateFirst(query, update, "goods");
            return true;
        }
        return false;
    }

    @Override
    public List<Goods> getGoodsByUserId(String openId) {
        Query query = Query.query(Criteria.where("user_id").is(openId));
        return mongoTemplate.find(query, Goods.class, "goods");
    }
}
