package android.day;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import voss.narrator.R;


public class ChatAdapter extends BaseAdapter {

    public ArrayList<ChatItem> chatReference;
    LayoutInflater inflater;
    public ChatAdapter(ArrayList<ChatItem> chatReference, Context a){
        this.chatReference = chatReference;
        this.inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public int getCount(){
        return chatReference.size();
    }

    public long getItemId(int x){
        return 0;
    }

    public Object getItem(int position){
        return chatReference.get(position);
    }

    public View getView(int position, View vi, ViewGroup parent) {
        if (vi == null)
            vi = inflater.inflate(R.layout.chat_item, null);

        ChatItem cItem = chatReference.get(position);

        TextView textContent = (TextView) vi.findViewById(R.id.chat_item_text);
        TextView fromContent = (TextView) vi.findViewById(R.id.chat_item_sender);

        boolean chatItem = cItem.playerHTML != null;

        if(chatItem){
            fromContent.setText(Html.fromHtml(cItem.playerHTML));
            textContent.setText(cItem.text);
        }else{
            fromContent.setText("");
            textContent.setText(Html.fromHtml(cItem.text));
        }

        return vi;
    }
}
