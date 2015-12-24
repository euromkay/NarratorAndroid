package voss.android.texting;

import android.telephony.PhoneNumberUtils;

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

        return PhoneNumberUtils.compare(number, ((PhoneNumber) o).number);
    }

    public int compareTo(PhoneNumber t){
        return number.compareTo(t.number);
    }
}
