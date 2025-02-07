package com.lgzServer.netty.handlers;
import com.lgzServer.netty.redis.RedisHelper;
import com.lgzServer.spring.controller.BranchTransactController;
import com.lgzServer.types.*;

import com.lgzServer.types.sql.BranchTransaction;
import com.lgzServer.types.status.GlobalStatus;
import com.lgzServer.types.status.MessageTypeEnum;
import com.lgzServer.utils.JsonUtil;
import com.lgzServer.utils.TimeUtil;
import io.netty.channel.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class MessageHandler extends SimpleChannelInboundHandler<String> {
    public MessageHandler() throws IOException {
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String string) throws Exception {
        Message message=JsonUtil.jsonToObject(string, Message.class);
        if(message.type.equals(MessageTypeEnum.ServerAddress)){
            handleServerAddress(message,channelHandlerContext.channel());
        }
    }
    private void handleServerAddress(Message msg,Channel channel){
        ServerAddress address= JsonUtil.jsonToObject(msg.content, ServerAddress.class);
        ClientChannelManager.instance.putChannel(address.serverAddress, channel);
    }
    //处理本地事务的请求
    private void sendNotice(GlobalStatus globalStatus,String globalId,Map<String, BranchTransaction> maps){//全局事务的id
        RedisHelper redisHelper=RedisHelper.getInstance();
        if(globalStatus==GlobalStatus.wait) return;//如果当前全局事务还有为执行完的
        Set<String> keys = maps.keySet();
        GlobalNotice globalNotice=new GlobalNotice();
        if(globalStatus==GlobalStatus.fail){
            globalNotice.setIsSuccess(false);
        }else if(globalStatus==GlobalStatus.success){
            globalNotice.setIsSuccess(true);
        }
        //向所有的本地事务 发送通知
        for(String branchId:keys){
           BranchTransaction branchTransaction=maps.get(branchId);
           globalNotice.setGlobalId(globalId);
           Message message=new Message(MessageTypeEnum.GlobalNotice, JsonUtil.objToJson(globalNotice), TimeUtil.getLocalTime());
           ReceiveContext receiveContext=new ReceiveContext<Message>(branchTransaction.getServerAddress(),message);
           ClientChannelManager.instance.sendMessage(receiveContext);
        }
        redisHelper.deleteGlobalTransaction(globalId);//删除redis中存储的事务
    }
}
