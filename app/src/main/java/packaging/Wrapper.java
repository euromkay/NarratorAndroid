package packaging;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import shared.packaging.Deliverer;

public class Wrapper implements Deliverer{

	private Parcel p;
	
	public Wrapper(Parcel p) {
		this.p = p;
	}

	public int readInt() {
		return p.readInt();
	}

	public void writeInt(int size) {
		p.writeInt(size);
		
	}


	public byte readByte() {
		return p.readByte();
	}

	public void writeByte(byte b) {
		p.writeByte(b);
		
	}

	public void readStringList(ArrayList<String> list) {
		p.readStringList(list);
		
	}

	public void writeStringList(List<String> list) {
		p.writeStringList(list);
	}


	public String readString() {
		return p.readString();
	}

	public void writeString(String s) {
		p.writeString(s);
		
	}


	public int[] createIntArray() {
		return p.createIntArray();
	}


	public void writeIntArray(int[] array) {
		p.writeIntArray(array);
		
	}


	public void finish() {
		
	}

	public void signal(String s) {
		
	}

	public void switchMode() {
		
	}

}
