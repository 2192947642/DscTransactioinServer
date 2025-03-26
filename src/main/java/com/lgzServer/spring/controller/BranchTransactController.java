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
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
public class BranchTransactController {
    @Autowired
    BranchTransactionMapper branchTransactionMapper;
    @Autowired
    GlobalTransactionMapper globalTransactionMapper;
    @Autowired
    TransactionTemplate  transactionTemplate;
    public  void updateBranchStatusCommon(BranchTransaction branchTransaction) {
        if (branchTransaction.getStatus() == BranchStatus.success) {
            //提交成功只能是 wait状态
            branchTransactionMapper.updateStatusWhenPre(branchTransaction.getBranchId(), branchTransaction.getStatus().name(),BranchStatus.wait.name());
        }else if(branchTransaction.getStatus()==BranchStatus.rollback){
            //rollback可以是wait状态或者success状态 也就是除commit之外
            branchTransactionMapper.updateStatusNotPre(branchTransaction.getBranchId(), branchTransaction.getStatus().name(),BranchStatus.commit.name());
        }else if(branchTransaction.getStatus()==BranchStatus.commit){//提交状态 只能来自于success
            branchTransactionMapper.updateStatusWhenPre(branchTransaction.getBranchId(), branchTransaction.getStatus().name(), BranchStatus.success.name());
        }
    }

    @PutMapping("/branchTransaction/status/notice")
    public void noticeBranchTransaction(@RequestBody BranchTransaction branchTransaction) {
        updateBranchStatusCommon(branchTransaction);
        GlobalNotice globalNotice = new GlobalNotice();
        ArrayList<BranchTransaction> lists = branchTransactionMapper.selectBranchTransactionByGlobalId(branchTransaction.getGlobalId());

        transactionTemplate.execute(status -> {
            GlobalTransaction globalTransaction = globalTransactionMapper.getGlobalTransactionForUpdate(branchTransaction.getGlobalId());
            globalNotice.setGlobalId(branchTransaction.getGlobalId());
            if (globalTransaction.getStatus() == GlobalStatus.wait) {
                if (branchTransaction.getStatus() == BranchStatus.success) {//如果是执行成功了
                    boolean isWait = false;
                    boolean isFail = false;
                    for (BranchTransaction branchTransaction1 : lists) {
                        if (branchTransaction1.getStatus() == BranchStatus.wait) {
                            isWait = true;
                        }
                        if (branchTransaction1.getStatus() == BranchStatus.rollback) {
                            isFail = true;
                        }
                    }
                    if (isFail) {
                        globalTransactionMapper.updateGlobalTransactionStatusWhenWait(branchTransaction.getGlobalId(), GlobalStatus.fail.toString());
                        globalNotice.setIsSuccess(false);
                    }else if(isWait){
                        return null;
                    }else{
                        globalTransactionMapper.updateGlobalTransactionStatusWhenWait(branchTransaction.getGlobalId(), GlobalStatus.success.toString());
                        globalNotice.setIsSuccess(true);
                    }
                } else if (branchTransaction.getStatus() == BranchStatus.rollback) {
                    //更新全局事务状态为失败
                    globalTransactionMapper.updateGlobalTransaction(branchTransaction.getGlobalId(), GlobalStatus.fail.toString());
                    globalNotice.setIsSuccess(false);
                }
            } else if (globalTransaction.getStatus() == GlobalStatus.fail) {
                globalNotice.setIsSuccess(false);
            } else if (globalTransaction.getStatus() == GlobalStatus.success) {
                globalNotice.setIsSuccess(true);
            }
            return null;
        });
        //对所有的分布式下属服务中未进行提交或者回滚的服务进行通知
        for (BranchTransaction branchTransaction1 : lists) {
            //如果是已经提交或者回滚的服务则跳过
            if (branchTransaction1.getStatus() == BranchStatus.commit || branchTransaction1.getStatus() == BranchStatus.rollback) {
                continue;
            }
            globalNotice.setBranchId(branchTransaction1.getBranchId());
            Message message = new Message(MessageTypeEnum.GlobalNotice, JsonUtil.objToJson(globalNotice), TimeUtil.getLocalTime());
            ReceiveContext<Message> receiveContext = new ReceiveContext<>(branchTransaction1.getServerAddress(), message);
            ClientChannelManager.instance.sendMessage(receiveContext);
        }
    }

    //插入分支事务
    @PostMapping("/branchTransaction")
    public Result<BranchTransaction> joinBranchTransaction(@RequestBody BranchTransaction branchTransaction) {
        if (branchTransaction.getBranchId() == null) {
            branchTransaction.setBranchId(BranchTransaction.generateBranchId());
        }
        branchTransactionMapper.insertBranchTransaction(branchTransaction);
        return Result.success(branchTransaction);
    }

    @PutMapping("/branchTransaction/status")
    public void updateBranchStatus(@RequestBody BranchTransaction branchTransaction) {
        updateBranchStatusCommon(branchTransaction);
    }

    @GetMapping("/branchTransaction")
    public Result<BranchTransaction> getBranchTransaction(@RequestParam("branchId") String branchId) {
        return Result.success(branchTransactionMapper.selectBranchTransaction(branchId));
    }

    @DeleteMapping("/branchTransaction")
    public void deleteBranchTransaction(@RequestParam String branchId) {
        branchTransactionMapper.deleteBranchTransaction(branchId);
    }
}
