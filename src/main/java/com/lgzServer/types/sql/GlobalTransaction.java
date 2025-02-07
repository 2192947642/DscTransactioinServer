package com.lgzServer.types.sql;

import com.lgzServer.types.status.GlobalStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class GlobalTransaction {
    public static String generateGlobalId(){
        return "global"+"_"+ UUID.randomUUID().toString();
    }
    public String globalId;//本地事务的id
    public String beginTime;//开始时间
    public Long timeout;//超时时间 单位ms
    public GlobalStatus status;//全局事务的状态
}
