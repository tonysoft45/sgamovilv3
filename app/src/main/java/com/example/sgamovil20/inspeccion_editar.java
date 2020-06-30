package com.example.sgamovil20;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class inspeccion_editar extends AppCompatActivity {

    // Elementos
    ListView lst_Detalles;
    Button bt_Grabar;
    Button bt_Cancelar;
    Button bt_Escanear;
    TextView lbl_Folio;
    TextView lbl_Fecha;
    EditText txt_Codigo;
    Spinner cb_Estado;

    ProgressDialog progressDialog;

    // Variables locales
    String sFolios[];
    String sFecha[];

    String sCodigos[];
    String sProductos[];
    String sCajas[];
    String sCantidadCajas[];
    String sEstatus[];
    int contRegistros=0;

    String snIDCat_Estados[];
    String sEstados[];
    int contEstados=0;

    int images[]={R.drawable.ic_drag_handle, R.drawable.ic_done};

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

    private static String API_INSPECCION="";
    private static String API_INSPECCION_DETALLES="";
    private static String API_INSPECCION_GRABAR ="";
    private static String API_INSPECCION_VALIDAR ="";
    private static String API_CAT_ESTADOS ="";

    private static int bandQR=0;

    private ZBarScannerView mScannerView;
    // --------------------------------------------

    // ***********************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspeccion_editar);

        // Inicializacion
        bandQR=0;

        // Elementos
        bt_Grabar = findViewById(R.id.bt_Grabar);
        bt_Cancelar = findViewById(R.id.bt_Cancelar);
        bt_Escanear = findViewById(R.id.bt_Escanear);
        lbl_Folio = findViewById(R.id.lbl_Folio);
        lbl_Fecha = findViewById(R.id.lbl_Fecha);
        txt_Codigo = findViewById(R.id.txt_Codigo);
        lst_Detalles = findViewById(R.id.lst_Detalles);
        cb_Estado = findViewById(R.id.cb_Estado);

        /*
        String[] arraySpinner = new String[] {
                "1", "2", "3", "4", "5", "6", "7"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cb_Estado.setAdapter(adapter);
         */

        // Get Datos
        SESION=getIntent().getStringExtra("sesion");
        NIDUSUARIO=getIntent().getIntExtra("nidusuario",0);
        FOLIO=getIntent().getStringExtra("folio");

        DIRECCION_HOST = MainActivity.get_DireccionHost();
        UBICACION=MainActivity.get_Ubicacion();

        API_INSPECCION=MainActivity.get_Inspeccion();
        API_INSPECCION_DETALLES=MainActivity.get_Inspeccion_Detalles();
        API_INSPECCION_GRABAR=MainActivity.get_Inspeccion_Grabar();
        API_INSPECCION_VALIDAR=MainActivity.get_Inspeccion_Validar();
        API_CAT_ESTADOS=MainActivity.get_Cat_Estados();

        // Mensaje de carga
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        CargarEncabezado();
        CargarDetalles();

        CargarEstados();

        // Captura del Codigo
        txt_Codigo.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    Validar_Codigo();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(bandQR==1){
            // Carga los detalles
            CargarDetalles();
            bandQR=0;
        }
    }
    // ***********************************************************************************

    // ***********************************************************************************
    // FUNCIONES
    public void CargarEncabezado(){
        String sitio="http://" + DIRECCION_HOST + UBICACION + API_INSPECCION ;
        Inspeccion_Encabezado(sitio, SESION, NIDUSUARIO, "Folio=" + FOLIO + " and bEstado=0");
    }

    public void CargarDetalles(){
        String sitio="http://" + DIRECCION_HOST + UBICACION + API_INSPECCION_DETALLES ;
        Inspeccion_Detalles(sitio, SESION, NIDUSUARIO, "Folio=" + FOLIO + " and bEstado=0");
    }

    public void CargarEstados(){
        String sitio="http://" + DIRECCION_HOST + UBICACION + API_CAT_ESTADOS ;
        Cat_Estados(sitio, SESION, NIDUSUARIO, "bEstado=0");
    }

    public void Validar_Codigo(){

        String Codigo = txt_Codigo.getText().toString();

        txt_Codigo.setText("");

        if(Codigo.length()>0){

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Validando...");
            progressDialog.show();

            String sitio="http://" + DIRECCION_HOST + UBICACION + API_INSPECCION_VALIDAR;
            Inspeccion_Validar(sitio, SESION, NIDUSUARIO, FOLIO, Codigo);

        } else {

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Mensaje");
            builder.setMessage("Capture el Codigo");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
    }
    // ***********************************************************************************

    // ***********************************************************************************
    // BOTONES
    public void bt_Grabar(View view) {
        // Mensaje de carga
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Grabando...");
        progressDialog.show();

        bt_Grabar.setVisibility(View.INVISIBLE);
        String sitio="http://" + DIRECCION_HOST + UBICACION + API_INSPECCION_GRABAR;
        Inspeccion_Grabar(sitio, SESION, NIDUSUARIO, FOLIO);
    }

    public void bt_Cancelar(View view){
        Intent iMenu = new Intent(this, inspeccion.class);
        iMenu.putExtra("sesion",SESION);
        iMenu.putExtra("nidusuario",NIDUSUARIO);
        startActivity(iMenu);
    }

    public void bt_Escanner(View v){
        bandQR=1;


        Intent iMenu = new Intent(this, inspeccion_editar_qr.class);
        iMenu.putExtra("sesion",SESION);
        iMenu.putExtra("nidusuario",NIDUSUARIO);
        iMenu.putExtra("folio", FOLIO);
        startActivity(iMenu);


    }
    // ***********************************************************************************

    // ***********************************************************************************
    // Adaptadores para enlazar los renglones
    class myAdapter extends ArrayAdapter<String> {

        Context context;
        String rCodigos[];
        String rProductos[];
        String rCajas[];
        String rCantidadCajas[];
        String rEstatus[];
        int rImgs[];

        myAdapter(Context c, String codigos[], String productos[], String cajas[], String cantidadcajas[], String estatus[], int imgs[]){
            super(c, R.layout.row_recepcion, R.id.lbl_Codigo, codigos);

            this.context = c;
            this.rCodigos = codigos;
            this.rProductos= productos;
            this.rCajas= cajas;
            this.rImgs=imgs;
            this.rCantidadCajas=cantidadcajas;
            this.rEstatus=estatus;
        }


        public View getView(int posicion, @Nullable View convertView, @NonNull ViewGroup parent){
            int SELECTED_COLOR = 0xFF8BC34A;

            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row_recepcion, parent, false );

            ImageView images = row.findViewById(R.id.image);
            TextView l_Codigo = row.findViewById(R.id.lbl_Codigo);
            TextView l_Producto = row.findViewById(R.id.lbl_Producto);
            TextView l_Cajas = row.findViewById(R.id.lbl_Cajas);
            TextView l_Piezas = row.findViewById(R.id.lbl_Piezas);

            l_Codigo.setText("Codigo:" + rCodigos[posicion]);
            l_Producto.setText(rProductos[posicion]);
            l_Cajas.setText(rCajas[posicion]);
            l_Piezas.setText(rCantidadCajas[posicion]);

            if(rEstatus[posicion].equals("INSPECCIONADO")){
                l_Codigo.setBackgroundColor(SELECTED_COLOR);
                l_Producto.setBackgroundColor(SELECTED_COLOR);
                l_Cajas.setBackgroundColor(SELECTED_COLOR);
                l_Piezas.setBackgroundColor(SELECTED_COLOR);
                images.setImageResource(rImgs[1]);
            } else {
                images.setImageResource(rImgs[0]);
            }

            return row;

        }

    }
    // ***********************************************************************************


    // ***********************************************************************************
    // Acceso a las APIS


    // -----------------------------------------------
    // ENCABEZADO
    private void Inspeccion_Encabezado(final String urlWebService, final String l_Sesion, final int l_nIDUsuario, final String l_Condicion) {

        class Inspeccion_Encabezado extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                try {
                    CargarEncabezado(s);
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
                    jsonParam.put("condicion", l_Condicion);
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
        Inspeccion_Encabezado getJSON = new Inspeccion_Encabezado();
        getJSON.execute();
    }

    private void CargarEncabezado(String json) throws JSONException {

        try {
            //progressDialog.dismiss();

            JSONArray jsonArray = new JSONArray(json);
            String[] stocks = new String[jsonArray.length()];

            int band_Encontrado=0;
            String Folio="";
            String Fecha="";

            contRegistros=0;

            if(jsonArray.length()>0) {

                sFecha=new String[jsonArray.length()];
                sFolios=new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    if(obj.getString("retorno").equals("TRUE")){

                        Folio="";
                        Fecha="";

                        // carga los registros
                        Folio=obj.getString("Folio");
                        Fecha=obj.getString("Fecha");

                        band_Encontrado=1;

                        break;

                    }
                }
            }

            if(band_Encontrado>0){
                lbl_Folio.setText("Folio: " + Folio);
                lbl_Fecha.setText("Fecha: " + Fecha);

            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Mensaje");
                builder.setMessage("No existen el Folio para la Recepcion");
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

    // -----------------------------------------------
    // GRABAR RECIBO
    private void Inspeccion_Grabar(final String urlWebService, final String l_Sesion, final int l_nIDUsuario, final String l_Folio) {

        class Inspeccion_Grabar extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                try {
                    Finalizar_Grabacion(s);
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
        Inspeccion_Grabar getJSON = new Inspeccion_Grabar();
        getJSON.execute();
    }



    private void Finalizar_Grabacion(String json) throws JSONException {

        try {
            progressDialog.dismiss();

            JSONArray jsonArray = new JSONArray(json);
            String[] stocks = new String[jsonArray.length()];

            int band_Encontrado=0;
            String MSG="";

            contRegistros=0;

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

            if(band_Encontrado>0){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Mensaje");
                builder.setMessage("GRABACION EXITOSA");
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                Intent iMenu = new Intent(this, inspeccion.class);
                iMenu.putExtra("sesion",SESION);
                iMenu.putExtra("nidusuario",NIDUSUARIO);
                startActivity(iMenu);

            } else {

                bt_Grabar.setVisibility(View.VISIBLE);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Mensaje");
                builder.setMessage(MSG);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }

        } catch (Exception e){

            bt_Grabar.setVisibility(View.VISIBLE);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Mensaje");
            builder.setMessage("No existe conexion con el servidor");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }
    // -----------------------------------------------


    // -----------------------------------------------
    // Cargar Detalles
    private void Inspeccion_Detalles(final String urlWebService, final String l_Sesion, final int l_nIDUsuario, final String l_Condicion) {

        class Inspeccion_Detalles extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                try {
                    CargarRegistros(s);
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
                    jsonParam.put("condicion", l_Condicion);
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
        Inspeccion_Detalles getJSON = new Inspeccion_Detalles();
        getJSON.execute();
    }



    private void CargarRegistros(String json) throws JSONException {

        try {
            progressDialog.dismiss();

            JSONArray jsonArray = new JSONArray(json);
            String[] stocks = new String[jsonArray.length()];

            int band_Encontrado=0;
            String Codigo="";
            String Producto="";
            String Cajas="";
            String Piezas="";
            String Estatus="";

            contRegistros=0;



            if(jsonArray.length()>0) {

                sCodigos=new String[jsonArray.length()];
                sProductos=new String[jsonArray.length()];
                sCajas=new String[jsonArray.length()];
                sCantidadCajas=new String[jsonArray.length()];
                sEstatus=new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    if(obj.getString("retorno").equals("TRUE")){

                        Codigo="";
                        Producto="";
                        Cajas="";
                        Piezas="";
                        Estatus="";

                        // carga los registros
                        Codigo=obj.getString("Codigo");
                        Producto=obj.getString("Producto");
                        Cajas=obj.getString("Cajas");
                        Piezas=obj.getString("CantidadCaja");
                        Estatus=obj.getString("Estatus");

                        sCodigos[contRegistros]=Codigo;
                        sProductos[contRegistros]=Producto;
                        sCajas[contRegistros]=Cajas;
                        sCantidadCajas[contRegistros]=Piezas;
                        sEstatus[contRegistros]=Estatus;
                        contRegistros++;

                        if(contRegistros>100){
                            break;
                        }
                    }
                }
            }

            if(contRegistros>0){

                inspeccion_editar.myAdapter adapter = new inspeccion_editar.myAdapter(this, sCodigos, sProductos, sCajas, sCantidadCajas, sEstatus, images);
                lst_Detalles.setAdapter(adapter);

                lst_Detalles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        int i;


                        for(i=0;i<sCodigos.length;i++){

                            if(position==i){

                                // Hacer algo con la seleccion


                            }
                        }
                    }
                });

            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Mensaje");
                builder.setMessage("No existen Registros con esa condicion");
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

    // -----------------------------------------------
    // VALIDAR
    private void Inspeccion_Validar(final String urlWebService, final String l_Sesion, final int l_nIDUsuario, final String l_Folio, final String l_Codigo) {

        class Inspeccion_Validar extends AsyncTask<Void, Void, String> {

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
        Inspeccion_Validar getJSON = new Inspeccion_Validar();
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

            }  else {
                CargarDetalles();
                bandQR=0;
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

    // -----------------------------------------------
    // CATALOGO DE ESTADOS
    private void Cat_Estados(final String urlWebService, final String l_Sesion, final int l_nIDUsuario, final String l_Condicion) {

        class Cat_Estados extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                try {
                    Cargar_CatEstados(s);
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
                    jsonParam.put("condicion", l_Condicion);
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
        Cat_Estados getJSON = new Cat_Estados();
        getJSON.execute();
    }

    private void Cargar_CatEstados(String json) throws JSONException {

        try {
            //progressDialog.dismiss();

            JSONArray jsonArray = new JSONArray(json);
            String[] stocks = new String[jsonArray.length()];

            String nIDCat_Estado="";
            String Estado="";

            contEstados=0;

            if(jsonArray.length()>0) {

                snIDCat_Estados=new String[jsonArray.length()];
                sEstados=new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    if(obj.getString("retorno").equals("TRUE")){

                        nIDCat_Estado="";
                        Estado="";

                        // carga los registros
                        nIDCat_Estado=obj.getString("nIDCat_Estado");
                        Estado=obj.getString("Estado");

                        snIDCat_Estados[contEstados]=nIDCat_Estado;
                        sEstados[contEstados]=Estado;

                        contEstados++;

                        if(contEstados>100){
                            break;
                        }

                    }
                }
            }

            if(contEstados>0){

                // Carga los estados
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, sEstados);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                cb_Estado.setAdapter(adapter);

            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Mensaje");
                builder.setMessage("Catalogo de Estados NO disponible");
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



}
