package com.dolphin.rpc.registry.provider;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.dolphin.rpc.core.ApplicationType;
import com.dolphin.rpc.core.io.Connection;
import com.dolphin.rpc.core.io.Connector;
import com.dolphin.rpc.core.io.HostAddress;
import com.dolphin.rpc.core.io.transport.Header;
import com.dolphin.rpc.core.io.transport.Message;
import com.dolphin.rpc.core.io.transport.PacketType;
import com.dolphin.rpc.netty.connector.NettyConnector;
import com.dolphin.rpc.registry.MySQLRegistryAddressContainer;
import com.dolphin.rpc.registry.ServiceInfo;
import com.dolphin.rpc.registry.protocle.Commands;
import com.dolphin.rpc.registry.protocle.RegistryRequest;

public class NettyServiceProvider extends NettyConnector implements ServiceProvider, Connector {

    private Logger     logger = Logger.getLogger(NettyServiceProvider.class);

    private Connection connection;

    public NettyServiceProvider() {
        //TODO 注册中心地址 
        List<HostAddress> all = MySQLRegistryAddressContainer.getInstance().getAll();
        HostAddress masterRegistryAddress = all.get(new Random().nextInt(all.size()));
        logger.info("Connecting registry server [" + masterRegistryAddress.toString() + "]");
        connection = connect(masterRegistryAddress);
        //TODO 要起个线程，自动重连，防止注册中心挂了

    }

    @Override
    public void register(ServiceInfo serviceInfo) {
        //TODO 获取自己的地址去注册
        RegistryRequest registryRequest = new RegistryRequest(ApplicationType.RPC_SERVER,
            Commands.REGISTER, serviceInfo);
        connection.writeAndFlush(new Message(new Header(PacketType.REGISTRY), registryRequest));
    }

    @Override
    public void unRegister(ServiceInfo serviceInfo) {
        RegistryRequest registryRequest = new RegistryRequest(ApplicationType.RPC_SERVER,
            Commands.UN_REGISTER, serviceInfo);
        connection.writeAndFlush(new Message(new Header(PacketType.REGISTRY), registryRequest));
    }

}
