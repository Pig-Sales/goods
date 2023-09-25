package com.ps.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.SpringDataMongoDB;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import java.util.concurrent.TimeUnit;

@Configuration
public class MongoConfiguration {


    @Value("${spring.data.mongodb.maxSize}")
    private int maxSize;

    @Value("${spring.data.mongodb.minSize}")
    private int minSize;

    @Value("${spring.data.mongodb.maxConnectionLifeTime}")
    private int maxConnectionLifeTime;

    @Value("${spring.data.mongodb.maxConnectionIdleTime}")
    private int maxConnectionIdleTime;

    @Value("${spring.data.mongodb.maxWaitTime}")
    private int maxWaitTime;

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.data.mongodb.one")
    public MongoProperties masterMongoProperties() {
        return new MongoProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.data.mongodb.two")
    public MongoProperties twoMongoProperties() {
        return new MongoProperties();
    }


    /**
     * 连接池配置1
     *
     * @return
     */
    @Bean
    @Primary
    public MongoDatabaseFactory mongoDatabaseFactory(@Qualifier("masterMongoProperties") MongoProperties properties) {
        return getSimpleMongoClientDatabaseFactory(properties);
    }

    /**
     * 连接池配置2
     *
     * @return
     */
    @Bean
    public MongoDatabaseFactory twoMongoDatabaseFactory(@Qualifier("twoMongoProperties") MongoProperties properties) {
        return getSimpleMongoClientDatabaseFactory(properties);
    }

    private SimpleMongoClientDatabaseFactory getSimpleMongoClientDatabaseFactory(MongoProperties properties){
        MongoClientSettings.Builder builder = MongoClientSettings.builder();
        builder.applyConnectionString(new ConnectionString(properties.getUri()));
        builder.applyToConnectionPoolSettings(b -> {
            b.maxSize(maxSize);
            b.minSize(minSize);
            b.maxConnectionLifeTime(maxConnectionLifeTime, TimeUnit.SECONDS);
            b.maxConnectionIdleTime(maxConnectionIdleTime, TimeUnit.MINUTES);
            b.maxWaitTime(maxWaitTime, TimeUnit.MILLISECONDS);
        });
        MongoClient mongoClient = MongoClients.create(builder.build(), SpringDataMongoDB.driverInformation());

        return new SimpleMongoClientDatabaseFactory(mongoClient, properties.getDatabase());
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate(@Qualifier("mongoDatabaseFactory") MongoDatabaseFactory mongoDatabaseFactory) {
        return getMongoTemplate(mongoDatabaseFactory);
    }

    @Bean
    public MongoTemplate twoMongoTemplate(@Qualifier("twoMongoDatabaseFactory") MongoDatabaseFactory mongoDatabaseFactory) {
        return getMongoTemplate(mongoDatabaseFactory);
    }

    private MongoTemplate getMongoTemplate(MongoDatabaseFactory mongoDatabaseFactory){
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDatabaseFactory);
        //去除保存实体时，spring data mongodb 自动添加的_class字段
        MappingMongoConverter mongoMapping = (MappingMongoConverter) mongoTemplate.getConverter();
        mongoMapping.setTypeMapper(new DefaultMongoTypeMapper(null));
        mongoMapping.afterPropertiesSet();
        return mongoTemplate;
    }
}
