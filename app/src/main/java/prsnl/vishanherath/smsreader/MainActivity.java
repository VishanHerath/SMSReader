package prsnl.vishanherath.smsreader;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static MainActivity inst;
    ArrayList<String> smsMessagesList = new ArrayList<String>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;
    EditText phoneNum;
    Button addNum;
    String numberStr = "";
    SharedPreferences sharedPref;
    static String SHARED_PREF = "sharedPrefs";
    SharedPreferences.Editor edPref;

    public static MainActivity instance() {
        return inst;
    }
    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smsListView = (ListView) findViewById(R.id.SMSList);

        sharedPref = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        edPref = sharedPref.edit();
        //numberStr = sharedPref.getString("number", "null");

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);
        phoneNum = findViewById(R.id.phoneNumTxt);
        addNum = findViewById(R.id.addNumBtn);

        // Add SMS Read Permision At Runtime
        // Todo : If Permission Is Not GRANTED
        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {

            // Todo : If Permission Granted Then Show SMS
            refreshSmsInbox();

        } else {
            // Todo : Then Set Permission
            final int REQUEST_CODE_ASK_PERMISSIONS = 123;
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }
    public void refreshSmsInbox() {
        numberStr = sharedPref.getString("number","null");
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        do {
            if(smsInboxCursor.getString(indexAddress).equals(numberStr)) {
                String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
                        "\n" + smsInboxCursor.getString(indexBody) + "\n";
                arrayAdapter.add(str);
            }
        } while (smsInboxCursor.moveToNext());
    }

    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String[] smsMessages = smsMessagesList.get(pos).split("\n");
            String address = smsMessages[0];
            String smsMessage = "";
            for (int i = 1; i < smsMessages.length; ++i) {
                smsMessage += smsMessages[i];
            }

            String smsMessageStr = address + "\n";
            smsMessageStr += smsMessage;
            Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setNum(View view){
        String temp = phoneNum.getText().toString();
        edPref.putString("number", temp);
        edPref.commit();
    }

    public void viewMssgs(View view){
        refreshSmsInbox();
    }

}