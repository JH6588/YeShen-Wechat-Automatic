package com.hui.demo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import redis.clients.jedis.Jedis;

public class PropertiesUtil {

	private static Properties _prop = new Properties();

	/**
	 * 读取配置文件
	 * 
	 * @param fileName
	 */
	public static void readProperties(String fileName) {
		try {
			// InputStream in = PropertiesUtil.class.getResourceAsStream("/"+fileName);
			InputStream in = new FileInputStream(fileName);
			BufferedReader bf = new BufferedReader(new InputStreamReader(in));
			_prop.load(bf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据key读取对应的value
	 * 
	 * @param key
	 * @return
	 */
	public static String getProperty(String key) {
		return _prop.getProperty(key);
	}

	public static int getIntProperty(String key) {
		return Integer.parseInt(_prop.getProperty(key));
	}

}