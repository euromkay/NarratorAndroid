package voss.android.texting;

/**
 * Created by Michael on 9/26/2015.
 */
public class PhoneNumber implements Comparable<PhoneNumber>{

    String number;
    public PhoneNumber(String number){
        this.number = number;
    }

    public String toString(){
        return number;
    }

    public boolean equals(Object o){
        if(o == null)
            return false;
        if(o.getClass() != getClass())
            return false;
        if(!number.equals(((PhoneNumber) o).number))
            return false;

        return true;
    }

    public int compareTo(PhoneNumber t){
        return number.compareTo(t.number);
    }
}
