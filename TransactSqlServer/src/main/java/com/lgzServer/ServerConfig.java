package com.lgzServer;

import com.lgzServer.types.redis.RedisConfig;
import com.lgzServer.types.status.RedisEnum;
import com.lgzServer.utils.AddressUtil;
import com.lgzServer.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Set;
@Slf4j
public class ServerConfig {
    //服务的ip
    public String serverIp;
    //服务的端口
    public int serverPort;
    //nacos
    public String nacosUserName;
    public String nacosPassword;
    public String nacosNameSpace;
    public String nacosGroup;
    public String nacosServerAddress;
    public RedisConfig redisConfig;
    public static ServerConfig instance;
    static {
        try {
            instance = getConfigByFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ServerConfig getConfigByFile()throws IOException {
        File file=new File("setting.json");
        if(file.exists()){
            FileInputStream fileInputStream=new FileInputStream(file);
            byte[] bytes= fileInputStream.readAllBytes();
            String str=new String(bytes);
            ServerConfig serverConfig= JsonUtil.jsonToObject(str,ServerConfig.class);
            if(serverConfig.serverIp=="auto"){
                try {
                    serverConfig.serverIp= AddressUtil.getIp();
                }catch (UnknownHostException e){
                    serverConfig.serverIp= AddressUtil.getExternalIP();
                    log.error(e.toString());
                }
            }
            return serverConfig;
        }else{
            throw new FileNotFoundException("setting.json文件不存在");
        }

    }
}
