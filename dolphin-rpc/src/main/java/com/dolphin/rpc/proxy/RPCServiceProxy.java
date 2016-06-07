package com.dolphin.rpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.dolphin.rpc.core.annotation.RPCService;
import com.dolphin.rpc.core.io.Connection;
import com.dolphin.rpc.core.io.request.RequestManager;
import com.dolphin.rpc.core.io.transport.Header;
import com.dolphin.rpc.core.io.transport.PacketType;
import com.dolphin.rpc.core.io.transport.RPCRequest;
import com.dolphin.rpc.core.io.transport.RPCResult;

public class RPCServiceProxy implements InvocationHandler {

    private static Logger         logger         = Logger.getLogger(RPCServiceProxy.class);

    private static RequestManager requestManager = RequestManager.getInstance();

    /** 客户端选择器 @author jiujie 2016年5月24日 上午11:33:08 */
    private ConnectionSelector    clientSelector;

    public RPCServiceProxy() {
        this.clientSelector = ServiceConnectionSelector.getInstance();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> clazz = method.getDeclaringClass();
        RPCService annotation = clazz.getAnnotation(RPCService.class);
        String serviceName = annotation.value();
        String group = annotation.group();

        String className = clazz.getName();
        if (logger.isDebugEnabled()) {
            logger.debug("Service [" + className + "] invoke starting");
        }
        RPCRequest request = new RPCRequest();
        request.setClassName(className);
        request.setMethodName(method.getName());
        request.setParamters(args);
        request.setParamterTypes(method.getParameterTypes());
        Connection connection = clientSelector.select(group, serviceName);
        RPCResult result = (RPCResult) requestManager.sysnRequest(connection,
            new Header(PacketType.RPC), request);
        if (result.getException() != null) {
            throw result.getException();
        }
        return result.getResult();
    }

}
