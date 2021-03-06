package com.dolphin.rpc.registry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.dolphin.rpc.core.config.RegistryConfig;
import com.dolphin.rpc.core.exception.AddressExitsException;
import com.dolphin.rpc.core.exception.AddressFormatException;
import com.dolphin.rpc.core.io.HostAddress;
import com.dolphin.rpc.registry.server.RegistryAddressContainer;

public class MySQLRegistryAddressContainer implements RegistryAddressContainer {

    private static RegistryAddressContainer registryAddressContainer = new MySQLRegistryAddressContainer();

    private String                          url;

    private String                          username;

    private String                          password;

    // 加载驱动
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("驱动加载出错!");
        }
    }

    private MySQLRegistryAddressContainer() {
        RegistryConfig registryConfig = new RegistryConfig();
        url = registryConfig.getDbUrl();
        username = registryConfig.getDbUsername();
        password = registryConfig.getDbPassword();
    }

    public static RegistryAddressContainer getInstance() {
        return registryAddressContainer;
    }

    @Override
    public List<HostAddress> getAll() {
        List<HostAddress> addresses = new ArrayList<>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            st = conn.createStatement();
            String sql;
            sql = "SELECT host,port FROM registry_server_address";
            rs = st.executeQuery(sql);
            while (rs.next()) {
                HostAddress hostAddress = new HostAddress(rs.getString(1), rs.getInt(2));
                addresses.add(hostAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            free(rs, st, conn);
        }

        return addresses;

    }

    private boolean exist(HostAddress address) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "SELECT host,port FROM registry_server_address where host=? and port=?";
            st = conn.prepareStatement(sql);
            st.setString(1, address.getHost());
            st.setInt(2, address.getPort());
            rs = st.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            free(rs, st, conn);
        }
        return false;
    }

    @Override
    public void add(HostAddress address) {
        if (!HostAddress.verify(address)) {
            throw new AddressFormatException();
        }
        if (exist(address)) {
            throw new AddressExitsException();
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = getConnection();
            String sql;
            sql = "insert into registry_server_address (host,port) values (?,?)";
            st = conn.prepareStatement(sql);
            st.setString(1, address.getHost());
            st.setInt(2, address.getPort());
            st.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            free(null, st, conn);
        }
    }

    @Override
    public void remove(HostAddress address) {
        if (!HostAddress.verify(address)) {
            throw new AddressFormatException();
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = getConnection();
            String sql;
            sql = "delete from registry_server_address where host=? and port=?";
            st = conn.prepareStatement(sql);
            st.setString(1, address.getHost());
            st.setInt(2, address.getPort());
            st.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            free(null, st, conn);
        }
    }

    // 获得连接
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    // 释放连接
    public static void free(ResultSet rs, Statement st, Connection conn) {
        try {
            if (rs != null) {
                rs.close(); // 关闭结果集
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (st != null) {
                    st.close(); // 关闭Statement
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (conn != null) {
                        conn.close(); // 关闭连接
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

        }

    }

    public static void main(String[] args) {
        RegistryAddressContainer instance = MySQLRegistryAddressContainer.getInstance();
        System.out.println();
    }

}
