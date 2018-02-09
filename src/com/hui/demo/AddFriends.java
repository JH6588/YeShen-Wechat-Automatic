package com.hui.demo;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

public class AddFriends {
	static Screen screen = new Screen();
	static void back() throws FindFailed, InterruptedException {
		while(true) {
			
			if (screen.exists(".\\images\\1514887860813.png") != null) {
				screen. click(".\\images\\1514887860813.png");
				Thread.sleep(1000);
			
			}else
				break;
			
		}
	}
	static int  addFriend(String user ,String message) throws FindFailed, InterruptedException {
		
			if (screen.exists(".\\images\\1514887860813.png") != null) {
				back();
			} //进入主界面
			
			System.out.println("扫描用户列表，准备对新通过好友发消息");
			Chat.chatToNew(PropertiesUtil.getProperty("chat_message"));
			
			if (screen.exists(".\\images\\1514887860813.png") != null) {
				back();
			}  //进入主界面
			screen.click(".\\images\\1514887909760.png");
			screen.wait(".\\images\\1514887860813.png" ,5);
			screen.paste(user.trim());
			Thread.sleep(1000);
			
			
			
			if(screen.exists(".\\images\\1514888140612.png") != null) {
				back();
				return 2;  //已经是好友
			}else if (screen.exists(".\\images\\1514888204446.png") != null) {
				screen.click("\\images\\1514888204446.png");
			}else {
				back(); //搜好友时 未知情况
			}
		
			if (screen.exists("\\images\\1514888288244.png") != null ||
					screen.exists("\\images\\1514957951722.png")   != null) {
				back();
				return 0; //不存在 或者被搜账号异常
			}else if(screen .exists("\\images\\1514888307934.png") != null) {
				screen.click("\\images\\1514888307934.png");

				
				//有些直接通过的情况 排除
				try {
				screen.wait("\\images\\1514888341023.png",5);
				screen.click("\\images\\1514888418571.png");
				screen. paste(new Pattern("\\images\\1514888488503.png").targetOffset(-23,13), message);
	            screen.click("\\images\\1514888341023.png") ;
	            screen.wait("\\images\\1514888307934.png" ,5) ;
	            back();
	            return 1;  //成功
				}catch(Exception e){
					e.printStackTrace();
					if (screen.exists("\\images\\1514888307934.png") != null) {
						back();
						return 1;
					}
									
				}		
			}  else  if (screen.exists("\\images\\1515033349301.png") != null) {
				back();
				return  -1;
			}
			
			return 5; //异常情况
			
		}
	
	static void minimizeWindow() throws FindFailed {
		
		screen.click(new Pattern("\\images\\1514887909760.png").targetOffset(3, -50));
		
	}
	

}
