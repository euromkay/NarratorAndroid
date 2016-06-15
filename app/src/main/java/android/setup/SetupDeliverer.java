package android.setup;


import java.util.ArrayList;
import java.util.List;

import shared.logic.exceptions.UnsupportedMethodException;
import shared.packaging.Deliverer;

public class SetupDeliverer implements Deliverer{

    public SetupDeliverer(){
        data = new ArrayList<String>();
    }

    private ArrayList<String> data;
    public SetupDeliverer(String compress){
        data = new ArrayList<String>();
        for(String s: compress.split("@")){
            if(s.length() == 0)
                continue;
            data.add(s);
        }
    }

    private String pop(){
        return data.remove(0);
    }
    private void push(String s){
        data.add(s+"@");
    }

    public int readInt(){
        return Integer.parseInt(pop());
    }

    public void writeInt(int size){
        push(size+"");
    }

    public byte readByte(){
        return Byte.parseByte(pop());
    }

    public void writeByte(byte true1){
        push(true1+"");
    }



    public void readStringList(ArrayList<String> list){
        throw new UnsupportedMethodException();
    }

    public void writeStringList(List<String> list){
        throw new UnsupportedMethodException();
    }

    public String readString(){
        return pop();
    }

    public void writeString(String s){
        push(s);
    }

    public int[] createIntArray(){
        throw new UnsupportedMethodException();
    }

    public void writeIntArray(int[] array){
        throw new UnsupportedMethodException();
    }

    public void finish(){
        throw new UnsupportedMethodException();
    }

    public void signal(String s){

    }

    public void switchMode(){
        throw new UnsupportedMethodException();
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(String s: data)
            sb.append(s);
        return sb.toString();
    }
}
