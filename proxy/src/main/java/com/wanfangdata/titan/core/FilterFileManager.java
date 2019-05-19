package com.wanfangdata.titan.core;

import com.netflix.config.ConfigurationManager;
import com.wanfangdata.titan.common.Constants;
import com.wanfangdata.titan.common.FileFilter;
import com.wanfangdata.titan.filters.FilterLoader;
import com.wanfangdata.titan.groovy.GroovyFile;
import com.wanfangdata.titan.utils.ZookeeperUtil;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 监听zookeeper titan节点的变化 动态的更新过滤器列表
 *
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public class FilterFileManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterFileManager.class);

    private static String TITAN_NODE_NAME;

    protected static FilterFileManager instance = null;

    private String[] aDirectories;

    private static FileFilter FILENAME_FILTER;

    protected FilterFileManager() {
    }

    public static FilterFileManager getInstance() {

        if (instance == null) {
            synchronized (FilterFileManager.class) {
                if (instance == null) {
                    instance = new FilterFileManager();
                }
            }
        }
        return instance;
    }

    public static void setFilenameFilter(FileFilter filter) {
        FILENAME_FILTER = filter;
    }

    /**
     * 初始化 过滤器文件管理者
     *
     * @param directories
     * @throws IOException
     */
    public static void init(String titanNodeName, String... directories) throws Exception {
        TITAN_NODE_NAME = titanNodeName;
        getInstance();
        instance.aDirectories = directories;
        instance.manageFiles();
    }

    /**
     * 返回一组从titan节点下获取到的filters文件列表
     *
     * @return
     */
    private List<GroovyFile> getFiles() throws Exception {
        List<GroovyFile> list = new ArrayList<>();
        for (String sDirectory : aDirectories) {
            CuratorFramework curator = ZookeeperUtil.getCurator(TITAN_NODE_NAME);
            boolean exists = checkExists(sDirectory);
            if (!exists) {
                continue;
            }
            List<String> nodes = curator.getChildren().forPath(sDirectory);
            for (String configName : nodes) {
                boolean accept = FILENAME_FILTER.accept(configName);
                if (!accept) {
                    continue;
                }
                GroovyFile file = getFile(sDirectory,configName, curator.getData().forPath(sDirectory + "/" + configName));
                if (file == null) {
                    continue;
                }
                list.add(file);
            }
        }
        return list;
    }

    /**
     * 校验节点是否存在
     *
     * @param path
     * @return
     */
    private boolean checkExists(String path) throws Exception {
        if (path == null) {
            return false;
        }
        CuratorFramework curator = ZookeeperUtil.getCurator(TITAN_NODE_NAME);
        Stat stat = curator.checkExists().forPath(path);
        if (stat == null) {
            return false;
        }
        return true;
    }

    private GroovyFile getFile(String sDirectory,String configName, byte[] configNode) throws IOException {
        if (configNode == null) {
            return null;
        }
        String content = new String(configNode);
        return createGroovyFile(sDirectory,configName, content);
    }

    private GroovyFile createGroovyFile(String sDirectory,String fileName, String fileContent) {
        GroovyFile groovyFile = new GroovyFile(fileName, fileContent);
        groovyFile.setRoot(sDirectory);
        return groovyFile;
    }

    /**
     * 将文件放入FilterLoader。 FilterLoader 只会添加新的或已更改的过滤器
     *
     * @param aFiles a List<File>
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private void processGroovyFiles(List<GroovyFile> aFiles) throws Exception {
        for (GroovyFile file : aFiles) {
            FilterLoader.getInstance().putFilter(file);
        }
    }

    protected void manageFiles() throws Exception {
        List<GroovyFile> aFiles = getFiles();
        processGroovyFiles(aFiles);
    }
}

