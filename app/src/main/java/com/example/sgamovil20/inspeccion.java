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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class inspeccion extends AppCompatActivity {

    // Elementos
    ListView listView;
    TextView txt_Folio;
    ProgressDialog progressDialog;

    // Variables locales
    String sFolios[];
    String sFechas[];
    String sArchivos[];
    int contRegistros=0;

    int images[]={R.drawable.ic_chat};

    // Variables de entorno
    private static final String TAG_RETORNO = "resultado";
    private static final String TAG_MSG = "mensaje";
    private static final String TAG_SESION = "sesion";
    private static final String TAG_IDUSUARIO = "nidusuario";

    // --------------------------------------------
    // Variables globales
    private static String SESION = "";
    private static int NIDUSUARIO = 0;
    private static int FOLIO =0;

    private static String DIRECCION_HOST = "";
    private static String UBICACION = "";
    private static String API_PACKINGLIST ="";
    private static String API_INSPECCION ="";
    // --------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspeccion);

        // Elementos
        listView = findViewById(R.id.lst_Detalles);
        txt_Folio = findViewById(R.id.txt_Folio);


        // Get Datos
        SESION=getIntent().getStringExtra("sesion");
        NIDUSUARIO=getIntent().getIntExtra("nidusuario",0);
        FOLIO=getIntent().getIntExtra("folio", 0);

        DIRECCION_HOST = MainActivity.get_DireccionHost();
        UBICACION=MainActivity.get_Ubicacion();

        API_PACKINGLIST =MainActivity.get_PackingList();
        API_INSPECCION =MainActivity.get_Inspeccion();

        // Mensaje de carga
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        // Cargar los datos de los packinglist para recibo
        String sitio="http://" + DIRECCION_HOST + UBICACION + API_INSPECCION ;
        PackingListInspeccion(sitio, SESION, NIDUSUARIO, "");


    }

    // ***********************************************************************************
    // Botones
    public void bt_Buscar(View view){
        String Folio=txt_Folio.getText().toString();
        int iFolio;

        try {
            iFolio=Integer.parseInt(Folio);

            Buscar(iFolio);

        } catch(Exception e){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Mensaje");
            builder.setMessage("El Folio debe de ser numerico");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public void bt_Actualizar(View view){
        String sitio="http://" + DIRECCION_HOST + UBICACION + API_INSPECCION;
        PackingListInspeccion(sitio, SESION, NIDUSUARIO, "");
    }

    public void bt_Regresar(View view){
        Intent iMenu = new Intent(this, aMenu.class);
        iMenu.putExtra("sesion",SESION);
        iMenu.putExtra("nidusuario",NIDUSUARIO);
        startActivity(iMenu);
    }

    // ***********************************************************************************

    // ***********************************************************************************
    // Funciones
    public void Buscar(int iFolio){
        String sitio="http://" + DIRECCION_HOST + UBICACION + API_INSPECCION ;
        PackingListInspeccion(sitio, SESION, NIDUSUARIO, "Folio=" + iFolio + " and bEstado=0");
    }

    public void Inspeccion_Codigos(String Folio){
        Intent iRecibo_Editar = new Intent(this, inspeccion_editar.class);
        iRecibo_Editar.putExtra("sesion",SESION);
        iRecibo_Editar.putExtra("nidusuario",NIDUSUARIO);
        iRecibo_Editar.putExtra("folio",Folio);
        startActivity(iRecibo_Editar );
    }
    // ***********************************************************************************


    // ***********************************************************************************
    // Adaptadores para enlazar los renglones
    class myAdapter extends ArrayAdapter<String> {

        Context context;
        String rFolios[];
        String rFechas[];
        String rArchivos[];
        int rImgs[];

        myAdapter(Context c, String folios[], String fechas[], String archivos[], int imgs[]){
            super(c, R.layout.row_inspeccion, R.id.lbl_Folio, folios);

            this.context = c;
            this.rFolios = folios;
            this.rFechas= fechas;
            this.rImgs=imgs;
            this.rArchivos=archivos;
        }


        public View getView(int posicion, @Nullable View convertView, @NonNull ViewGroup parent){
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row_inspeccion, parent, false );

            ImageView images = row.findViewById(R.id.image);
            TextView l_Folio = row.findViewById(R.id.lbl_Folio);
            TextView l_Fecha = row.findViewById(R.id.lbl_Fecha);
            TextView l_Archivo = row.findViewById(R.id.lbl_Archivo);

            images.setImageResource(rImgs[0]);
            l_Folio.setText("Folio:" + rFolios[posicion]);
            l_Fecha.setText(rFechas[posicion]);
            l_Archivo.setText(rArchivos[posicion]);

            return row;

        }
    }

    // ***********************************************************************************

    // ***********************************************************************************
    // Acceso a las APIS

    // -----------------------------------------------
    // Iniciar Sesion
    private void PackingListInspeccion(final String urlWebService, final String l_Sesion, final int l_nIDUsuario, final String l_Condicion) {

        class PackingListInspeccion extends AsyncTask<Void, Void, String> {

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
        PackingListInspeccion getJSON = new PackingListInspeccion();
        getJSON.execute();
    }



    private void CargarRegistros(String json) throws JSONException {

        try {
            progressDialog.dismiss();

            JSONArray jsonArray = new JSONArray(json);
            String[] stocks = new String[jsonArray.length()];

            int band_Encontrado=0;
            String Folio="";
            String Fecha="";
            String Archivo="";

            contRegistros=0;



            if(jsonArray.length()>0) {

                sFolios=new String[jsonArray.length()];
                sFechas=new String[jsonArray.length()];
                sArchivos=new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    if(obj.getString("retorno").equals("TRUE")){

                        Folio="";
                        Fecha="";
                        Archivo="";

                        // carga los registros
                        Folio=obj.getString("Folio");
                        Fecha=obj.getString("Fecha");
                        Archivo=obj.getString("Archivo");

                        sFolios[contRegistros]=Folio;
                        sFechas[contRegistros]=Fecha;
                        sArchivos[contRegistros]=Archivo;
                        contRegistros++;

                        if(contRegistros>100){
                            break;
                        }
                    }
                }
            }

            if(contRegistros>0){

                inspeccion.myAdapter adapter = new inspeccion.myAdapter(this, sFolios, sFechas, sArchivos, images);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        int i;

                        for(i=0;i<sFolios.length;i++){

                            if(position==i){
                                Inspeccion_Codigos(sFolios[i]);
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

    // ***********************************************************************************

}
