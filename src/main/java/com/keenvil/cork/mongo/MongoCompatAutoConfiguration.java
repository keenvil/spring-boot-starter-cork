package com.keenvil.cork.mongo;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Registra {@link CompatMongoTemplate} como MongoTemplate primario (reemplaza la clase MongoCompatConfig que cada
 * servicio copiaba). Es opt-in: solo con {@code keenvil.mongo.compat.enabled=true}, para que subir de version de cork
 * no cambie el MongoTemplate de los servicios que no la usaban (p.ej. townhall, que tiene su propia factory).
 */
@AutoConfiguration(beforeName = "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration")
@ConditionalOnClass(MongoTemplate.class)
@ConditionalOnProperty(prefix = "keenvil.mongo.compat", name = "enabled", havingValue = "true")
public class MongoCompatAutoConfiguration {

  @Bean
  @Primary
  @ConditionalOnMissingBean(CompatMongoTemplate.class)
  public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory) {
    return new CompatMongoTemplate(mongoDbFactory);
  }
}
