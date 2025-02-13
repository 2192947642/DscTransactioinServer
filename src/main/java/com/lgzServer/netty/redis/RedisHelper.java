package com.lgzServer.netty.redis;
import com.alibaba.nacos.common.utils.StringUtils;
import com.lgzServer.types.redis.GlobalType;
import com.lgzServer.types.sql.BranchTransaction;
import com.lgzServer.types.status.GlobalStatus;
import com.lgzServer.types.status.BranchStatus;
import com.lgzServer.utils.JsonUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Getter
@Component
public class RedisHelper {
    public static RedisHelper getInstance(){
        return instance;
    }
    private static  RedisHelper instance;


    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @PostConstruct
    public  void init(){
        RedisHelper.instance=this;
    }

    public void updateGlobalStatus(String globalId, GlobalStatus status){
        stringRedisTemplate.opsForHash().put(globalId,"status",JsonUtil.objToJson(status));
    }

    public GlobalType getGlobalType(String globalId){
        Map<Object,Object> map= stringRedisTemplate.opsForHash().entries(globalId);
        String status=map.get("status").toString();//
        map.remove("status");
        HashMap<String, BranchTransaction> typeMaps=new HashMap<>();
        for(Object key:map.keySet()){
            BranchTransaction branchTransaction=JsonUtil.jsonToObject(map.get(key).toString(), BranchTransaction.class);
            typeMaps.put(key.toString(),branchTransaction);
        }
        GlobalStatus globalStatus=null;
        if(StringUtils.hasLength(status)){//如果当前有状态
            globalStatus=JsonUtil.jsonToObject(status,GlobalStatus.class);
        }
        //如果当前还没有确定最终结果 那么就进行进一步判断
        if(globalStatus==null||globalStatus==GlobalStatus.wait){
            globalStatus=null;
            for(String key:typeMaps.keySet()){
                BranchTransaction branchTransaction=typeMaps.get(key);
                if(branchTransaction.getStatus()== BranchStatus.wait){
                   globalStatus=GlobalStatus.wait;
                }
                else if(branchTransaction.getStatus() == BranchStatus.fail){
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
