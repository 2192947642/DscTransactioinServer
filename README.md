## 分布式事务服务端
#### 特点

使用nacos作为服务注册发现中心,支持集群化

使用redis作为事务的状态存储

消息重发服务,保证在客户端不断网的情况下收到来自于服务端的消息通知

使用netty框架 来进行数据传输

#### 实现

使用netty与客户端进行连接,并在nacos中进行注册,注册类如下

```java
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
```

在redis中存储事务的信息状态,事务信息类型如下

```java
package com.lgzServer.types;

import com.lgzServer.types.status.LocalStatus;
import com.lgzServer.utils.TimeUtil;
import lombok.Data;

import java.util.UUID;

@Data
public class LocalType {

    public static String generateGlobalId(){
        return "global"+"_"+UUID.randomUUID().toString();
    }
    public static String generateLocalId(){
        return "local"+"_"+UUID.randomUUID().toString();
    }
    public LocalType(){

    }
    public LocalType(String serverAddress){
        this.serverAddress=serverAddress;
        globalId=generateGlobalId();
        localId=generateLocalId();
        beginTime= TimeUtil.getLocalTime();
        status= LocalStatus.wait;
    }
    public LocalType(String globalId,String serverAddress){
        if(globalId!=null) this.globalId=globalId;
        else this.globalId=generateGlobalId();
        localId=generateLocalId();
        beginTime= TimeUtil.getLocalTime();
        status= LocalStatus.wait;
        this.serverAddress=serverAddress;
    }
    public Long trxId;//本地事务的事务id
    public String serverAddress;//当前事务的服务地址
    public String globalId;//全局事务的uuid 存放在redis中
    public String localId;//本地事务的uuid
    public String beginTime;//事务开始时间
    public LocalStatus status;//事务的状态
}
```

每当有客户端发来一个事务状态通知时,如果为成功那么就检查全局事务状态是否成功,如果为失败 那么就将全局事务状态设置为失败。

当全局事务状态为成功或者失败时,对客户端进行通知,客户端接收到通知后对事务进行提交或者回滚,随后当操作成功后将redis中的对应的本地事务进行删除.

本地事务通知类如下

```java
package com.lgzServer.types;

import lombok.Data;

@Data
public class LocalNotice {
    public String globalId;//全局事务id
    public String localId;//本地事务id
    public boolean  isSuccess;//是否成功
}
```

全局事务通知如下

```java
@Data
public class GlobalNotice {
    public LocalType localType;
    public Boolean  isSuccess;//是否成功
}
```

使用的lua删除脚本如下

```lua
local globalId = KEYS[1]
local localId = ARGV[1]
if redis.call('HEXISTS', globalId, localId) == 1 then
    redis.call('HDEL', globalId, localId)
    -- 检查哈希表中是否只剩下 'status' 键或者是否为空
    local remaining_keys = redis.call('HKEYS', globalId)
    if #remaining_keys == 0 or (#remaining_keys == 1 and remaining_keys[1] == 'status') then
        redis.call('DEL', globalId)
    end
end
return nil
```



#### 配置文件介绍

```json
{
  "serverIp": "127.0.0.1",//服务注册进nacos中的ip 当为auto时 会自动获得当前ip
  "serverPort": 8000,//服务的启动端口
  "redisConfig": {//redis连接设置
    "redisType": "standalone",//当前redis的类型取值为 standalone,sentinel,cluster
    "password": "",//redis的密码
    "sentinel": {
      "masterName": "master",
      "addresses": ["127.0.0.1:6379"]
    },
    "standalone": {
      "database": 0,
      "address": "127.0.0.1:6379"
    },
    "cluster": {
      "addresses": ["127.0.0.1:6379"]
    }
  },
  "nacosUserName": "", //nacos的账号
  "nacosPassword": "",//nacos的密码
  "nacosServerAddress": "http://49.235.154.48:8848",
  "nacosNameSpace": "public",
  "nacosGroup": "DEFAULT_GROUP"
}
```

###### 
