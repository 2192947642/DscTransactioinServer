package com.lgzServer.netty.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.lgzServer.utils.AddressUtil;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sound.sampled.Port;
import java.net.UnknownHostException;
import java.util.Properties;
@Service
@Data
public class NettyNacosRegistry {
    @Value("${transact.netty.port}")
    private int nettyPort;
    @Value("${transact.netty.ip}")
    private String serverIp;
    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String serverAddr;

    @Value("${spring.cloud.nacos.discovery.username:${spring.cloud.nacos.username:}}")
    String userName;

    @Value("${spring.cloud.nacos.discovery.password:${spring.cloud.nacos.password:}}")
    String password;
    @Value("${spring.cloud.nacos.discovery.namespace:public}")
    String nameSpace;
    @Value("${spring.cloud.nacos.discovery.group:DEFAULT_GROUP}")
    String group;

    NamingService namingService;
    String serviceName="TractSqlServiceNetty";
    @PostConstruct
    public void init() throws NacosException, UnknownHostException {
        this.register();
    }


    public void register() throws  NacosException {
        Properties properties=new Properties();
        //设置nacos服务的地址
        properties.setProperty("serverAddr",serverAddr);
        //如果有账号密码那么久进行设置
        if(StringUtils.hasLength(userName)&& StringUtils.hasLength(password)){
            properties.setProperty("username",userName);
            properties.setProperty("password",password);
        }
        properties.setProperty("namespace",nameSpace);
        this.namingService= NacosFactory.createNamingService(properties);
        Instance instance=new Instance();
        instance.setIp(this.serverIp);//设置ip
        instance.setPort(this.nettyPort);
        instance.setServiceName(serviceName);
        this.namingService.registerInstance(serviceName,group,instance);
    }
}
