package com.dut.pbl6_server.config;

import com.dut.pbl6_server.common.constant.CommonConstants;
import lombok.RequiredArgsConstructor;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@EnableCaching
@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private int redisPort;
    @Value("${spring.data.redis.jedis.pool.max-active}")
    private int maxConnection;
    @Value("${spring.data.redis.jedis.pool.max-idle}")
    private int maxConnectionIdle;
    @Value("${spring.data.redis.jedis.pool.min-idle}")
    private int minConnectionIdle;
    @Value("${spring.data.redis.jedis.pool.max-wait}")
    private int maxConnectionWait;
    @Value("${spring.data.redis.username}")
    private String redisUsername;
    @Value("${spring.data.redis.password}")
    private String redisPassword;
    @Value("${spring.data.redis.ssl.enabled}")
    private Boolean sslEnabled;
    @Value("${application.profile}")
    private String profile;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisHost);
        configuration.setPort(redisPort);
        if (profile.equals(CommonConstants.PROD_PROFILE)) {
            configuration.setUsername(redisUsername);
            configuration.setPassword(redisPassword);
        }
        return new JedisConnectionFactory(
            configuration,
            getJedisClientConfiguration(sslEnabled));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setExposeConnection(true);
        template.setEnableTransactionSupport(true);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    private JedisClientConfiguration getJedisClientConfiguration(boolean useSsl) {
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = JedisClientConfiguration.builder();
        GenericObjectPoolConfig<?> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxTotal(maxConnection);
        genericObjectPoolConfig.setMaxIdle(maxConnectionIdle);
        genericObjectPoolConfig.setMinIdle(minConnectionIdle);
        genericObjectPoolConfig.setMaxWait(Duration.ofSeconds(maxConnectionWait));
        return useSsl
            ? builder.usePooling().poolConfig(genericObjectPoolConfig).and().useSsl().build()
            : builder.usePooling().poolConfig(genericObjectPoolConfig).build();
    }
}
