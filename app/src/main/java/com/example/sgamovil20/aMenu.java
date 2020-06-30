package com.example.sgamovil20;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class aMenu extends AppCompatActivity {

    // Elementos
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
    private static String DIRECCION_HOST = "";
    private static String UBICACION = "";
    private static String API_CERRARSESION = "";
    // --------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_menu);

        SESION=getIntent().getStringExtra("sesion");
        NIDUSUARIO=getIntent().getIntExtra("nidusuario",0);

        DIRECCION_HOST = MainActivity.get_DireccionHost();
        UBICACION=MainActivity.get_Ubicacion();

        API_CERRARSESION=MainActivity.get_CerrarSesion();

    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){
            case R.id.mnu_Recbo:
                fn_Recibo();
                break;

            case R.id.mnu_Inspeccion:
                fn_Inspeccion();
                break;

            case R.id.mnu_Entrada:
                /*
                Toast.makeText(getBaseContext(),"Entrada", Toast.LENGTH_SHORT).show();


                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle("Mensaje");
                builder2.setMessage("Falla en la  conexion con el servidor");
                AlertDialog alertDialog2 = builder2.create();
                alertDialog2.show();
                */


                fn_Entrada();
                break;

            case R.id.mnu_Salida:
                /*
                Toast.makeText(getBaseContext(),"Salida", Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
                builder3.setTitle("Mensaje");
                builder3.setMessage("Falla en la  conexion con el servidor");
                AlertDialog alertDialog3 = builder3.create();
                alertDialog3.show();
                */

                fn_Salidas();
                break;



            case R.id.mnu_Envio:
                /*
                Toast.makeText(getBaseContext(),"Traspaso de Envio", Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder6 = new AlertDialog.Builder(this);
                builder6.setTitle("Mensaje");
                builder6.setMessage("Falla en la  conexion con el servidor");
                AlertDialog alertDialog6 = builder6.create();
                alertDialog6.show();
                */
                fn_Traspaso_Envio();
                break;

            case R.id.mnu_Recepcion:
                /*
                Toast.makeText(getBaseContext(),"Traspaso de Recepcion", Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder7 = new AlertDialog.Builder(this);
                builder7.setTitle("Mensaje");
                builder7.setMessage("Falla en la  conexion con el servidor");
                AlertDialog alertDialog7 = builder7.create();
                alertDialog7.show();
                */
                fn_Traspaso_Envio();

                break;

            case R.id.mnu_Salir:
                fn_Salir();
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    // ***********************************************************************************
    // FUNCIONES

    public void fn_Recibo(){
        Intent iRecibo = new Intent(this, recibodeproductos.class);
        iRecibo.putExtra("sesion",SESION);
        iRecibo.putExtra("nidusuario",NIDUSUARIO);
        startActivity(iRecibo);
    }

    public void fn_Inspeccion(){
        Intent iInspeccion = new Intent(this, inspeccion.class);
        iInspeccion.putExtra("sesion",SESION);
        iInspeccion.putExtra("nidusuario",NIDUSUARIO);
        startActivity(iInspeccion);
    }

    public void fn_Entrada(){
        Intent iInspeccion = new Intent(this, entradaporotrosconceptos.class);
        iInspeccion.putExtra("sesion",SESION);
        iInspeccion.putExtra("nidusuario",NIDUSUARIO);
        startActivity(iInspeccion);
    }

    public void fn_Salidas(){
        Intent iInspeccion = new Intent(this, salidaporotrosconceptos.class);
        iInspeccion.putExtra("sesion",SESION);
        iInspeccion.putExtra("nidusuario",NIDUSUARIO);
        startActivity(iInspeccion);
    }

    public void fn_Traspaso_Envio(){
        Intent iInspeccion = new Intent(this, traspaso_envio.class);
        iInspeccion.putExtra("sesion",SESION);
        iInspeccion.putExtra("nidusuario",NIDUSUARIO);
        startActivity(iInspeccion);
    }

    public void fn_Traspaso_Recepcion(){
        Intent iInspeccion = new Intent(this, traspaso_recepcion.class);
        iInspeccion.putExtra("sesion",SESION);
        iInspeccion.putExtra("nidusuario",NIDUSUARIO);
        startActivity(iInspeccion);
    }


    public void fn_Salir(){
        String sitio="http://" + DIRECCION_HOST + UBICACION + API_CERRARSESION;

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Procesando...");
        progressDialog.show();

        FinalizarSesion(sitio, SESION, NIDUSUARIO);
    }

    // ***********************************************************************************


    // ***********************************************************************************
    // Acceso a las APIS

    // -----------------------------------------------
    // Iniciar Sesion
    private void FinalizarSesion(final String urlWebService, final String l_Sesion, final int l_nIDUsuario) {

        class FinalizarSesion extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                try {
                    FinalizarSesion_Accion(s);
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
        FinalizarSesion getJSON = new FinalizarSesion();
        getJSON.execute();
    }



    private void FinalizarSesion_Accion(String json) throws JSONException {

        try {
            progressDialog.dismiss();

            JSONArray jsonArray = new JSONArray(json);
            String[] stocks = new String[jsonArray.length()];

            int band_Encontrado=0;

            if(jsonArray.length()>0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    if(obj.getString("retorno").equals("TRUE")){

                        band_Encontrado=1;

                        break;
                    }
                }
            }

            if(band_Encontrado==1){


                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Mensaje");
                builder.setMessage("Sesion Cerrada con Exito");
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                Intent iOpcion = new Intent(this, MainActivity.class);
                startActivity(iOpcion);

            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Mensaje");
                builder.setMessage("Error al Cerrar");
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

        } catch (Exception e){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Mensaje");
            builder.setMessage("No existe conexion con el servidor");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }
    // -----------------------------------------------

    // ***********************************************************************************


}
