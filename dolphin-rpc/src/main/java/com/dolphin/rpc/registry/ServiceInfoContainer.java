package com.dolphin.rpc.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dolphin.rpc.core.io.HostAddress;
import com.dolphin.rpc.core.io.transport.codec.ProtobufferCodec;

public class ServiceInfoContainer {

    private transient static Logger                  logger              = Logger
        .getLogger(ServiceInfoContainer.class);

    private Map<String, Map<String, ServiceInfoSet>> groupedServiceInfos = new ConcurrentHashMap<>();

    public ServiceInfoContainer() {
    }

    /**
     * 往分组中加入一个Service的信息
     * @author jiujie
     * 2016年5月31日 下午3:07:18
     * @param group
     * @param serviceInfo
     */
    public void add(ServiceInfo serviceInfo) {
        if (serviceInfo == null || StringUtils.isBlank(serviceInfo.getGroup())
            || StringUtils.isBlank(serviceInfo.getName())) {
            return;
        }
        get(serviceInfo.getGroup(), serviceInfo.getName()).add(serviceInfo);
        //        logger.info("Add Service [" + serviceInfo.toString() + "] success.");
    }

    /**
     * 通过Group和ServiceName获取ServiceInfoSet
     * @author jiujie
     * 2016年5月31日 下午3:08:28
     * @param group
     * @param serviceName
     * @return
     */
    public ServiceInfoSet get(String group, String serviceName) {
        Map<String, ServiceInfoSet> groupServiceInfos = groupedServiceInfos.get(group);
        ServiceInfoSet serviceInfos = null;
        if (groupServiceInfos == null) {
            synchronized (groupedServiceInfos) {
                if (groupServiceInfos == null) {
                    groupServiceInfos = new ConcurrentHashMap<>();
                    serviceInfos = new ServiceInfoSet();
                    groupServiceInfos.put(serviceName, serviceInfos);
                    groupedServiceInfos.put(group, groupServiceInfos);
                }
            }
        } else {
            serviceInfos = groupServiceInfos.get(serviceName);
        }
        return serviceInfos;
    }

    /**
     * 移除一个分组中的ServiceInfo
     * @author jiujie
     * 2016年5月31日 下午3:13:02
     * @param group
     * @param serviceInfo
     */
    public void remove(ServiceInfo serviceInfo) {
        if (serviceInfo == null || StringUtils.isBlank(serviceInfo.getGroup())
            || StringUtils.isBlank(serviceInfo.getName())) {
            return;
        }
        get(serviceInfo.getGroup(), serviceInfo.getName()).remove(serviceInfo);
        //        logger.info("Remove Service [" + serviceInfo.toString() + "] success.");
    }

    public static void main(String[] args) {
        ServiceInfo serviceInfo1 = new ServiceInfo("test", "orderService",
            new HostAddress("10.1.1.31", 2222));
        ServiceInfo serviceInfo2 = new ServiceInfo("test", "orderService",
            new HostAddress("10.1.1.31", 2222));
        ServiceInfoContainer serviceInfoContaintor = new ServiceInfoContainer();
        serviceInfoContaintor.add(serviceInfo1);
        serviceInfoContaintor.add(serviceInfo2);
        System.out.println(serviceInfo1.hashCode());
        System.out.println(serviceInfo2.hashCode());
        //        serviceInfoContaintor.remove(serviceInfo1);
        System.out.println(serviceInfo1.hashCode());
        ProtobufferCodec protobufferCodec = new ProtobufferCodec();
        byte[] encode = protobufferCodec.encode(serviceInfoContaintor.getAll());
        List<ServiceInfo> decode = protobufferCodec.decode(encode, ArrayList.class);
        System.out.println(decode.size());
    }

    /**
     * ServiceInfo的Set
     * @author jiujie
     * @version $Id: ServiceInfoContaintor.java, v 0.1 2016年5月31日 下午3:59:25 jiujie Exp $
     */
    public class ServiceInfoSet implements Set<ServiceInfo> {

        private Map<String, ServiceInfo> map = new HashMap<>();

        public ServiceInfoSet() {

        }

        private String getUniqueKey(ServiceInfo serviceInfo) {
            HostAddress hostAddress = serviceInfo.getHostAddress();
            return hostAddress.getHost() + hostAddress.getPort();
        }

        private ServiceInfo castServiceInfo(Object object) {
            return (ServiceInfo) object;
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return map.containsKey(o);
        }

        @Override
        public Iterator<ServiceInfo> iterator() {
            return map.values().iterator();
        }

        @Override
        public Object[] toArray() {
            return map.values().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return map.values().toArray(a);
        }

        @Override
        public boolean add(ServiceInfo e) {
            return map.put(getUniqueKey(e), e) != null;
        }

        @Override
        public boolean remove(Object o) {
            boolean result = map.remove(getUniqueKey(castServiceInfo(o))) != null;
            return result;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return map.values().containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends ServiceInfo> c) {
            return map.values().addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return map.values().retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return map.values().removeAll(c);
        }

        @Override
        public void clear() {
            map.clear();
        }

    }

    public void addAll(List<ServiceInfo> serviceInfos) {
        if (serviceInfos == null || serviceInfos.isEmpty()) {
            return;
        }
        for (ServiceInfo serviceInfo : serviceInfos) {
            add(serviceInfo);
        }
    }

    public List<ServiceInfo> getAll() {
        List<ServiceInfo> serviceInfos = new ArrayList<>();
        Collection<Map<String, ServiceInfoSet>> values = groupedServiceInfos.values();
        if (values != null) {
            for (Map<String, ServiceInfoSet> value : values) {
                Collection<ServiceInfoSet> serviceInfoSets = value.values();
                if (serviceInfoSets == null || serviceInfoSets.isEmpty()) {
                    continue;
                }
                for (ServiceInfoSet serviceInfoSet : serviceInfoSets) {
                    if (serviceInfoSet == null || serviceInfoSet.isEmpty()) {
                        continue;
                    }
                    serviceInfos.addAll(serviceInfoSet);
                }
            }
        }
        return serviceInfos;
    }

}
