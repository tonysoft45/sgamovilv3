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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class recibo_editar_recepcion extends AppCompatActivity {

    // Elementos
    ListView lst_Detalles;
    Button bt_Grabar;
    Button bt_Cancelar;
    Button bt_Escanear;
    TextView lbl_Folio;
    TextView lbl_Fecha;
    EditText txt_Codigo;

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

    int images[]={R.drawable.ic_drag_handle, R.drawable.ic_done};

    // Variables de entorno
    private static final String TAG_RETORNO = "resultado";
    private static final String TAG_MSG = "mensaje";
    private static final String TAG_SESION = "sesion";
    private static final String TAG_IDUSUARIO = "nidusuario";

    private ZXingScannerView escannerView;
    public TextView txt_CodigoLeido;
    public String Valor_Leido="";

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

    private static int bandQR=0;
    // --------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recibo_editar_recepcion);


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

        // Mensaje de carga
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        CargarEncabezado();
        CargarDetalles();

        // Captura del Codigo
        txt_Codigo.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    //Toast.makeText(recibo_editar_recepcion.this, txt_Codigo.getText(), Toast.LENGTH_SHORT).show();
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
    // FUNCIONES
    public void CargarEncabezado(){
        String sitio="http://" + DIRECCION_HOST + UBICACION + API_PACKINGLIST ;
        PackingList_Encabezado(sitio, SESION, NIDUSUARIO, "Folio=" + FOLIO + " and bEstado=0");
    }

    public void CargarDetalles(){
        String sitio="http://" + DIRECCION_HOST + UBICACION + API_PACKINGLIST_DETALLES ;
        PackingList_Detalles(sitio, SESION, NIDUSUARIO, "Folio=" + FOLIO + " and bEstado=0");
    }

    public void Validar_Codigo(){

        String Codigo = txt_Codigo.getText().toString();

        txt_Codigo.setText("");

        if(Codigo.length()>0){

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Validando...");
            progressDialog.show();

            String sitio="http://" + DIRECCION_HOST + UBICACION + API_PACKINGLIST_VALIDAR_RECIBO ;
            PackingList_Validar(sitio, SESION, NIDUSUARIO, FOLIO, Codigo);


        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        String sitio="http://" + DIRECCION_HOST + UBICACION + API_PACKINGLIST_GRABAR_RECIBO ;
        PackingList_Grabar_Recibo(sitio, SESION, NIDUSUARIO, FOLIO);
    }

    public void bt_Cancelar(View view){
        Intent iMenu = new Intent(this, recibodeproductos.class);
        iMenu.putExtra("sesion",SESION);
        iMenu.putExtra("nidusuario",NIDUSUARIO);
        startActivity(iMenu);
    }

    public void bt_Escanner(View v){
        bandQR=1;


        Intent iMenu = new Intent(this, recibo_editar_recepcion_qr.class);
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

            if(rEstatus[posicion].equals("RECIBO")){
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
    private void PackingList_Encabezado(final String urlWebService, final String l_Sesion, final int l_nIDUsuario, final String l_Condicion) {

        class PackingList_Encabezado extends AsyncTask<Void, Void, String> {

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
        PackingList_Encabezado getJSON = new PackingList_Encabezado();
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
    private void PackingList_Grabar_Recibo(final String urlWebService, final String l_Sesion, final int l_nIDUsuario, final String l_Folio) {

        class PackingList_Grabar_Recibo extends AsyncTask<Void, Void, String> {

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
                    jsonParam.put("estatus", "SIN INSPECCION");
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
        PackingList_Grabar_Recibo getJSON = new PackingList_Grabar_Recibo();
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

                Intent iMenu = new Intent(this, recibodeproductos.class);
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
    private void PackingList_Detalles(final String urlWebService, final String l_Sesion, final int l_nIDUsuario, final String l_Condicion) {

        class PackingList_Detalles extends AsyncTask<Void, Void, String> {

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
        PackingList_Detalles getJSON = new PackingList_Detalles();
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

                myAdapter adapter = new myAdapter(this, sCodigos, sProductos, sCajas, sCantidadCajas, sEstatus, images);
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






}

