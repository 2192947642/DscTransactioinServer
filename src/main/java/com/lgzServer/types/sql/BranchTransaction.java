package com.lgzServer.types.sql;

import com.lgzServer.types.status.BranchStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class BranchTransaction {

    public static String generateBranchId(){
        return "branch_"+ UUID.randomUUID();
    }
    private String globalId;
    private String branchId;
    private String applicationName;//服务名
    private String serverAddress;
    private BranchStatus status;//本地事务的状态
    private String beginTime;//开启时间
}
