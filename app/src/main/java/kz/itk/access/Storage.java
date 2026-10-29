package kz.itk.access;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Storage {

    private static final String PREFS_NAME = "access_data";
    private static final String KEY_PREFIX = "key_";
    private static final String MESSAGES = "messages";

    private final SharedPreferences prefs;

    public Storage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isRegistered() {
        return prefs.contains(Field.IIN.name());
    }

    public void register(Profile profile, Map<Role, String> keys) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Field.SURNAME.name(), profile.surname);
        editor.putString(Field.NAME.name(), profile.name);
        editor.putString(Field.PATRONYMIC.name(), profile.patronymic);
        editor.putString(Field.IIN.name(), profile.iin);
        for (Map.Entry<Role, String> entry : keys.entrySet()) {
            editor.putString(KEY_PREFIX + entry.getKey().name(), CaesarCipher.encrypt(entry.getValue()));
        }
        editor.putString(MESSAGES, new JSONArray().toString());
        editor.apply();
    }

    public Role findRoleByKey(String key) {
        String encrypted = CaesarCipher.encrypt(key);
        for (Role role : Role.values()) {
            if (encrypted.equals(prefs.getString(KEY_PREFIX + role.name(), null))) {
                return role;
            }
        }
        return null;
    }

    public String getField(Field field) {
        return prefs.getString(field.name(), "");
    }

    public List<String> getMessages() {
        List<String> messages = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(MESSAGES, "[]"));
            for (int i = 0; i < array.length(); i++) {
                messages.add(array.getString(i));
            }
        } catch (JSONException e) {
            messages.clear();
        }
        return messages;
    }

    public void addMessage(String text) {
        String time = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date());
        JSONArray array = new JSONArray(getMessages());
        array.put("[" + time + "] " + text);
        prefs.edit().putString(MESSAGES, array.toString()).apply();
    }
}
