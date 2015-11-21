package voss.packaging;

import android.os.Parcel;
import android.os.Parcelable;
import voss.android.CommunicatorPhone;
import voss.android.texting.CommunicatorText;
import voss.android.wifi.CommunicatorInternet;
import voss.logic.Narrator;
import voss.logic.support.Communicator;
import voss.logic.support.CommunicatorHandler;
import voss.logic.support.CommunicatorNull;

public class Board implements CommunicatorHandler, Parcelable{

	private Narrator n;
	public Board(Narrator n){
		this.n = n;
	}

	public Board(){

	}

	public static Board GetParcel(Narrator n) {
		Board b = new Board(n);
		return b;
	}

	public static final Parcelable.Creator<Board> CREATOR = new Parcelable.Creator<Board>() {
		public Board createFromParcel(Parcel in) {
			Wrapper wrap = new Wrapper(in);
			Packager pack = new Packager(wrap);
			Board b = new Board();
			b.setNarrator(new Narrator(pack, b));
			return b;
		}

		public Board[] newArray(int size) {
			return new Board[size];
		}
	};

	public int describeContents() {
		return 0;
	}

	public void setNarrator(Narrator narrator) {
		this.n = narrator;
	}

	public void writeToParcel(Parcel parcel, int flags) {
		p = new Packager(new Wrapper(parcel));
		n.writeToPackage(p, this);
	}

	private Packager p;

	public void writeToBox() {
		p = new Packager(new Box());
		n.writeToPackage(p, this);
	}

	public Narrator getFromBox() {
		p.reading();
		try{
			return new Narrator(p, this);
		}catch (Error e){
			p.finish();
			throw e;
		}
	}

	public void finish() {
		p.finish();
	}

	public static Narrator getNarrator(Parcelable p) {
		if(p == null)
			return null;
		return ((Board) p).n;
	}

	public Communicator getComm(Packager in, Narrator narrator) {
		Communicator comm = null;
		switch (in.readInt()) {

			case PHONE:
				comm = new CommunicatorPhone();
				break;
			case TEXT:
				comm = new CommunicatorText();
				comm.getFromParcel(in);
				break;
			case INTERNET:
				comm = new CommunicatorInternet(null);
				break;
			default:
				comm = new CommunicatorNull();
				break;
		}
		return comm;
	}

	public static final int BASIC = 0;
	public static final int PHONE = 1;
	public static final int TEXT = 2;
	public static final int INTERNET = 3;

	public void writeHeading(Packager p, Communicator c) {
		// TODO Auto-generated method stub
		if(c instanceof CommunicatorNull)
			p.write(BASIC);
		else if(c instanceof CommunicatorPhone)
			p.write(PHONE);
		else if(c instanceof CommunicatorText)
			p.write(TEXT);
		else if(c instanceof CommunicatorInternet)
			p.write(INTERNET);

	}
}




	
	
	
