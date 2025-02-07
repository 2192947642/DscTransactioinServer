package com.lgzServer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

public class AddressUtil {
    public static String buildAddress(String ip, String port) throws UnknownHostException {
        if(ip.equals("auto")) ip=getIp();
        return ip+":"+port;
    }
    private static Integer port=null;//当前服务的端口 //在start时进行设置
    public static String getIp() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }
    public static String getExternalIP() {
        try {
            URL url = new URL("http://icanhazip.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
