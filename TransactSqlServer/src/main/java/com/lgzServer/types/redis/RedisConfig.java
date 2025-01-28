package com.lgzServer.types.redis;

import com.lgzServer.types.status.RedisEnum;

public class RedisConfig {
    public  RedisEnum redisType;
    public  String password;
    public  Sentinel sentinel;
    public  Standalone  standalone;
    public  Cluster cluster;
}
