package com.example.forwalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.core.content.ContextCompat;


public class SharedReference {

    static final String USER_NAME="username";
    static final String USER_PW="userpw";
    static final String USER_TYPE="usertype";
    static SharedPreferences getSharedReferences(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUserName(Context ctx, String userName, String userPw, int userType){
        SharedPreferences.Editor editor = getSharedReferences(ctx).edit();
        editor.putString(USER_NAME,userName);
        editor.putString(USER_PW,userPw);
        editor.putInt(USER_TYPE,userType);
        editor.commit();
    }//save id and pw

    public static String getUserName(Context ctx){
        return getSharedReferences(ctx).getString(USER_NAME,"");
    }//bring saved id

    public static String getUserPw(Context ctx){
        return getSharedReferences(ctx).getString(USER_PW,"");
    }//bring saved pw

    public static int getUserType(Context ctx){
        return getSharedReferences(ctx).getInt(USER_TYPE,2);
    }

    public static void clearUserName(Context ctx){
        SharedPreferences.Editor editor = getSharedReferences(ctx).edit();
        editor.clear();
        editor.commit();
    }//clear saved id and pw
}
