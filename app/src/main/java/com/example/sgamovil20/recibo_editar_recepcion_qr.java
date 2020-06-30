package com.example.sgamovil20;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class recibo_editar_recepcion_qr extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView escannerView;

    public TextView txt_CodigoLeido;

    public String Valor_Leido="";

    ProgressDialog progressDialog;

    // Variables de entorno
    private static final String TAG_RETORNO = "resultado";
    private static final String TAG_MSG = "mensaje";
    private static final String TAG_SESION = "sesion";
    private static final String TAG_IDUSUARIO = "nidusuario";

    // --------------------------------------------
    // Variables globales
    private static String SESION = "";
    private static int NIDUSUARIO = 0;
    private static String FOLIO ="0";

    private static String DIRECCION_HOST = "";
    private static String UBICACION = "";
    private static String API_RECEPCION="";
    private static String API_PACKINGLIST="";
    private static String API_PACKINGLIST_DETALLES="";
    private static String API_PACKINGLIST_GRABAR_RECIBO ="";
    private static String API_PACKINGLIST_VALIDAR_RECIBO ="";


    // --------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recibo_editar_recepcion_qr);

        // Get Datos
        SESION=getIntent().getStringExtra("sesion");
        NIDUSUARIO=getIntent().getIntExtra("nidusuario",0);
        FOLIO=getIntent().getStringExtra("folio");

        DIRECCION_HOST = MainActivity.get_DireccionHost();
        UBICACION=MainActivity.get_Ubicacion();

        API_PACKINGLIST=MainActivity.get_PackingList();
        API_PACKINGLIST_DETALLES=MainActivity.get_PackignList_Detalles();
        API_PACKINGLIST_GRABAR_RECIBO=MainActivity.get_PackignList_Grabar_Recibo();
        API_PACKINGLIST_VALIDAR_RECIBO=MainActivity.get_PackignList_Validar_Recibo();

        fn_Escanear();

    }

    protected void onStop(){
      super.onStop();
    }


    public void fn_Escanear(){
        escannerView = new ZXingScannerView(this);
        setContentView(escannerView);
        escannerView.setResultHandler(this);
        escannerView.startCamera();
    }

    @Override
    public void handleResult(Result result) {
        Log.v("RESULTADO:" ,result.getText());

        Valor_Leido=result.getText();
        escannerView.resumeCameraPreview(this);

        escannerView.stopCamera();
        escannerView.stopCameraPreview();
        //escannerView=null;

        // Valida
        if(Valor_Leido.length()>0){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Validando...");
            progressDialog.show();

            String sitio="http://" + DIRECCION_HOST + UBICACION + API_PACKINGLIST_VALIDAR_RECIBO ;
            PackingList_Validar(sitio, SESION, NIDUSUARIO, FOLIO, Valor_Leido);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Mensaje");
            builder.setMessage("CODIGO NO LEIDO");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            escannerView.startCamera();
            escannerView.resumeCameraPreview(this);
        }


        //finish();
    }

    // ***********************************************************************************
    // Acceso a las APIS


    // -----------------------------------------------
    // ENCABEZADO
    private void PackingList_Validar(final String urlWebService, final String l_Sesion, final int l_nIDUsuario, final String l_Folio, final String l_Codigo) {

        class PackingList_Validar extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                try {
                    Validar(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {

                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Accept","application/json");
                    con.setDoOutput(true);
                    con.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("sesion", l_Sesion);
                    jsonParam.put("nidusuario", l_nIDUsuario);
                    jsonParam.put("folio", l_Folio);
                    jsonParam.put("codigo", l_Codigo);
                    String message = jsonParam.toString();

                    DataOutputStream os = new DataOutputStream(con.getOutputStream());
                    os.write(message.getBytes());
                    os.flush();
                    os.close();

                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;

                }
            }
        }
        PackingList_Validar getJSON = new PackingList_Validar();
        getJSON.execute();
    }



    private void Validar(String json) throws JSONException {

        try {

            progressDialog.dismiss();

            JSONArray jsonArray = new JSONArray(json);
            String[] stocks = new String[jsonArray.length()];

            int band_Encontrado=0;
            String MSG="";

            if(jsonArray.length()>0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    if(obj.getString("retorno").equals("TRUE")){
                        band_Encontrado=1;
                        break;
                    } else {

                        if(obj.getString("retorno").equals("FALSE")) {
                            MSG=obj.getString("msg");
                            break;
                        }
                    }
                }
            }

            if(band_Encontrado<=0){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Mensaje");
                builder.setMessage("CODIGO NO ENCONTRADO");
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                escannerView.startCamera();
                escannerView.resumeCameraPreview(this);

            } else {
                finish();
            }

        } catch (Exception e){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Mensaje");
            builder.setMessage("No existe conexion con el servidor");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            escannerView.startCamera();
            escannerView.resumeCameraPreview(this);
        }

    }
    // -----------------------------------------------

    // ***********************************************************************************
}


