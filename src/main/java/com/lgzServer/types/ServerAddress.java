package com.lgzServer.types;

public class ServerAddress {
    public static String buildAddressStr(String ip, String port){
        return ip+":"+port;
    }
    public static ServerAddress buildServerAddress(String ip,String port)
    {
        String a= buildAddressStr(ip,port);
        ServerAddress address=new ServerAddress();
        address.serverAddress=a;
        return address;
    };
    public ServerAddress(){

    }
    public String serverAddress;
}
