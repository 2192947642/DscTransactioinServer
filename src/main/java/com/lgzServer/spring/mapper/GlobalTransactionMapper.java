package com.lgzServer.spring.mapper;

import com.lgzServer.types.sql.GlobalTransaction;
import org.apache.ibatis.annotations.*;

import java.util.ArrayList;

@Mapper
public interface GlobalTransactionMapper {
    @Insert("insert into global_transaction(global_id,status) values(#{globalId},#{status})")
    int insertGlobalTransaction(GlobalTransaction globalTransaction);
    @Update("update global_transaction set status=#{status} where global_id=#{globalId}")
    int updateGlobalTransaction(@Param("globalId") String globalId, @Param("status") String status);
    @Delete("delete from global_transaction where global_id=#{globalId}")
    int deleteGlobalTransaction(String globalId);
    @Select("select * from global_transaction where global_id=#{globalId}")
    GlobalTransaction getGlobalTransaction(String globalId);
    @Select("select * from global_transaction where global_id in ${globalIds}")
    ArrayList<GlobalTransaction> getGlobalTransactions(ArrayList<String> globalIds);
}
