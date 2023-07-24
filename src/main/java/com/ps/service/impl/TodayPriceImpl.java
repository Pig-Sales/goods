package com.ps.service.impl;

import com.ps.pojo.Today_Price;
import com.ps.service.TodayPriceService;
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
import java.util.List;

@Service
public class TodayPriceImpl implements TodayPriceService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Today_Price> getTodayPrice(Integer days) {
        Pageable pageable = PageRequest.of(0, days, Sort.by(Sort.Order.desc("today_price_time")));
        Query query = new Query().with(pageable);
        return mongoTemplate.find(query, Today_Price.class,"today-price");
    }

    @Override
    public Today_Price createTodayPrice(Float todayPrice, String todayPriceTime) {
        Query query = Query.query(Criteria.where("today_price_time").is(todayPriceTime));
        Update update = new Update();
        update.setOnInsert("today_price_id",new ObjectId());
        update.setOnInsert("today_price_time",todayPriceTime);
        update.setOnInsert("create_time",LocalDateTime.now().toString());
        update.set("today_price",todayPrice);
        update.set("update_time",LocalDateTime.now().toString());
        mongoTemplate.upsert(query,update,Today_Price.class,"today-price");
        return mongoTemplate.findOne(query, Today_Price.class,"today-price");
    }
}
