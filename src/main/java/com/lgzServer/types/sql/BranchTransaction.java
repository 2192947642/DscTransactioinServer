package com.lgzServer.types.sql;

import com.lgzServer.types.status.LocalStatus;
import lombok.Data;

@Data
public class BranchTransaction {

    public static String generateLocalId(){
        return "local_"+System.currentTimeMillis();
    }
    private String globalId;
    private String branchId;
    private String applicationName;//服务名
    private String serverAddress;
    private LocalStatus status;//本地事务的状态
    private String beginTime;//开启时间
}
