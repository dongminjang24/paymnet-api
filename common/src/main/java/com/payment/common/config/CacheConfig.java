package com.payment.common.config;


import java.time.Duration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.payment.common.config.properties.CacheProperties;
import com.payment.common.config.properties.RedisProperties;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableCaching
public class CacheConfig {

	private final RedisProperties redisProperties;
	private final CacheProperties cacheProperties;
	private static final String REDISSON_HOST_PREFIX = "redis://";

	@Bean
	public RedisConnectionFactory connectionFactory() {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		redisStandaloneConfiguration.setHostName(redisProperties.getHost());
		redisStandaloneConfiguration.setPort(redisProperties.getPort());
		return new LettuceConnectionFactory(redisStandaloneConfiguration);
	}

	@Bean
	public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofSeconds(cacheProperties.getTtl()))
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
		return RedisCacheManager.RedisCacheManagerBuilder
			.fromConnectionFactory(connectionFactory)
			.cacheDefaults(redisCacheConfiguration)
			.build();
	}

	@Bean
	public RedissonClient redissonClient() {
		RedissonClient redisson = null;
		Config config = new Config();
		config.useSingleServer().setAddress(REDISSON_HOST_PREFIX + redisProperties.getHost() + ":" + redisProperties.getPort());
		redisson = Redisson.create(config);
		return redisson;
	}
}