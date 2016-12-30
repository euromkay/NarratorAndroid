package android.day;

import json.JSONObject;

public class ChatItem {
	
	public String text, playerHTML;
	public ChatItem(String text){
		this.text = text;
	}
	
	public ChatItem(String playerHTML, String text){
		this.text = text;
		this.playerHTML = playerHTML;
	}
}