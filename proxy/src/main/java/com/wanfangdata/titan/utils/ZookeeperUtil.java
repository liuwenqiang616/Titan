package com.wanfangdata.titan.utils;

import com.wanfangdata.zookeepertools.ZookeeperConfig;
import com.wanfangdata.zookeepertools.ZookeeperConfigHelper;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

/**
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public class ZookeeperUtil {
    private static final Logger log = LoggerFactory.getLogger(ZookeeperUtil.class);

    private static HashMap<String, CuratorFramework> curators = new HashMap<>();

    /**
     * 初始化ZooKeeper客户端
     *
     * @return Zookerper客户端
     */
    public static CuratorFramework getCurator(String configName) {

        if (curators.containsKey(configName)) {
            return curators.get(configName);
        }

        log.debug("初始化Zookeeper客户端, configName: " + configName);
        ZookeeperConfig config = ZookeeperConfigHelper.getInstance().get(configName);
        RetryPolicy policy = new ExponentialBackoffRetry(config.getRetryTimeout(), config.getRetryNumber());
        ACLProvider aclProvider = new ACLProvider() {
            @Override
            public List<ACL> getDefaultAcl() {
                return ZooDefs.Ids.READ_ACL_UNSAFE;
            }

            @Override
            public List<ACL> getAclForPath(String s) {
                return ZooDefs.Ids.READ_ACL_UNSAFE;
            }
        };

        CuratorFramework curator = CuratorFrameworkFactory.builder().connectString(config.connectionString)
                .sessionTimeoutMs(config.getWaitTimeout()).retryPolicy(policy)
                .aclProvider(aclProvider).authorization(config.getAuthSchema(), config.getAuthContent().getBytes()).build();
        curator.start();
        curators.put(configName, curator);
        return curator;
    }
}
