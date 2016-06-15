package android;

import java.util.TreeMap;

import android.texting.CommunicatorText;
import android.texting.PhoneNumber;
import shared.logic.Narrator;
import shared.logic.Player;


public class PhoneBook {
    private TreeMap<PhoneNumber, Player> numberBook;
    private TreeMap<String, Player> nameBook;

    public PhoneBook(Narrator n){
        numberBook = new TreeMap<>();
        nameBook = new TreeMap<>();
        for(Player p: n.getAllPlayers()){
            if(p.getCommunicator() != null && p.getCommunicator().getClass() == CommunicatorText.class){
                PhoneNumber number = ((CommunicatorText) p.getCommunicator()).getNumber();
                numberBook.put(number, p);
            }
            nameBook.put(p.getName().toLowerCase(), p);
        }
        nameBook.put(n.Skipper.getName().toLowerCase(), n.Skipper);
    }

    public Player getByName(String name){
        return nameBook.get(name.toLowerCase());
    }
    public Player getByNumber(PhoneNumber number){
        return numberBook.get(number);
    }

}
