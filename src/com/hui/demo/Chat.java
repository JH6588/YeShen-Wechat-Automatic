package com.hui.demo;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;

public class Chat {
	final static int slide_max = 1;
	static Screen screen = new Screen();

	public static void chatToNew(String msg) throws InterruptedException {

		while (true) {
			try {
				talk(msg);

			} catch (FindFailed e) {
				System.out.println("扫描结束");
				break;
			} catch (Exception e) {
				System.out.println("添加错误:" + e.getMessage());
				break;
			}

		}

	}

	public static void talk(String msg) throws FindFailed, InterruptedException {

		Match aim = screen.find(".\\images\\1515123285375.png"); // 1515123285375
		screen.click(aim);
		Thread.sleep(1000);
		screen.paste(".\\images\\1514273805574.png", msg);
		screen.wait(".\\images\\1514273594711.png");
		screen.click(".\\images\\1514273602432.png");
		screen.click(".\\images\\1514273616893.png");
		Thread.sleep(1000);
	}

	public static void main(String[] args) throws InterruptedException {
		chatToNew("88");
	}
}