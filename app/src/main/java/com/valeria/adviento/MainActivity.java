package com.valeria.adviento;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class MainActivity extends Activity {

    private static final int PICK_FILE = 42;
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private boolean pageReady = false;
    private String pendingImport = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setTextZoom(100);

        web.addJavascriptInterface(new Bridge(), "Android");

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                String scheme = u.getScheme();
                if ("http".equals(scheme) || "https".equals(scheme)) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, u));
                    } catch (Exception ignored) { }
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                pageReady = true;
                if (pendingImport != null) {
                    sendImport(pendingImport);
                    pendingImport = null;
                }
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                String type = "*/*";
                String[] accept = params.getAcceptTypes();
                if (accept != null && accept.length > 0 && accept[0] != null && accept[0].startsWith("image")) {
                    type = "image/*";
                }
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType(type);
                try {
                    startActivityForResult(Intent.createChooser(i, "Elegir archivo"), PICK_FILE);
                } catch (Exception e) {
                    fileCallback = null;
                    return false;
                }
                return true;
            }
        });

        Notify.channel(this);
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 7);
        }
        Notify.schedule(this);

        web.loadUrl("file:///android_asset/index.html");
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    /** Abre un calendario recibido (por WhatsApp, Drive, Telegram…). */
    private void handleIntent(Intent intent) {
        if (intent == null) return;
        Uri uri = null;
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            uri = intent.getData();
        } else if (Intent.ACTION_SEND.equals(intent.getAction())) {
            uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        }
        if (uri == null) return;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) return;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            String text = out.toString("UTF-8");
            if (pageReady) sendImport(text); else pendingImport = text;
        } catch (Exception ignored) { }
    }

    private void sendImport(String text) {
        web.evaluateJavascript("window.appImport && appImport(" + JSONObject.quote(text) + ")", null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && fileCallback != null) {
            fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            fileCallback = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (web != null) web.evaluateJavascript("window.appPause && appPause()", null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) web.evaluateJavascript("window.appResume && appResume()", null);
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("window.appBack ? appBack() : false", value -> {
            if (!"true".equals(value)) finish();
        });
    }

    private void jsToast(String msg) {
        runOnUiThread(() -> web.evaluateJavascript("window.appToast && appToast(" + JSONObject.quote(msg) + ")", null));
    }

    private class Bridge {
        @JavascriptInterface
        public void setStatusBar(String hex) {
            runOnUiThread(() -> {
                try {
                    getWindow().setStatusBarColor(android.graphics.Color.parseColor(hex));
                } catch (Exception ignored) { }
            });
        }

        /** Guarda un vale (JPG) en la galería, en Imágenes/Adviento. */
        @JavascriptInterface
        public void saveImage(String b64, String name) {
            try {
                byte[] data = Base64.decode(b64, Base64.DEFAULT);
                if (Build.VERSION.SDK_INT >= 29) {
                    ContentValues v = new ContentValues();
                    v.put(MediaStore.Images.Media.DISPLAY_NAME, name);
                    v.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    v.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Adviento");
                    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
                    if (uri == null) throw new Exception("sin uri");
                    try (OutputStream o = getContentResolver().openOutputStream(uri)) {
                        if (o == null) throw new Exception("sin salida");
                        o.write(data);
                    }
                    jsToast("Vale guardado en la galería");
                } else {
                    File dir = new File(getCacheDir(), "shared");
                    if (!dir.exists()) dir.mkdirs();
                    File f = new File(dir, name.replaceAll("[^A-Za-z0-9._-]", "_"));
                    try (FileOutputStream o = new FileOutputStream(f)) {
                        o.write(data);
                    }
                    Uri u = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".files", f);
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("image/jpeg");
                    i.putExtra(Intent.EXTRA_STREAM, u);
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(i, "Guardar vale"));
                }
            } catch (Exception e) {
                jsToast("No se pudo guardar el vale");
            }
        }

        @JavascriptInterface
        public void setNotify(boolean on, String name, int year) {
            SharedPreferences p = getSharedPreferences(Notify.PREFS, Context.MODE_PRIVATE);
            p.edit().putBoolean("on", on).putString("name", name).putInt("year", year).apply();
            Notify.schedule(MainActivity.this);
        }

        @JavascriptInterface
        public void testNotify(String name) {
            int day = Notify.today();
            Notify.show(MainActivity.this, day > 0 ? day : 1, name, true);
        }

        @JavascriptInterface
        public void share(String json, String fileName) {
            try {
                File dir = new File(getCacheDir(), "shared");
                if (!dir.exists()) dir.mkdirs();
                File f = new File(dir, fileName.replaceAll("[^A-Za-z0-9._-]", "_"));
                try (Writer w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8")) {
                    w.write(json);
                }
                Uri uri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".files", f);
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("application/json");
                i.putExtra(Intent.EXTRA_STREAM, uri);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(i, "Enviar calendario"));
            } catch (Exception ignored) { }
        }
    }
}
