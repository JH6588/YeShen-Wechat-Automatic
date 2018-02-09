package com.hui.demo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.sikuli.script.Location;

import redis.clients.jedis.Jedis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sikuli.script.FindFailed;

public class Round {
	static {

		PropertiesUtil.readProperties(".//conf//my.properties");

		// Runtime.getRuntime().addShutdownHook(new ExitHandler());
	}
	// private static final String filename = ".//conf//acct.txt";
	// private static BufferedReader source = getAccountSource();

	private static String redisPassword = PropertiesUtil.getProperty("redis_password");
	private static String redisConnection = PropertiesUtil.getProperty("redis_host");
	private static Log log = LogFactory.getLog(Round.class);
	private static List<Map<String, Integer>> positionInfos;
	static String chatMessage = PropertiesUtil.getProperty("chat_message");
	static int initPositionX = PropertiesUtil.getIntProperty("init_x");
	static int initPositionY = PropertiesUtil.getIntProperty("init_y");
	static String[] messages = PropertiesUtil.getProperty("message").split("---");
	static String[] addNumber = PropertiesUtil.getProperty("add_number").split("---");
	static String[] roundTimes = PropertiesUtil.getProperty("round_sleep").split("---");
	static String[] exceptionTimes = PropertiesUtil.getProperty("exception_sleep").split("---");
	// public static class ExitHandler extends Thread {
	// public ExitHandler() {
	// super("Exit Handler");
	// }
	//
	// public void run() {
	//
	// try {
	// saveFile(source, filename);
	// source.close();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// log.info("退出程序");
	// System.out.println("Set exit");
	// }
	// }

	private static int currentTime() {
		return (int) (System.currentTimeMillis() / 1000);
	}

	public static void saveFile(BufferedReader breader, String saveTxt) throws IOException {

		FileWriter writer = new FileWriter(saveTxt); // 打开已存在的文件 直接将其清空
		String temp;
		while ((temp = breader.readLine()) != null) {
			writer.write(temp + "\n");
		}
		writer.close();
	}

	// private static BufferedReader getAccountSource() {
	// try {
	// return new BufferedReader(new FileReader(filename));
	// } catch (FileNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// return null;
	// }
	// }
	private static Jedis getRedisConnection() throws InterruptedException {
		Jedis jedis;
		while (true) {
			try {
				jedis = new Jedis(redisConnection, 6379, 3000);
				jedis.auth(redisPassword);
				return jedis;
			} catch (Exception e) {
				System.out.println("redis连接错误：" + e.getMessage() + "稍后重新获取");
				Thread.sleep(30 * 1000);

			}

		}
	}

	private static List<Map<String, Integer>> getAllPosition() {
		List<Map<String, Integer>> allPosition = new ArrayList<Map<String, Integer>>();
		if (PropertiesUtil.getProperty("multi").equals("off")) {
			Map<String, Integer> position0 = new HashMap<String, Integer>();
			position0.put("positionX", initPositionX);
			position0.put("positionY", initPositionY);
			allPosition.add(position0);
		}

		else if(PropertiesUtil.getProperty("preview").equals("on")) {
			int machineNum = PropertiesUtil.getIntProperty("number");
			int machinewidth = PropertiesUtil.getIntProperty("width");
			int upOffset = PropertiesUtil.getIntProperty("up_offset");

			for (int i = 0; i < machineNum; i++) {
				Map<String, Integer> position1 = new HashMap<String, Integer>();
				position1.put("positionX", (int) machinewidth * ((2 * i) + 1) / 2);
				position1.put("positionY", upOffset);
				allPosition.add(position1);
			}
		}else {
			int machineNum = PropertiesUtil.getIntProperty("number");
			int lineWidth = PropertiesUtil.getIntProperty("no_preview_line_width");
			int noPreviewOffset = PropertiesUtil.getIntProperty("no_preview_up_offset");
			for (int i = 0; i < machineNum; i++) {
				Map<String, Integer> position2 = new HashMap<String, Integer>();
				position2.put("positionX", initPositionX );
				position2.put("positionY", noPreviewOffset - (int) (i+1/2) *lineWidth);
				allPosition.add(position2);
			}
		
		
		
		}
		return allPosition;

	}

	private static int getRandomData(String[] data) {
		int result;
		int resMin = Integer.valueOf(data[0]);
		if (data.length == 2) {

			int resMax = Integer.valueOf(data[1]);
			int range = resMax + 1 - resMin;
			result = resMin + +new Random().nextInt(range);
		} else {
			result = resMin;
		}
		return result;

	}

	// 对单个模拟器进行加人操作
	private static Map<String, Integer> round(Map<String, Integer> position) throws Exception {

		String account;
		int addCount = 0;
		int addNum = getRandomData(addNumber);

		Jedis jedis = getRedisConnection();

		while (true) {

			try {
				account = jedis.rpop("acct");
			} catch (Exception e) {
				System.out.println("获取数据错误：" + e.getMessage());
				jedis = getRedisConnection();
				continue;
			}
			if (account == null) {
				System.out.println("用户名单文件为空,稍后继续观察");
				Thread.sleep(30 * 1000);
				continue;
			}
			addCount += 1;
			String message = messages[new Random().nextInt(messages.length)];
			log.debug("account " + account.trim() + ";");
			int res = AddFriends.addFriend(account, message);
			log.info("添加好友:" + account.trim() + "结果 : " + res);
			if (res == -1) {

				position.put("sleep", getRandomData(exceptionTimes));

				break;
			} else if (addCount == addNum) {
				position.put("sleep", getRandomData(roundTimes));
				break;
			}

		}
		jedis.close();
		position.put("sleep_start", currentTime());
		AddFriends.minimizeWindow();
		log.debug("当前position " + position);
		return position;
	}

	// 找到可用的模拟器
	private static Map<String, Integer> getRightPosition() throws InterruptedException {
		log.info("切换到其他可用的模拟器");
		Collections.shuffle(positionInfos);
		while (true) {
			for (Map<String, Integer> pos : positionInfos) {
				if (pos.get("sleep") != null) {
					int waitedTime = currentTime() - pos.get("sleep_start");
					if (waitedTime > pos.get("sleep")) {
						return pos;
					} else
						System.out.println("坐标: " + pos + ",还要等待 : " + (pos.get("sleep") - waitedTime));

				} else
					return pos;
			}

			Thread.sleep(1000 * 10);
		}

	}

	public static void main(String[] args) throws InterruptedException, FindFailed, IOException {

		positionInfos = getAllPosition();
		System.out.println(PropertiesUtil.getProperty("chat_message"));
		log.info("即将运行");

		Thread.sleep(6000);
		try {
			while (true) {

				Map<String, Integer> pos = getRightPosition();
				int posIndex = positionInfos.indexOf(pos);
				log.debug("索引为" + posIndex + "详情:" + pos);

				if (!PropertiesUtil.getProperty("multi").equals("off")) {
					new Location(initPositionX, initPositionY).click(); // 点击原点

				} // 不是单个的情况下 需要先点击原点
				new Location((double) pos.get("positionX"), (double) pos.get("positionY")).click();

				Thread.sleep(3000);

				// System.out.println("扫描用户列表，准备对新通过好友发消息");
				// SikuliExample.go(PropertiesUtil.getProperty("chat_message"));
				positionInfos.set(posIndex, round(pos));
				log.debug("所有的" + positionInfos);
			}
		} catch (Exception e) {
			e.printStackTrace();

			log.error("报错:" + e);

		}
	}

}