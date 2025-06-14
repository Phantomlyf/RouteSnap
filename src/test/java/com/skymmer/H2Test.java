package com.skymmer;

import java.sql.Connection;
import java.sql.DriverManager;

public class H2Test {
    public static void main(String[] args) throws Exception {
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:file:~/userdb;AUTO_SERVER=TRUE", "sa", "");
        System.out.println("连接成功！");
    }
}
