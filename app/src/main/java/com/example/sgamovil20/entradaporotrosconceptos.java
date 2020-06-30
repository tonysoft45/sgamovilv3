package com.example.sgamovil20;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class entradaporotrosconceptos extends AppCompatActivity {
    WebView wv_1;
    ProgressDialog progressDialog;

    private static String DIRECCION_HOST = "";
    private static String UBICACION = "";
    private static String PAGINA = "/api/entradaporotrosconceptos_detalles.api.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entradaporotrosconceptos);

        DIRECCION_HOST = MainActivity.get_DireccionHost();
        UBICACION="/sga";

        // Mensaje de carga
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        wv_1 = (WebView)findViewById(R.id.wv_1);

        wv_1.setWebViewClient(new WebViewClient());
        wv_1.getSettings().setJavaScriptEnabled(true);

        String sitio="http://" + DIRECCION_HOST + UBICACION + "/" + PAGINA;
        wv_1.loadUrl(sitio);

        progressDialog.dismiss();
    }
}
