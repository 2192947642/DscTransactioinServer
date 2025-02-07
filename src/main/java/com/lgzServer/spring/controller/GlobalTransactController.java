package com.lgzServer.spring.controller;

import com.lgzServer.spring.mapper.GlobalTransactionMapper;
import com.lgzServer.types.Result;
import com.lgzServer.types.sql.GlobalTransaction;
import com.lgzServer.types.status.GlobalStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class GlobalTransactController {
     @Autowired
     GlobalTransactionMapper globalTransactionMapper;
     @GetMapping("/globalTransaction")
     public Result<GlobalTransaction> getItem(@RequestParam("globalId") String globalId){
        return Result.success(globalTransactionMapper.getGlobalTransaction(globalId));
     }
     //添加一个全局事务
     @PostMapping("/globalTransaction/create")
     public Result<GlobalTransaction> beginTransaction(){
         GlobalTransaction globalTransaction = new GlobalTransaction();
         globalTransaction.setGlobalId(GlobalTransaction.generateGlobalId());//设置一个global
         globalTransaction.setStatus(GlobalStatus.wait);
         globalTransactionMapper.insertGlobalTransaction(globalTransaction);//
         return Result.success(globalTransaction);
     }
     @GetMapping("/globalTransactions")
     public Result<ArrayList<GlobalTransaction>> getItems(@RequestParam("globalIds")ArrayList<String> globalIds){
        return Result.success(globalTransactionMapper.getGlobalTransactions(globalIds));
     }

}
