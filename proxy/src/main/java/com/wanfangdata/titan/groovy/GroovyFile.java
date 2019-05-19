package com.wanfangdata.titan.groovy;

/**
 * @ClassName GroovyFile
 * @Author liuwq
 * @Date 2019/5/19 19:02
 * @Version 1.0
 **/
public class GroovyFile {

    private String root;

    private String name;

    private String content;

    public GroovyFile(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
