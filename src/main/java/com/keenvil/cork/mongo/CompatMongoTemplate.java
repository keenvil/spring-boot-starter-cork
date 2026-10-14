package com.keenvil.cork.mongo;

import com.mongodb.client.MongoCollection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.Document;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * MongoTemplate que inserta con insertOne/insertMany del driver 4.x y devuelve el _id generado.
 * Era una copia identica en community, crowd, mailman, security y trebuchet; ahora vive en cork.
 */
public class CompatMongoTemplate extends MongoTemplate {

  public CompatMongoTemplate(MongoDatabaseFactory mongoDbFactory) {
    super(mongoDbFactory);
  }

  @Override
  protected Object insertDocument(final String collectionName, final Document dbDoc, final Class<?> entityClass) {
    execute(collectionName, (MongoCollection<Document> collection) -> {
      collection.insertOne(dbDoc);
      return null;
    });
    return dbDoc.get("_id");
  }

  @Override
  protected List<Object> insertDocumentList(final String collectionName, final List<Document> documents) {
    if (documents.isEmpty()) {
      return Collections.emptyList();
    }
    execute(collectionName, (MongoCollection<Document> collection) -> {
      collection.insertMany(documents);
      return null;
    });
    return documents.stream().map(doc -> doc.get("_id")).collect(Collectors.toList());
  }
}
