package com.lgzServer.spring.controller;

import com.lgzServer.spring.mapper.BranchTransactionMapper;
import com.lgzServer.spring.mapper.GlobalTransactionMapper;
import com.lgzServer.types.Result;
import com.lgzServer.types.sql.BranchTransaction;
import com.lgzServer.types.sql.GlobalTransaction;
import com.lgzServer.types.status.BranchStatus;
import com.lgzServer.types.status.GlobalStatus;
import com.lgzServer.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;

@RestController
public class GlobalTransactController {
     @Autowired
     GlobalTransactionMapper globalTransactionMapper;
     @Autowired
    BranchTransactionMapper branchTransactionMapper;
     @GetMapping("/globalTransaction")
     public Result<GlobalTransaction> getItem(@RequestParam("globalId") String globalId){
        return Result.success(globalTransactionMapper.getGlobalTransaction(globalId));
     }
     //添加一个全局事务
     @PostMapping("/globalTransaction/create")
     public Result<GlobalTransaction> beginTransaction(@RequestParam("timeout")Long timeout){
         GlobalTransaction globalTransaction = new GlobalTransaction();
         globalTransaction.setTimeout(timeout);
         globalTransaction.setGlobalId(GlobalTransaction.generateGlobalId());//设置一个global
         globalTransaction.setStatus(GlobalStatus.wait);
         globalTransactionMapper.insertGlobalTransaction(globalTransaction);//
         return Result.success(globalTransaction);
     }
     @GetMapping("/globalTransactions")
     public Result<ArrayList<GlobalTransaction>> getItems(@RequestParam("globalIds")ArrayList<String> globalIds){
        ArrayList<GlobalTransaction> globalTransactions=globalTransactionMapper.getGlobalTransactions(globalIds);
        Long nowTime= TimeUtil.getNowTime();
        for(GlobalTransaction globalTransaction:globalTransactions){
           if(globalTransaction.getStatus()==GlobalStatus.wait){
                Date beginTime= TimeUtil.strToDate(globalTransaction.getBeginTime());
                if(nowTime-beginTime.getTime()>globalTransaction.getTimeout()){//如果已经超时
                    globalTransaction.setStatus(GlobalStatus.fail);
                }else{
                    boolean isSuccess=true;
                    ArrayList<BranchTransaction> lists=branchTransactionMapper.selectBranchTransactionByGlobalId(globalTransaction.getGlobalId());
                    for(BranchTransaction branchTransaction:lists){
                        if(branchTransaction.getStatus().equals(BranchStatus.fail.toString())||branchTransaction.getStatus().equals(BranchStatus.rollback.toString())){
                            isSuccess=false;
                            break;
                        }
                    }
                    if(isSuccess){
                        globalTransaction.setStatus(GlobalStatus.success);
                    }else {
                        globalTransaction.setStatus(GlobalStatus.fail);
                    }
                }
               globalTransactionMapper.updateGlobalTransactionStatusWhenWait(globalTransaction.getGlobalId(),GlobalStatus.success.toString());
               globalTransaction.setStatus(globalTransactionMapper.getGlobalTransaction(globalTransaction.getGlobalId()).getStatus());
           }
       }
        return Result.success(globalTransactionMapper.getGlobalTransactions(globalIds));

     }
     @DeleteMapping("/globalTransaction")
     public void deleteItem(@RequestParam("globalId") String globalId){
         globalTransactionMapper.deleteGlobalTransaction(globalId);
     }

}
