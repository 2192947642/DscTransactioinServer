package com.lgzServer.netty.handlers;
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
}
