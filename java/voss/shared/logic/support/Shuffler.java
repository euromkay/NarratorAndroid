package voss.shared.logic.support;

import java.util.ArrayList;
import java.util.Random;

public class Shuffler {
	public static <T> void shuffle(ArrayList<T> list, Random rand){
		if(list.size() < 2)
			return;
		ArrayList<T> newList = new ArrayList<>();
		for (T t: list)
			newList.add(t);
		
		list.clear();
		
		while (newList.size() >= 2){
			int index = rand.nextInt(newList.size());
			T choice = newList.get(index);
			list.add(choice);
			newList.remove(choice);
		}
		list.add(newList.get(0));
	}
}
