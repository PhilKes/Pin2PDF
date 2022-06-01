package com.philkes.pin2pdf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.philkes.pin2pdf.storage.local.service.DBService;

import java.util.function.Consumer;

public class Util {

    /**
     * Returns the Domain name of the given URL
     */
    public static String getUrlDomainName(String url) {
        String domainName = new String(url);

        int index = domainName.indexOf("://");

        if (index != -1) {
            // keep everything after the "://"
            domainName = domainName.substring(index + 3);
        }

        index = domainName.indexOf('/');

        if (index != -1) {
            // keep everything before the '/'
            domainName = domainName.substring(0, index);
        }

        // check for and remove a preceding 'www'
        // followed by any sequence of characters (non-greedy)
        // followed by a '.'
        // from the beginning of the string
        domainName = domainName.replaceFirst("^www.*?\\.", "");

        return domainName;
    }

    /**
     * Show Dialog to enter Pinterest Username and store it in SharedPreference
     */
    public static void showUsernameInputDialog(Context context, Consumer<String> onSuccess) {
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("Please enter your Pinterest Username");

        String prefKey=context.getResources().getString(R.string.app_name);
        SharedPreferences sharedPref=context.getSharedPreferences(prefKey, Context.MODE_PRIVATE);

        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.edit_username, null);
        EditText editUsernameText= view.findViewById(R.id.edit_username_text);
        editUsernameText.setText(sharedPref.getString(prefKey,""));
        builder.setView(view);
        builder.setPositiveButton("OK", (dialog, which) -> {
            SharedPreferences.Editor preferenceEdit=sharedPref.edit();
            String username=editUsernameText.getText().toString();
            preferenceEdit.putString(context.getResources().getString(R.string.key_user_name), username);
            preferenceEdit.apply();
           // DBService.getInstance(context).clearAll();
            if(onSuccess!=null){
                onSuccess.accept(username);
            }
        });

        builder.setCancelable(false);
        builder.show();
    }
}
