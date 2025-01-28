package com.lgzServer.service;

import com.lgzServer.handlers.ClientChannelManager;
import com.lgzServer.redis.MsgReceiveHelper;
import com.lgzServer.types.Message;
import com.lgzServer.types.ReceiveContext;
import com.lgzServer.utils.TimeUtil;
import java.text.ParseException;
import java.util.concurrent.LinkedBlockingDeque;

public class ResendService {
    public static int reSendInterval=3000;
    public static  ResendService instance=new ResendService();
    private MsgReceiveHelper msgReceiveHelper;
    private final LinkedBlockingDeque<ReceiveContext<Message>> blockingDeque=new LinkedBlockingDeque<>();
    public void addToResendQueue(ReceiveContext<Message> receiveContext){
        blockingDeque.add(receiveContext);
    }
    ResendService(){
        msgReceiveHelper=MsgReceiveHelper.instance;
    }
    //开启一个线程进行消息的重发
    public void startResendService(){
      new Thread(()->{
            while(true){
                try {
                    String now= TimeUtil.getLocalTime();
                    while(true){
                        ReceiveContext<Message> receiveContext=blockingDeque.poll();//将第一个消息从队列中取出
                        if(receiveContext==null) break;
                        boolean isReceive=msgReceiveHelper.getMessageReceive(receiveContext.message.msgId);//通过message的id判断当前的服务端是否收到了该消息
                        if(isReceive){//如果是收到了消息,则删除该消息确认
                            msgReceiveHelper.deleteMessageReceive(receiveContext.message.msgId);
                        }
                        else{//如果没有收到消息 那么就判断是否满足再次发送的条件
                            String lastSendTime=receiveContext.message.lastSendTime;
                            if(TimeUtil.getPastSeconds(now,lastSendTime)>=reSendInterval){//如果当前到达了重发的时间间隔那么进行重发
                                receiveContext.message.setLastSendTime(now);
                                ClientChannelManager.instance.sendMessage(receiveContext,true);
                            }else{
                                blockingDeque.add(receiveContext);
                                break;//否则就break这个循环
                            }
                        }

                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
