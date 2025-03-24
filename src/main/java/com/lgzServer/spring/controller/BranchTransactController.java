package com.lgzServer.spring.controller;

import com.lgzServer.netty.handlers.ClientChannelManager;
import com.lgzServer.spring.mapper.BranchTransactionMapper;
import com.lgzServer.spring.mapper.GlobalTransactionMapper;
import com.lgzServer.types.*;
import com.lgzServer.types.sql.BranchTransaction;
import com.lgzServer.types.sql.GlobalTransaction;
import com.lgzServer.types.status.GlobalStatus;
import com.lgzServer.types.status.BranchStatus;
import com.lgzServer.types.status.MessageTypeEnum;
import com.lgzServer.utils.JsonUtil;
import com.lgzServer.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
public class BranchTransactController {
     @Autowired
     BranchTransactionMapper branchTransactionMapper;
     @Autowired
     GlobalTransactionMapper globalTransactionMapper;
     @PutMapping("/branchTransaction/status/notice")
     public void noticeBranchTransaction(@RequestBody BranchTransaction branchTransaction){
         if(branchTransaction.getStatus()== BranchStatus.success){
             branchTransactionMapper.updateStatusWhenWait(branchTransaction.getBranchId(),branchTransaction.getStatus().toString());
         }
         else{
             branchTransactionMapper.updateStatus(branchTransaction.getBranchId(),branchTransaction.getStatus().toString());
         }
         ArrayList<BranchTransaction> lists=branchTransactionMapper.selectBranchTransactionByGlobalId(branchTransaction.getGlobalId());
         GlobalTransaction globalTransaction=globalTransactionMapper.getGlobalTransaction(branchTransaction.getGlobalId());
         GlobalNotice globalNotice=new GlobalNotice();
         globalNotice.setGlobalId(branchTransaction.getGlobalId());
         if(globalTransaction.getStatus()==GlobalStatus.wait){
             if(branchTransaction.getStatus() == BranchStatus.success){//如果是执行成功了
                     boolean isWait=false;
                     boolean isFail=false;
                     for(BranchTransaction branchTransaction1:lists){
                         if(branchTransaction1.getStatus()== BranchStatus.wait){
                             isWait=true;
                         }
                         if(branchTransaction1.getStatus() == BranchStatus.rollback){
                             isFail=true;
                         }
                     }
                     if(isFail){
                         globalTransactionMapper.updateGlobalTransactionStatusWhenWait(branchTransaction.getGlobalId(),GlobalStatus.fail.toString());
                     }else if(!isWait&&!isWait){
                         globalTransactionMapper.updateGlobalTransactionStatusWhenWait(branchTransaction.getGlobalId(),GlobalStatus.success.toString());
                     }else if(!isFail&&isWait){
                         return;//如果当前分布式事务中还有任务没有执行完成 那么就return
                     }
                     GlobalStatus globalStatus=globalTransactionMapper.getGlobalTransaction(branchTransaction.getGlobalId()).getStatus();
                     if(globalStatus==GlobalStatus.success){
                         globalNotice.setIsSuccess(true);
                     }else{
                         globalNotice.setIsSuccess(false);
                     }
             }
             else if(branchTransaction.getStatus()== BranchStatus.rollback){
                 globalNotice.setIsSuccess(false);
                 //更新全局事务状态为失败
                 globalTransactionMapper.updateGlobalTransaction(branchTransaction.getGlobalId(),GlobalStatus.fail.toString());
             }
         }
         else if(globalTransaction.getStatus()==GlobalStatus.fail){
             globalNotice.setIsSuccess(false);
         }
         else if(globalTransaction.getStatus()==GlobalStatus.success){
             globalNotice.setIsSuccess(true);
         }
         //对所有的分布式下属服务中未进行提交或者回滚的服务进行通知
         for(BranchTransaction branchTransaction1:lists){
             //如果是已经提交或者回滚的服务则跳过
             if(branchTransaction1.getStatus()== BranchStatus.commit||branchTransaction1.getStatus()== BranchStatus.rollback){
                 continue;
             }
             globalNotice.setBranchId(branchTransaction1.getBranchId());
             Message message=new Message(MessageTypeEnum.GlobalNotice, JsonUtil.objToJson(globalNotice), TimeUtil.getLocalTime());
             ReceiveContext<Message> receiveContext= new ReceiveContext<>(branchTransaction1.getServerAddress(), message);
             ClientChannelManager.instance.sendMessage(receiveContext);
         }
     }

    //插入分支事务
    @PostMapping("/branchTransaction")
    public Result<BranchTransaction> joinBranchTransaction(@RequestBody BranchTransaction branchTransaction){
        if(branchTransaction.getBranchId() ==null){
            branchTransaction.setBranchId(BranchTransaction.generateBranchId());
        }
        branchTransactionMapper.insertBranchTransaction(branchTransaction);
        return Result.success(branchTransaction);
    }
    @PutMapping("/branchTransaction/status")
    public void updateBranchStatus(@RequestBody BranchTransaction branchTransaction){
        if(branchTransaction.getStatus()== BranchStatus.success){
            branchTransactionMapper.updateStatusWhenWait(branchTransaction.getBranchId(),branchTransaction.getStatus().toString());
        }
        else branchTransactionMapper.updateStatus(branchTransaction.getBranchId(),branchTransaction.getStatus().toString());

    }
    @GetMapping("/branchTransaction")
    public Result<BranchTransaction> getBranchTransaction(@RequestParam("branchId") String branchId){
        return Result.success(branchTransactionMapper.selectBranchTransaction(branchId));
    }
    @DeleteMapping("/branchTransaction")
    public void deleteBranchTransaction(@RequestParam String branchId){
        branchTransactionMapper.deleteBranchTransaction(branchId);
    }
}
