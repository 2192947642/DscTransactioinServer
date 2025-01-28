package com.lgzServer.types;

import lombok.Data;

@Data
public class LocalNotice {
    public String globalId;//全局事务id
    public String localId;//本地事务id
    public boolean  isSuccess;//是否成功
}
