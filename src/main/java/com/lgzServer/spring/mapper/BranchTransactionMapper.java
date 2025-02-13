package com.lgzServer.spring.mapper;

import com.lgzServer.types.sql.BranchTransaction;
import org.apache.ibatis.annotations.*;

import java.util.ArrayList;

@Mapper
public interface BranchTransactionMapper {
    @Update("update branch_transaction set status=#{status} where branch_id=#{branchId} and status='wait'")
    void updateStatusWhenWait(@Param("branchId") String branchId,@Param("status") String status);
    @Select("select * from branch_transaction where global_id=#{globalId}")
    ArrayList<BranchTransaction> selectBranchTransactionByGlobalId(String globalId);
    @Select("select * from branch_transaction where global_id in #{globalIds}")
    ArrayList<BranchTransaction> selectBranchTransactionByGlobalIds(ArrayList<String> globalIds);
    @Insert("insert into branch_transaction(global_id,branch_id,application_name,server_address,status,begin_time) values (#{globalId},#{branchId},#{applicationName},#{serverAddress},#{status},#{beginTime})")
    void insertBranchTransaction(BranchTransaction branchTransaction);
    @Delete("delete from branch_transaction where branch_id=#{branchId}")
    void deleteBranchTransaction(String branchId);
    @Delete("delete from branch_transaction where global_id=#{globalId}")
    void deleteBranchTransactionByGlobalId(String globalId);
    @Update("update branch_transaction set status=#{status} where branch_id=#{branchId}")
    void updateStatus(@Param("branchId") String branchId,@Param("status") String status);
    @Select("select * from branch_transaction where branch_id=#{branchId}")
    BranchTransaction selectBranchTransaction(String branchId);

}
