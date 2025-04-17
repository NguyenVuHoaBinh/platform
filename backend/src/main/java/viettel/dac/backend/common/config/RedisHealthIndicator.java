package viettel.dac.backend.common.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public Health health() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pong = new String(connection.ping());
            if ("PONG".equals(pong)) {
                return Health.up()
                        .withDetail("ping", "PONG")
                        .withDetail("version", connection.info().getProperty("redis_version"))
                        .build();
            } else {
                return Health.down()
                        .withDetail("ping", pong)
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
