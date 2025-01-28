package com.lgzServer.redis;
import com.alibaba.nacos.common.utils.StringUtils;
import com.lgzServer.ServerConfig;
import com.lgzServer.types.GlobalType;
import com.lgzServer.types.LocalType;
import com.lgzServer.types.redis.RedisConfig;
import com.lgzServer.types.status.GlobalStatus;
import com.lgzServer.types.status.LocalStatus;
import com.lgzServer.types.status.RedisEnum;
import com.lgzServer.utils.JsonUtil;
import lombok.Getter;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.*;

@Getter
public class RedisHelper {
    public static RedisHelper getInstance(){
        return instance;
    }
    private static  RedisHelper instance;
    public static void init(){
        if(RedisHelper.instance==null) RedisHelper.instance=new RedisHelper();
    }
    private LettuceConnectionFactory lettuceConnectionFactory;
    private StringRedisTemplate stringRedisTemplate;
    private RedisHelper() {
        ServerConfig config=ServerConfig.instance;
        RedisConfig redisConfig=config.redisConfig;
        //集群模式
        if(redisConfig.redisType==RedisEnum.cluster){//
            RedisClusterConfiguration clusterConfiguration=new RedisClusterConfiguration(redisConfig.cluster.addresses);
            clusterConfiguration.setPassword(redisConfig.password);
            this.lettuceConnectionFactory=new LettuceConnectionFactory(clusterConfiguration);
        }
        //单机模式
        else if(redisConfig.redisType==RedisEnum.standalone){
            String [] address=redisConfig.standalone.address.split(":");
            String host=address[0];
            Integer port=Integer.parseInt(address[1]);
            RedisStandaloneConfiguration configuration=new RedisStandaloneConfiguration();
            configuration.setPassword(redisConfig.password);
            configuration.setHostName(host);
            configuration.setPort(port);
            if(redisConfig.standalone.database!=null){
                configuration.setDatabase(redisConfig.standalone.database);
            }
            this.lettuceConnectionFactory=new LettuceConnectionFactory(configuration);
        }
        //sentinel 模式
        else if(redisConfig.redisType==RedisEnum.sentinel){
            RedisSentinelConfiguration sentinelConfiguration=new RedisSentinelConfiguration();
            sentinelConfiguration.setMaster(redisConfig.sentinel.masterName);
            for(String address:redisConfig.sentinel.addresses){
                String[] parts = address.split(":");
                sentinelConfiguration.sentinel(parts[0], Integer.parseInt(parts[1]));
            }
            sentinelConfiguration.setPassword(redisConfig.password);
            this.lettuceConnectionFactory=new LettuceConnectionFactory(sentinelConfiguration);
        }
        lettuceConnectionFactory.start();
        this.stringRedisTemplate=new StringRedisTemplate(lettuceConnectionFactory);
        this.stringRedisTemplate.opsForValue().set("test","test");
    }

    public void updateGlobalStatus(String globalId, GlobalStatus status){
        stringRedisTemplate.opsForHash().put(globalId,"status",JsonUtil.objToJson(status));
    }

    public GlobalType getGlobalType(String globalId){
        Map<Object,Object> map= stringRedisTemplate.opsForHash().entries(globalId);
        String status=map.get("status").toString();//
        map.remove("status");
        HashMap<String,LocalType> typeMaps=new HashMap<>();
        for(Object key:map.keySet()){
            LocalType localType=JsonUtil.jsonToObject(map.get(key).toString(), LocalType.class);
            typeMaps.put(key.toString(),localType);
        }
        GlobalStatus globalStatus=null;
        if(StringUtils.hasLength(status)){//如果当前有状态
            globalStatus=JsonUtil.jsonToObject(status,GlobalStatus.class);
        }
        //如果当前还没有确定最终结果 那么就进行进一步判断
        if(globalStatus==null||globalStatus==GlobalStatus.wait){
            globalStatus=null;
            for(String key:typeMaps.keySet()){
                LocalType localType=typeMaps.get(key);
                if(localType.status== LocalStatus.wait){
                   globalStatus=GlobalStatus.wait;
                }
                else if(localType.status== LocalStatus.fail){
                    globalStatus=GlobalStatus.fail;
                }
            }
            if(globalStatus==null){//如果不是fail 或者wait
                globalStatus=GlobalStatus.success;
            }
            updateGlobalStatus(globalId,globalStatus);//更新redis中的分布式事务状态
        }
        GlobalType globalType=new GlobalType(globalStatus,typeMaps);
        return  globalType;
    }

    //删除分布式事务的记录
    public void deleteGlobalTransaction(String globalId){
        stringRedisTemplate.delete(globalId);
    }

}
