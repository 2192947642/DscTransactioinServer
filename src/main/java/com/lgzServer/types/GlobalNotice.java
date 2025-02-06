package com.lgzServer.types;

import lombok.Data;

@Data
public class GlobalNotice {

    private String localId;
    private String globalId;
    private Boolean  isSuccess;//是否成功
}
