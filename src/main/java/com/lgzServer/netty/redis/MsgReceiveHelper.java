package com.lgzServer.netty.redis;
import org.springframework.data.redis.core.StringRedisTemplate;
public class MsgReceiveHelper {
    public static final MsgReceiveHelper instance=new MsgReceiveHelper();
    private final String prefix="receive:";//客户端收到消息的key localId//事务的id
    public void setMessageReceive(boolean receive,String msgId){
        StringRedisTemplate stringRedisTemplate=RedisHelper.getInstance().getStringRedisTemplate();
        stringRedisTemplate.opsForValue().set(prefix+msgId,String.valueOf(receive));
    }
    public boolean getMessageReceive(String msgId){
        StringRedisTemplate stringRedisTemplate=RedisHelper.getInstance().getStringRedisTemplate();
        return Boolean.valueOf(stringRedisTemplate.opsForValue().get(prefix+msgId));
    }
    public void deleteMessageReceive(String msgId){
        StringRedisTemplate stringRedisTemplate=RedisHelper.getInstance().getStringRedisTemplate();
        stringRedisTemplate.delete(prefix+msgId);
    }

}
