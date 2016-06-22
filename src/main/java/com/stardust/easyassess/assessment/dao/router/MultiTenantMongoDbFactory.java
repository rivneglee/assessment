package com.stardust.easyassess.assessment.dao.router;

import java.util.HashMap;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver.IndexDefinitionHolder;
import org.springframework.data.mongodb.core.mapping.BasicMongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class MultiTenantMongoDbFactory extends SimpleMongoDbFactory {

    private final String defaultName;
    private MongoTemplate mongoTemplate;
    private static final HashMap<String, Object> databaseIndexMap = new HashMap<String, Object>();

    public MultiTenantMongoDbFactory(final Mongo mongo, final String defaultDatabaseName) {
        super(mongo, defaultDatabaseName);
        this.defaultName = defaultDatabaseName;
    }

    public void setMongoTemplate(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public DB getDb() {
        final String tlName = TenantContext.getCurrentTenant();
        final String dbToUse = (tlName != null ? tlName : this.defaultName);
        createIndexIfNecessaryFor(dbToUse);
        return super.getDb(dbToUse);
    }

    private void createIndexIfNecessaryFor(final String database) {
        if (this.mongoTemplate == null) {
            return;
        }
        //sync and init once
        boolean needsToBeCreated = false;
        synchronized (MultiTenantMongoDbFactory.class) {
            final Object syncObj = databaseIndexMap.get(database);
            if (syncObj == null) {
                databaseIndexMap.put(database, new Object());
                needsToBeCreated = true;
            }
        }
        //make sure only one thread enters with needsToBeCreated = true
        synchronized (databaseIndexMap.get(database)) {
            if (needsToBeCreated) {
                createIndexes();
            }
        }
    }

    private void createIndexes() {
        final MongoMappingContext mappingContext = (MongoMappingContext) this.mongoTemplate.getConverter().getMappingContext();
        final MongoPersistentEntityIndexResolver indexResolver = new MongoPersistentEntityIndexResolver(mappingContext);
        for (BasicMongoPersistentEntity<?> persistentEntity : mappingContext.getPersistentEntities()) {
            checkForAndCreateIndexes(indexResolver, persistentEntity);
        }
    }

    private void checkForAndCreateIndexes(final MongoPersistentEntityIndexResolver indexResolver, final MongoPersistentEntity<?> entity) {
        if (entity.findAnnotation(Document.class) != null) {
            for (IndexDefinitionHolder indexDefinitionHolder : indexResolver.resolveIndexForEntity(entity)) {
                this.mongoTemplate.indexOps(entity.getType()).ensureIndex(indexDefinitionHolder);
            }
        }
    }
}