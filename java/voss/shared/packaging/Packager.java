package voss.shared.packaging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import voss.shared.event.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;

public class Packager{
	private Deliverer parcel;
	
	public Packager(Deliverer parcel2){
		this.parcel = parcel2;
	}

	public byte readByte() {
		return parcel.readByte();
	}

	public HashSet<Player> readHashSet(PlayerList players){
		HashSet<Player> set = new HashSet<Player>();
		
		int size = parcel.readInt();
		
		for(int i = 0; i < size; i++){
			set.add(players.getPlayerByName(parcel.readString()));
		}
		
		return set;
	}
	

	public void readStringList(ArrayList<String> list) {
		parcel.readStringList(list);
	}
	
	public int[] createIntArray() {
		return parcel.createIntArray();
	}

	public void writeHashSet(PlayerList players) {
		parcel.writeInt(players.size());
		for(Player p: players){
			parcel.writeString(p.getName());
		}
	}


	private static final byte TRUE = 1;
	private static final byte FALSE = 0;
	public boolean readBool() {
		byte b = parcel.readByte();
		if(b == TRUE)
			return true;
		if(b == FALSE)
			return false;
		else{
			parcel.finish();
			throw new Error("parceling is off: " + b);
		}
	}

	public void write(boolean b) {
		if(b)
			parcel.writeByte(TRUE);
		else
			parcel.writeByte(FALSE);
	}

	public ArrayList<Integer> readIntegerList() {
		if(readBool()){
			ArrayList<Integer> list = new ArrayList<Integer>();
			int size = parcel.readInt();
			for(int i = 0; i < size; i++)
				list.add(parcel.readInt());
			return list;
		}else
			return null;
			
	
	}

	public void write(ArrayList<Integer> list) {
		if(list == null)
			write(false);
		else{
			write(true);
			parcel.writeInt(list.size());
			for(int i: list){
				parcel.writeInt(i);
			}
		}
	}
	@SuppressWarnings("unused")
	private static int[] listToArray(ArrayList<Integer> list){
		int[] array = new int[list.size()];
		for(int i = 0; i < list.size(); i++){
			array[i] = list.get(i);
		}
		return array;
	}

	public ArrayList<String> readStringList() {
		
		ArrayList<String> list = new ArrayList<String>();
		parcel.readStringList(list);
		
		
		return list;
	}

	public void write(List<String> list) {
		parcel.writeStringList(list);
		
	}

	public String readString() {
		String s = parcel.readString();
		return s;
	}

	public void write(String s) {
		parcel.writeString(s);
	}

	public int[] readIntArray() {
		if(readBool() == true)
			return null;
		
		int[] array = parcel.createIntArray();
		return array;
	}

	public void write(int[] array) {
		if(array == null){
			write(true);
			return;
		}
		write(false);
		
		parcel.writeIntArray(array);
	}
	
	public boolean[] readBoolArray() {
		if(readBool() == true)
			return null;
		boolean[] array = new boolean[readInt()];
		for(int i = 0; i < array.length; i++)
			array[i] = readBool();
		return array;
	}

	public void writeArray(boolean[] array) {
		if(array == null){
			write(true);
			return;
		}
		write(false);
		write(array.length);
		for(boolean b: array)
			write(b);
	}

	public int readInt() {
		int i = parcel.readInt();
		return i;
	}

	public void write(int i) {
		parcel.writeInt(i);
	}

	public void write(PlayerList list) {
		parcel.writeInt(list.size());
		for(Player p: list)
			write(p);
		
	}
	
	public PlayerList readPlayers(Narrator n){
		int size = parcel.readInt();
		PlayerList list = new PlayerList();
		for(int i = 0; i < size; i++)
			list.add(readPlayer(n));
		return list;
	}

	public Player readPlayer(Narrator n) {
		String name = parcel.readString();
		if(name == NULL_PLAYER)
			return null;
		return n.getPlayerByName(name);
	}
	
	private static final String NULL_PLAYER = null;
	
	public void write(Player p){
		if(p == null)
			parcel.writeString(NULL_PLAYER);
		else
			parcel.writeString(p.getName());
	}

	public void signal(String s) {
		parcel.signal(s);
		
	}

	public void reading() {
		parcel.switchMode();
		
	}

	public void finish() {
		parcel.finish();
		
	}




	public void writeToPackage(Event e) {
		
	}


	
}
