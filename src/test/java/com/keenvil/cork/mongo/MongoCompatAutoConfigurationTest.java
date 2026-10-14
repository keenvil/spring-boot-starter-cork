package com.keenvil.cork.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

class MongoCompatAutoConfigurationTest {

  private final ApplicationContextRunner runner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(MongoCompatAutoConfiguration.class))
      // cliente real que no se conecta hasta la primera operacion (el test no toca la base)
      .withBean(MongoDatabaseFactory.class,
          () -> new SimpleMongoClientDatabaseFactory(MongoClients.create("mongodb://localhost:1"), "test"));

  @Test
  void apagadoPorDefecto() {
    runner.run(ctx -> assertThat(ctx).doesNotHaveBean(MongoTemplate.class));
  }

  @Test
  void conElFlagRegistraCompatMongoTemplate() {
    runner.withPropertyValues("keenvil.mongo.compat.enabled=true")
        .run(ctx -> assertThat(ctx.getBean(MongoTemplate.class)).isInstanceOf(CompatMongoTemplate.class));
  }
}
