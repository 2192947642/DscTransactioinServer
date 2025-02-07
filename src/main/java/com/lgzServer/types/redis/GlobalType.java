package com.lgzServer.types.redis;

import com.lgzServer.types.sql.BranchTransaction;
import com.lgzServer.types.status.GlobalStatus;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;
@AllArgsConstructor
@NoArgsConstructor
public class GlobalType {

   private GlobalStatus status;
   private Map<String, BranchTransaction>branchTransactionMap;
}
