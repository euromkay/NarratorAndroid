package voss.android.alerts;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import voss.android.R;
import voss.android.screens.ListingAdapter;
import voss.android.setup.ActivityCreateGame;
import voss.android.texting.PhoneNumber;


public class PhoneBookPopUp  extends DialogFragment implements OnClickListener, AdapterView.OnItemClickListener, LoaderCallbacks<Cursor>{


    public static final String PLAYER_LIST_KEY = "listforkey";

    View mainView;
    String[] players;
    ListView contactsLV;
    boolean isHost;
    public void setIsHost(){
        isHost = true;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){


        mainView = inflater.inflate(R.layout.create_player_list, container);

        Button b = (Button) mainView.findViewById(R.id.addPlayerConfirm);
        b.setOnClickListener(this);
        b.setText("Invite");

        setTitle("Invite people who will text to play:");

        contactsLV = (ListView) mainView.findViewById(R.id.listView1);
        contactsLV.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        contactsLV.setStackFromBottom(true);

        final EditText et = (EditText) mainView.findViewById(R.id.addPlayerContent);
        et.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            public void afterTextChanged(Editable s) {
                startQuery(et.getText().toString());
            }
        });
        return mainView;
    }


    public static final int CONTACT_QUERY_LOADER = 0;
    public static final String QUERY_KEY = "query";
    private void startQuery(String query){
        Bundle bundle = new Bundle();
        bundle.putString(QUERY_KEY, query);


        // Start the loader with the new query, and an object that will handle all callbacks.
        getLoaderManager().restartLoader(CONTACT_QUERY_LOADER, bundle, this);
    }

	public void setTitle(String s){
        getDialog().setTitle(s);
    }


    private static final int CONTACT_ID_INDEX = 0;
    private static final int LOOKUP_KEY_INDEX = 1;

    ArrayList<String> persistant = new ArrayList<String>();
    HashMap<String, PhoneNumber> contactList = new HashMap<>();
    public void onItemClick(AdapterView<?> parent, View v, int position, long l){
        TextView tv = (TextView) v;
        String name = tv.getText().toString();
        if(isHost){
            if(persistant.contains(name)){
                tv.setTextColor(Color.parseColor("#ffffff"));
                persistant.remove(name);
                contactList.remove(name);
            }else{
                tv.setTextColor(ActivityCreateGame.ParseColor(mListener.passContext(), R.color.trimmings));
                persistant.add(name);

            }
        }else{
            PhoneNumber number = contactList.get(name);
            mListener.startJoinProcess(number);
        }
    }

    private ArrayAdapter<String> getAdapter(String[] players){
        return new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, players);
    }


    public void onClick(View v) {
        HashMap<String, PhoneNumber> toInvite = new HashMap<String, PhoneNumber>();
        for(String name: persistant){
            toInvite.put(name, contactList.get(name));
        }
        mListener.startGame(this, toInvite, isHost);
    }

    public void refreshAdapter(ArrayList<String> newPeople){
        ArrayList<String> toAdd = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<Integer>();
        if(newPeople != null)
            toAdd.addAll(newPeople);
        toAdd.addAll(persistant);
        int white = parseColor(R.color.white);
        int trim = parseColor(R.color.trimmings);
        for(int i = 0; newPeople != null && i < newPeople.size(); i++){
            colors.add(white);
        }
        for(int i = 0; i < persistant.size(); i++){
            colors.add(trim);
        }
        contactsLV.setAdapter(new ListingAdapter(toAdd, getActivity()).setColors(colors));
    }

    public int parseColor(int id){
        return ActivityCreateGame.ParseColor(mListener.passContext(), id);
    }




    private boolean checkName(String name, EditText et){
        if (name.toLowerCase().equals("cancel") || name.toLowerCase().equals("skip")){
            Toast.makeText(getActivity(), "don't use this name", Toast.LENGTH_LONG).show();
            et.setText("");
            return true;
        }
        return false;
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Where the Contactables table excels is matching text queries,
        // not just data dumps from Contacts db.  One search term is used to query
        // display name, email address and phone number.  In this case, the query was extracted
        // from an incoming intent in the handleIntent() method, via the
        // intent.getStringExtra() method.

        // BEGIN_INCLUDE(uri_with_query)
        String query = args.getString(QUERY_KEY);
        Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Contactables.CONTENT_FILTER_URI, query);
        // END_INCLUDE(uri_with_query)


        // BEGIN_INCLUDE(cursor_loader)
        // Easy way to limit the query to contacts with phone numbers.
        String selection =
                ContactsContract.CommonDataKinds.Contactables.HAS_PHONE_NUMBER + " = " + 1;

        // Sort results such that rows for the same contact stay together.
        String sortBy = ContactsContract.CommonDataKinds.Contactables.LOOKUP_KEY;

        return new CursorLoader(
                mListener.passContext(),  // Context
                uri,       // URI representing the table/resource to be queried
                null,      // projection - the list of columns to return.  Null means "all"
                selection, // selection - Which rows to return (condition rows must match)
                null,      // selection args - can be provided separately and subbed into selection.
                sortBy);   // string specifying sort order
        // END_INCLUDE(cursor_loader)
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        ArrayList<String> contacts = new ArrayList<>();

        if (cursor.getCount() == 0) {
            refreshAdapter(null);
            return;
        }

        // Pulling the relevant value from the cursor requires knowing the column index to pull
        // it from.
        // BEGIN_INCLUDE(get_columns)
        int phoneColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.DISPLAY_NAME);
        int lookupColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.LOOKUP_KEY);
        int typeColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.MIMETYPE);
        // END_INCLUDE(get_columns)

        cursor.moveToFirst();
        // Lookup key is the easiest way to verify a row of data is for the same
        // contact as the previous row.
         String lookupKey = "";
        String name = null;
        String number = null;
        do {
            // BEGIN_INCLUDE(lookup_key)
            String currentLookupKey = cursor.getString(lookupColumnIndex);
            if (!lookupKey.equals(currentLookupKey)) {
                String displayName = cursor.getString(nameColumnIndex);
                if(persistant.contains(displayName))
                    continue;
                name = displayName;
                lookupKey = currentLookupKey;
            }

            String mimeType = cursor.getString(typeColumnIndex);
            if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                if(name != null){
                    contactList.put(name, new PhoneNumber(cursor.getString(phoneColumnIndex)));
                    contacts.add(name);
                }
            }
            // END_INCLUDE(retrieve_data)

            // Look at DDMS to see all the columns returned by a query to Contactables.
            // Behold, the firehose!
        } while (cursor.moveToNext());

        refreshAdapter(contacts);
    }


    public void onLoaderReset(Loader<Cursor> loader) {
    	
    }

    public interface AddPhoneListener {
        Context passContext();
        void startJoinProcess(PhoneNumber number);
        void startGame(PhoneBookPopUp pop, HashMap<String, PhoneNumber> contacts, boolean isHost);
    }

    private AddPhoneListener mListener;

    public void onAttach(Activity a){
        super.onAttach(a);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (AddPhoneListener) a;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(a.toString()
                    + " must implement PhoneBookListener");
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        contactsLV.setOnItemClickListener(this);
    }

    public void setButton(String s){
        ((Button)(mainView.findViewById(R.id.addPlayerConfirm))).setText(s);
    }




    private static final String[] PROJECTION =
            {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    Build.VERSION.SDK_INT
                            >= Build.VERSION_CODES.HONEYCOMB ?
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                            ContactsContract.Contacts.DISPLAY_NAME

            };

    private static final String SELECTION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?" :
                    ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
}
