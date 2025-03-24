### 分布式事务服务端

##### 使用

###### 服务端数据库表创建

​	

```mysql
create table branch_transaction
(
    global_id        varchar(100) null comment '所属全局事务id',
    branch_id        varchar(100) not null comment '分支事务id'
        primary key,
    application_name varchar(40)  null comment '服务名',
    server_address   varchar(40)  null comment '执行者的ip地址',
    status           varchar(10)  null comment '分支事务的状态',
    begin_time       datetime     null comment '分支事务开启时间'
)
    comment '分布式事务服务端存储的 分支事务的状态 wait,    success,commit,rollback';

create index branch_transaction_begin_time_index
    on branch_transaction (begin_time);

create index branch_transaction_global_id_index
    on branch_transaction (global_id);
```

```mysql
create table global_transaction
(
    global_id  varchar(100)             not null
        primary key,
    begin_time datetime default (now()) null,
    timeout    int                      null comment '超时时间 单位ms',
    status     varchar(10)              null comment '全局事务的状态 wait success fail'
)
    comment '全局事务状态 分布式事务服务端存储';
```

##### 功能

负责接受客户端通知,创建和存储全局事务,创建和存储分支事务(存储在mysql中).

对分支事务和全局事务的提供查询接口和删除接口,对分支事务提供修改状态接口.

根据全局事务状态来负责通知客户端事务回滚和提交(通过netty来进行发送信息)

服务注册发现中心使用nacos.

 
