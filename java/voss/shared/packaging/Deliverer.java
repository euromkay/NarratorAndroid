package voss.shared.packaging;

import java.util.ArrayList;
import java.util.List;

public interface Deliverer{

	int readInt();

	void writeInt(int size);

	byte readByte();

	void writeByte(byte true1);

	void readStringList(ArrayList<String> list);

	void writeStringList(List<String> list);

	String readString();

	void writeString(String s);

	int[] createIntArray();

	void writeIntArray(int[] array);

	void finish();

	void signal(String s);

	void switchMode();

}
