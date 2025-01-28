package com.lgzServer.types;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ReceiveContext<T>{
    public String remoteAddress;//接收人的地址
    public T message;
}
