package io.github.willemvlh.transformer.app.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.willemvlh.transformer.app.ServerOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaffeineCacheConfig {

    private final ServerOptions serverOptions;

    @Autowired
    public CaffeineCacheConfig(ServerOptions serverOptions) {
        this.serverOptions = serverOptions;
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("xsl");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    Caffeine <Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(serverOptions.getXslCacheMaxItems())
                .recordStats();
    }

}
