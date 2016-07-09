package com.stardust.easyassess.assessment.conf;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.stardust.easyassess.assessment.dao.router.MultiTenantMongoDbFactory;
import com.stardust.easyassess.assessment.dao.router.TenantContext;
import com.stardust.easyassess.core.context.ContextSession;
import com.stardust.easyassess.core.context.ShardedSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Configuration
public class AssessAppConf  {
    @Value("${authentication.server}")
    private String authenticationServer;

    @Value("${assess.db.default}")
    private String defaultDB;

    @Value("${assess.db.server}")
    private String dbServer;

    @Bean
    public MongoTemplate mongoTemplate(final Mongo mongo) throws Exception {
        return new MongoTemplate(mongoDbFactory(mongo));
    }

    @Bean
    public MultiTenantMongoDbFactory mongoDbFactory(final Mongo mongo) throws Exception {
        return new MultiTenantMongoDbFactory(mongo, defaultDB);
    }

    @Bean
    public Mongo mongo() throws Exception {
        return new MongoClient(dbServer);
    }

    @Autowired
    @Scope("request")
    @Lazy
    @Bean
    public ContextSession getContextSession(HttpSession session, HttpServletRequest request) {
        Map pathVariables
                = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        String domain = (String)pathVariables.get("domain");
        if (domain == null || domain.isEmpty()) {
            domain = defaultDB;
        }

        TenantContext.setCurrentTenant(domain);

        return new ShardedSession(session, domain);
    }
}