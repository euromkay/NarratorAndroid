package voss.shared.packaging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Box implements Deliverer{

	private FileWriter in;
	private FileWriter out;
	private Queue<String> bx = new LinkedList<String>();
	
	
	public Box(){
		try {
			in = new FileWriter(new File("input.txt"));
			out = new FileWriter(new File("output.txt")); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void write(String s){
		counter++;
		try {
			if(s == null)
				in.write("null");
			else
				in.write(s+"");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void read(String s){
		counter--;
		try {
			if(s == null)
				out.write("null");
			else
				out.write(s+"");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	 void queue(String s){
		bx.add(s);
		write(s);
	}
	private String dequeue(){
		String s = bx.remove();
		read(s);
		if(counter == -1){
			counter = signals.remove();
			return dequeue();
		}
		else
			return s;
	}
	
	public int readInt() {
		return Integer.parseInt(dequeue());
	}

	
	public void writeInt(int size) {
		queue(size+"");
		
	}

	
	public byte readByte() {
		return Byte.parseByte(dequeue());
	}

	
	public void writeByte(byte b) {
		queue(b+"");
		
	}

	
	public void readStringList(ArrayList<String> list) {
		int size = readInt();
		for(int i = 0; i < size; i++)
			list.add(dequeue());
	}

	
	public void writeStringList(List<String> list) {
		writeInt(list.size());
		for(String s: list)
			writeString(s);
		
	}

	
	public String readString() {
		return dequeue();
	}

	
	public void writeString(String s) {
		queue(s);
	}

	
	public int[] createIntArray() {
		int[] array = new int[readInt()];
		for(int i = 0; i < array.length; i++)
			array[i] = readInt();
		return array;
	}

	
	public void writeIntArray(int[] array) {
		writeInt(array.length);
		for(int i : array)
			writeInt(i);
	}

	
	public void finish() {
		try {
			in.flush();
			out.flush();
			out.close();
			in.close();
		} catch (IOException e) {
			//e.printStackTrace();
		}
		
	}

	private Queue<Integer> signals = new LinkedList<Integer>();
	private int counter = 0;
	public void signal(String s) {
		signals.add(counter);
		queue(s);
		counter = 0;
	}

	public void switchMode() {
		signals.add(counter);
		counter = signals.remove();
	}

}
