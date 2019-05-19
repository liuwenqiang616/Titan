package com.wanfangdata.titan.common;

import com.dianping.cat.Cat;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public class CatContext implements Cat.Context {
	private Map<String, String> properties = new HashMap<String, String>();

	@Override
	public void addProperty(String key, String value) {
		this.properties.put(key, value);
	}

	@Override
	public String getProperty(String key) {
		return this.properties.get(key);
	}

}
