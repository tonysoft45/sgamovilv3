package com.example.sgamovil20;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // Elementos
    ProgressDialog progressDialog;
    private EditText txt_Usuario;
    private EditText txt_Password;
    private Button bt_Entrar;

    private EditText txt_Folio;

    // Variables de entorno
    private static final String TAG_RETORNO = "resultado";
    private static final String TAG_MSG = "mensaje";
    private static final String TAG_SESION = "sesion";
    private static final String TAG_IDUSUARIO = "nidusuario";

    // --------------------------------------------
    // Variables globales
    private static String SESION = "";
    private static int NIDUSUARIO = 0;
    private static String DIRECCION_HOST = "10.21.120.50:8080";
    //private static String DIRECCION_HOST = "10.20.4.127";
    //private static String DIRECCION_HOST = "192.168.1.94"; // local
    private static String UBICACION = "/sga/api/";

    private static String API_INICIARSESION = "iniciarsesion.api.php";
    private static String API_CERRARSESION = "cerrarsesion.api.php";

    private static String API_PACKINGLIST = "packinglist_consultar_todos.api.php";
    private static String API_PACKINGLIST_DETALLES = "packinglist_deta_consultar.api.php";
    private static String API_PACKINGLIST_GRABAR_RECIBO = "packinglist_estatus.api.php";
    private static String API_PACKINGLIST_VALIDAR_RECIBO = "packinglist_deta_validar.api.php";

    private static String API_INSPECCION = "inspeccion_consultar_todos.api.php";
    private static String API_INSPECCION_DETALLES = "inspeccion_deta_consultar.api.php";
    private static String API_INSPECCION_GRABAR = "inspeccion_estatus.api.php";
    private static String API_INSPECCION_VALIDAR = "inspeccion_deta_validar.api.php";

    // Catalogos
    private static String API_CAT_ESTADOS = "cat_estados_consultar_todos.api.php";

    // Metodos para leer las variables
    public static String get_Sesion(){
        return SESION;
    }

    public static int get_nIDUsuario(){
        return NIDUSUARIO;
    }

    public static String get_DireccionHost(){
        return DIRECCION_HOST;
    }

    public static String get_Ubicacion(){
        return UBICACION;
    }

    // Metodos para leer apis
    public static String get_IniciarSesion(){
        return API_INICIARSESION;
    }

    public static String get_CerrarSesion(){
        return API_CERRARSESION;
    }

    // Recibo
    public static String get_PackingList(){
        return API_PACKINGLIST;
    }

    public static String get_PackignList_Detalles(){
        return API_PACKINGLIST_DETALLES;
    }

    public static String get_PackignList_Grabar_Recibo(){
        return API_PACKINGLIST_GRABAR_RECIBO;
    }

    public static String get_PackignList_Validar_Recibo(){
        return API_PACKINGLIST_VALIDAR_RECIBO;
    }

    // Inspeccion
    public static String get_Inspeccion(){
        return API_INSPECCION;
    }

    public static String get_Inspeccion_Detalles(){
        return API_INSPECCION_DETALLES;
    }

    public static String get_Inspeccion_Grabar(){
        return API_INSPECCION_GRABAR;
    }

    public static String get_Inspeccion_Validar(){
        return API_INSPECCION_VALIDAR;
    }

    public static String get_Cat_Estados(){
        return API_CAT_ESTADOS;
    }
    // --------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        txt_Usuario=(EditText)findViewById(R.id.txt_Usuario);
        txt_Password=(EditText)findViewById(R.id.txt_Password);

        bt_Entrar=(Button)findViewById(R.id.bt_Entrar);

/*
        // Temporal
        Intent iMenu = new Intent(this, recibo_editar_recepcion.class);
        startActivity(iMenu);
 */

    }


    public void bt_Entrar(View view){
        String usuario=txt_Usuario.getText().toString();
        String pass=txt_Password.getText().toString();


        bt_Entrar.setVisibility(View.INVISIBLE);

        usuario=usuario.trim();
        pass=pass.trim();

        if(usuario.length()>0) {
            if (pass.length() > 0) {
                String sitio="http://" + DIRECCION_HOST + UBICACION + API_INICIARSESION;

                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Conectando...");
                progressDialog.show();

                IniciarSesion(sitio, usuario, pass);
            } else {
                bt_Entrar.setVisibility(View.VISIBLE);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Mensaje");
                builder.setMessage("Capture la contraseña");
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        } else {
            bt_Entrar.setVisibility(View.VISIBLE);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Mensaje");
            builder.setMessage("Capture el Usuario");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    /*
    public void bt_Buscar(View view){
        String Folio=txt_Folio.getText().toString();
        int iFolio;

        try {
            iFolio=Integer.parseInt(Folio);

            Intent iRecibo = new Intent(this, recibo.class);
            iRecibo.putExtra("sesion",SESION);
            iRecibo.putExtra("nidusuario",NIDUSUARIO);
            iRecibo.putExtra("folio",iFolio);
            startActivity(iRecibo);

        } catch(Exception e){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Mensaje");
            builder.setMessage("El Folio debe de ser numerico");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }
    */



    // ***********************************************************************************
    // Acceso a las APIS

    // -----------------------------------------------
    // Iniciar Sesion
    private void IniciarSesion(final String urlWebService, final String l_Usuario, final String l_Pass) {

        class IniciarSesion extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                try {
                    IniciarSesion_Accion(s);
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
                    jsonParam.put("usuario", l_Usuario);
                    jsonParam.put("pass", l_Pass);
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
        IniciarSesion getJSON = new IniciarSesion();
        getJSON.execute();
    }



    private void IniciarSesion_Accion(String json) throws JSONException {

        try {
            progressDialog.dismiss();

            JSONArray jsonArray = new JSONArray(json);
            String[] stocks = new String[jsonArray.length()];

            int band_Encontrado=0;

            if(jsonArray.length()>0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    if(obj.getString("retorno").equals("TRUE")){

                        SESION=obj.getString("sesion");
                        NIDUSUARIO=obj.getInt("nidusuario");

                        band_Encontrado=1;

                        break;
                    }
                }
            }

            if(band_Encontrado==1){


                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Mensaje");
                builder.setMessage("El tiempo de conexión a excedido el tiempo permitido");
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                Intent iMenu = new Intent(this, aMenu.class);
                iMenu.putExtra("sesion",SESION);
                iMenu.putExtra("nidusuario",NIDUSUARIO);
                startActivity(iMenu);

                /*
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setTitle("Mensaje");
                builder1.setMessage("Se conectara por WEB APP");
                AlertDialog alertDialog1 = builder1.create();
                alertDialog1.show();

                // Temporal
                Intent iMenu = new Intent(this, web.class);
                startActivity(iMenu);
                 */
            } else {
                bt_Entrar.setVisibility(View.VISIBLE);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Mensaje");
                builder.setMessage("Acceso Denegado");
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

        } catch (Exception e){
            bt_Entrar.setVisibility(View.VISIBLE);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Mensaje");
            builder.setMessage("No existe conexion con el servidor");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            Intent iMenu = new Intent(this, web.class);
            startActivity(iMenu);
        }

    }
    // -----------------------------------------------

    // ***********************************************************************************

}
