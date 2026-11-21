package com.valeria.adviento;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.time.LocalDate;

public class DailyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        SharedPreferences p = c.getSharedPreferences(Notify.PREFS, Context.MODE_PRIVATE);
        if (p.getBoolean("on", true)) {
            LocalDate d = LocalDate.now(Notify.ZONE);
            int year = p.getInt("year", d.getYear());
            int day = Notify.today();
            if (day > 0 && d.getYear() == year) Notify.show(c, day, p.getString("name", ""), false);
        }
        Notify.schedule(c);
    }
}
