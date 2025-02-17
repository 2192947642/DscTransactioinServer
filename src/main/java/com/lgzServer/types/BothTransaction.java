package com.lgzServer.types;

import com.lgzServer.types.sql.BranchTransaction;
import com.lgzServer.types.sql.GlobalTransaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BothTransaction {
    BranchTransaction branchTransaction;
    GlobalTransaction globalTransaction;
}
