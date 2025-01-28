package com.lgzServer.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.lgzServer.ServerConfig;
import org.springframework.util.StringUtils;

import java.util.Properties;
public class NacosRegistry {
    String serviceName="TractSqlService";
    String serverAddress;
    String userName;
    String passWord;
    String nameSpace;
    String group;
    NamingService namingService;

    public NacosRegistry(){
        initFromConfig(ServerConfig.instance);
    }
    private  void initFromConfig(ServerConfig config){
        this.serverAddress=ServerConfig.instance.nacosServerAddress;
        this.userName=ServerConfig.instance.nacosUserName;
        this.passWord=ServerConfig.instance.nacosPassword;
        this.nameSpace=ServerConfig.instance.nacosNameSpace;
        this.group=ServerConfig.instance.nacosGroup;
    }
    public void register() throws  NacosException {
        Properties properties=new Properties();
        //设置nacos服务的地址
        properties.setProperty("serverAddr",serverAddress);
        //如果有账号密码那么久进行设置
        if(StringUtils.hasLength(userName)&& StringUtils.hasLength(passWord)){
            properties.setProperty("username",userName);
            properties.setProperty("password",passWord);
        }
        properties.setProperty("namespace",nameSpace);
        this.namingService= NacosFactory.createNamingService(properties);
        Instance instance=new Instance();
        instance.setIp(ServerConfig.instance.serverIp);//设置ip
        instance.setPort(ServerConfig.instance.serverPort);
        instance.setServiceName(serviceName);
        this.namingService.registerInstance(serviceName,group,instance);
    }
}
