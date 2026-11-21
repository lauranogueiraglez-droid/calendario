package com.valeria.adviento;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** Aviso diario a las 10:30 (hora de España) del 1 al 24 de diciembre. */
final class Notify {
    static final String CHANNEL = "bloques";
    static final String PREFS = "adviento";
    static final ZoneId ZONE = ZoneId.of("Europe/Madrid");

    private Notify() { }

    static void channel(Context c) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm != null && nm.getNotificationChannel(CHANNEL) == null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Bloque del día", NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("Aviso cada mañana de diciembre");
            nm.createNotificationChannel(ch);
        }
    }

    static void schedule(Context c) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        if (am == null) return;
        Intent i = new Intent(c, DailyReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(c, 1, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pi);
        SharedPreferences p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!p.getBoolean("on", true)) return;
        ZonedDateTime now = ZonedDateTime.now(ZONE);
        ZonedDateTime t = now.withHour(10).withMinute(30).withSecond(0).withNano(0);
        if (!t.isAfter(now)) t = t.plusDays(1);
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t.toInstant().toEpochMilli(), pi);
    }

    static int today() {
        LocalDate d = LocalDate.now(ZONE);
        return (d.getMonthValue() == 12 && d.getDayOfMonth() <= 24) ? d.getDayOfMonth() : 0;
    }

    static void show(Context c, int day, String name, boolean test) {
        channel(c);
        if (day < 1 || day > 24) day = 1;
        Intent open = new Intent(c, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(c, 2, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        String who = (name == null || name.isEmpty()) ? "" : ", " + name;
        String title = day == 24 ? "¡Último bloque!" : "¡Bloque " + day + " desbloqueado!";
        String text = day == 24
                ? "Hoy es Nochebuena" + who + ". Ve a por el bloque 24."
                : "Ya puedes picar el bloque " + day + who + ". Te espera un minijuego.";
        if (test) title = "Así llegará el aviso: " + title;
        Notification n = new Notification.Builder(c, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        try {
            if (nm != null) nm.notify(100 + day, n);
        } catch (SecurityException ignored) { }
    }
}
