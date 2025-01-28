package com.lgzServer.types;

import com.lgzServer.types.status.GlobalStatus;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;
@AllArgsConstructor
@NoArgsConstructor
public class GlobalType {

   public GlobalStatus status;
   public Map<String,LocalType> localTypeMap;
}
