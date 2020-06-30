package com.example.sgamovil20;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class traspaso_recepcion extends AppCompatActivity {

    WebView wv_1;

    private static String DIRECCION_HOST = "";
    private static String UBICACION = "";
    private static String PAGINA = "/api/traspasos_recepcion_detalles.api.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traspaso_recepcion);

        DIRECCION_HOST = MainActivity.get_DireccionHost();
        UBICACION="/sga";

        wv_1 = (WebView)findViewById(R.id.wv_1);

        wv_1.setWebViewClient(new WebViewClient());
        wv_1.getSettings().setJavaScriptEnabled(true);

        String sitio="http://" + DIRECCION_HOST + UBICACION + "/" + PAGINA;
        wv_1.loadUrl(sitio);
    }
}
