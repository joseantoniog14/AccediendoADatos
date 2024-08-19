package com.example.accediendoadatos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity{
    Button buttonCSV,buttonJSON,buttonXML,buttonINSERTAR;
    EditText editTextID,editTextMODELO,editTextMARCA,editTextPRECIO;
    ListView lista;
    static final String SERVIDOR= "http://192.168.3.203c/nube/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonCSV= findViewById(R.id.buttonCSV);
        buttonJSON= findViewById(R.id.buttonJSON);
        buttonXML= findViewById(R.id.buttonXML);
        buttonINSERTAR= findViewById(R.id.buttonINSERTAR);
        editTextID=findViewById(R.id.editTextID);
        editTextMODELO=findViewById(R.id.editTextMODELO);
        editTextMARCA=findViewById(R.id.editTextMARCA);
        editTextPRECIO=findViewById(R.id.editTextPRECIO);
        lista= findViewById(R.id.listview);

        registerForContextMenu(lista);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String reg= (String) lista.getItemAtPosition(i);
                editTextID.setText(reg.split(",")[0].split(": ")[1]);
                editTextMARCA.setText(reg.split(",")[1].split(": ")[1]);
                editTextMODELO.setText(reg.split(",")[2].split(": ")[1]);
                editTextPRECIO.setText(reg.split(",")[3].split(": ")[1]);
            }
        });
        buttonCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DescargarCSV descargarCSV= new DescargarCSV();
                descargarCSV.execute("listadoCSV.php");
            }
        });
        buttonJSON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DescargarJSON descargarJSON= new DescargarJSON();
                descargarJSON.execute("listadoJSON.php");

            }
        });
        buttonXML.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DescargarXML descargarXML= new DescargarXML();
                descargarXML.execute("listadoXML.php");
            }
        });
        buttonINSERTAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                INSERTAR insertar= new INSERTAR();
                insertar.execute("insertar.php?producto="+editTextMARCA.getText()+"&modelo="+editTextMODELO.getText()+"&precio="+editTextPRECIO.getText());
                buttonCSV.callOnClick();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("¿Qué quiere hacer?");
        menu.add(0,v.getId(),0,"Modificar");
        menu.add(0,v.getId(),0,"Borrar");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int posicionLista=info.position;
        if(item.getTitle()=="Modificar"){
            MODIFICAR modificar= new MODIFICAR();
            modificar.execute("actualizar.php?id="+editTextID.getText()+"&producto="+editTextMODELO.getText()+"&modelo="+editTextMARCA.getText()+"&precio="+editTextPRECIO.getText());
            buttonCSV.callOnClick();
        }
        if(item.getTitle()=="Borrar"){
            BORRAR borrar= new BORRAR();
            borrar.execute("borrar.php?id="+editTextID.getText());
            buttonCSV.callOnClick();
        }
        return  true;
    }

    private  class DescargarCSV extends AsyncTask<String,Void,Void>{
        String todo;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            ArrayAdapter<String> adapter;
            List<String> list= new ArrayList<String>();
            String [] lineas = todo.split("\n");
            for (String linea: lineas){
                String[] campos= linea.split(";");
                String dato= "ID: "+campos[0];
                dato+= ", Modelo: "+campos[1];
                dato+= ", Marca: "+campos[2];
                dato+= ", Precio: "+campos[3];
                list.add(dato);
            }
            adapter= new ArrayAdapter<String>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item,list);
            lista.setAdapter(adapter);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String script = strings[0];
            URL url;
            HttpURLConnection httpURLConnection;

            try {
                url=new URL(SERVIDOR+script);
                httpURLConnection=(HttpURLConnection) url.openConnection();
                if (httpURLConnection.getResponseCode()==httpURLConnection.HTTP_OK){
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    String linea="";
                    while ((linea=br.readLine())!=null){
                        todo+=linea+"\n";
                        Thread.sleep(100);
                        publishProgress();
                    }
                    br.close();
                    inputStream.close();
                }else{
                    Toast.makeText(MainActivity.this, "No se pudo conectar a la nube", Toast.LENGTH_SHORT).show();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return  null;
        }
    }
    private  class DescargarJSON extends AsyncTask<String,Void,Void> {
        String todo="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            ArrayAdapter<String> adapter;
            List<String> list = new ArrayList<String>();
            String[] lineas = todo.split("\n");
            JsonParser parser= new JsonParser();
            JsonArray array= parser.parse(todo).getAsJsonArray();
            for (JsonElement linea : array) {
                JsonObject object= linea.getAsJsonObject();
                String dato = "ID: " + object.get("id").getAsString();
                dato += ", Modelo: " + object.get("modelo").getAsString();
                dato += ", Producto: " + object.get("producto").getAsString();
                dato += ", Precio: " + object.get("precio").getAsString();
                list.add(dato);
            }
            adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, list);
            lista.setAdapter(adapter);
            //progressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String script = strings[0];
            URL url;
            HttpURLConnection httpURLConnection;

            try {
                url = new URL(SERVIDOR + script);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                if (httpURLConnection.getResponseCode() == httpURLConnection.HTTP_OK) {
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    String linea = "";
                    while ((linea = br.readLine()) != null) {
                        todo += linea + "\n";
                        Thread.sleep(100);
                        publishProgress();
                    }
                    br.close();
                    inputStream.close();
                } else {
                    Toast.makeText(MainActivity.this, "No se pudo conectar a la nube", Toast.LENGTH_SHORT).show();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private  class DescargarXML extends AsyncTask<String,Void,Void>{
        String todo;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            ArrayAdapter<String> adapter;
            List<String> list= new ArrayList<String>();
            String [] lineas = todo.split("\n");
            for (String linea: lineas){
                list.add(linea);
            }
            adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, list);
            lista.setAdapter(adapter);
            //progressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String script = SERVIDOR+ strings[0];
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db= null;
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document doc = null;
            try {
                doc = db.parse(new URL(script).openStream());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
            Element raiz = doc.getDocumentElement();
            NodeList hijos= raiz.getChildNodes();
            for (int i = 0; i < hijos.getLength(); i++) {
                Node nodo = hijos.item(i);
                if (nodo instanceof Element) {
                    NodeList nietos=nodo.getChildNodes();
                    String[] fila = new String[nietos.getLength()];
                    for (int j = 0; j < nietos.getLength(); j++) {
                        fila[j]= nietos.item(j).getNodeName() +": "+nietos.item(j).getTextContent()+", ";
                        todo+=fila[j];
                    }
                    todo+="\n";
                }
            }
            return  null;
        }
    }
    private  class INSERTAR extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String script = strings[0];

            URL url = null;
            try {
                url = new URL(SERVIDOR + script);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection httpURLConnection;
            try {
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);

                PrintStream ps = new PrintStream(httpURLConnection.getOutputStream());

                ps.print("modelo="+editTextMODELO.getText());
                ps.print("&marca="+editTextMARCA.getText());
                ps.print("&precio="+editTextPRECIO.getText());
                InputStream inputStream = httpURLConnection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private  class MODIFICAR extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String script = strings[0];
            URL url = null;
            try {
                url = new URL(SERVIDOR + script);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection httpURLConnection;
            try {
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private  class BORRAR extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String script = strings[0];

            URL url = null;
            try {
                url = new URL(SERVIDOR + script);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection httpURLConnection;
            try {
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}