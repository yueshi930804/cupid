//package com.yue.cupid.generator;
//
//import java.net.InetAddress;
//import java.nio.charset.StandardCharsets;
//
///**
// * @author Yue
// * @since 2019/01/15
// */
//public class MachineCodeGenerator4ZK extends MachineCodeGenerator {
//    @Override
//    public void generate() {
//
//    }
//
//    /**
//     * 生成工作者ID
//     *
//     * @param zkServers    zookeeper地址
//     * @param datacenterId 数据中心ID
//     * @param serviceName  服务名称
//     * @return 工作者ID
//     * @throws UnknownHostException 不知道的Host异常
//     */
//    public String generate(String zkServers, int datacenterId, int maxWorkerId, String serviceName) throws UnknownHostException {
//        ZkClient zkClient = new ZkClient(zkServers, 60000, 60000);
//        String rootPath = "/workerId";
//        if (!zkClient.exists(rootPath)) {
//            zkClient.create(rootPath, new byte[0], PERSISTENT);
//        }
//        rootPath = rootPath + "/" + datacenterId;
//        if (!zkClient.exists(rootPath)) {
//            zkClient.create(rootPath, new byte[0], PERSISTENT);
//        }
//        String data = "{service: " + serviceName + ", ip: " + InetAddress.getLocalHost().getHostAddress() + "}";
//        String s = zkClient.create(rootPath + "/", data.getBytes(StandardCharsets.UTF_8), EPHEMERAL_SEQUENTIAL);
//        if (zkClient.getChildren(rootPath).size() > maxWorkerId) {
//            throw new RuntimeException(String.format("Worker can't be greater than %d", maxWorkerId));
//        }
//        int workerId = Integer.parseInt(s.replace(rootPath + "/", ""));
//        return (workerId % maxWorkerId) + 1;
//    }
//}
