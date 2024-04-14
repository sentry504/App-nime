package com.sentry504.scraperprueba;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sentry504.scraperprueba.common.NoconexionFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Toolbar menuHeader;
    ReleasesActivity releasesActivity = new ReleasesActivity();
    Dialog settingsDialog;
    private final static String FILE_NAME ="settings.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, initializationStatus -> {});
        setContentView(R.layout.activity_main);

        saveSettings(estructureSettings("AnimeFLV"), FILE_NAME);
        File archivo = new File(getFilesDir(),"listadoAnimes.txt");
        if (!(archivo.exists() && !archivo.isDirectory())) {
            JSONArray array = new JSONArray();
            saveSettings(array.toString(), "listadoAnimes.txt");
        }

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        menuHeader = findViewById(R.id.menu_toolbar);
        setSupportActionBar(menuHeader);
        menuHeader.setTitleTextColor(Color.WHITE);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if ((networkInfo == null)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container,new NoconexionFragment()).commit();
            Toast.makeText(this, "Parece que no tienes conexion a internet", Toast.LENGTH_LONG).show();
        }
        else{
            getSupportFragmentManager().beginTransaction().replace(R.id.container, releasesActivity).commit();
        }

        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder().setTestDeviceIds(Collections.singletonList("ABCDEF012345"))
                        .build());
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        Bundle args = new Bundle();
        try {
            args.putString("servidor", (String) readListFile(FILE_NAME).get("servidor")) ;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ConnectivityManager connectivityManager;
        NetworkInfo networkInfo;
        switch (item.getItemId()) {
            case (R.id.menuItemRecientes):
                connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                networkInfo = connectivityManager.getActiveNetworkInfo();
                if ((networkInfo == null)) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,new NoconexionFragment()).commit();
                    Toast.makeText(this, "Parece que no tienes conexion a internet", Toast.LENGTH_LONG).show();
                }
                else{
                    ReleasesActivity releasesActivity1 = new ReleasesActivity();
                    releasesActivity1.setArguments(args);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, releasesActivity1).commit();
                }
                return true;
            case (R.id.menuItemLibrary):
                connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                networkInfo = connectivityManager.getActiveNetworkInfo();
                if ((networkInfo == null)) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,new NoconexionFragment()).commit();
                    Toast.makeText(this, "Parece que no tienes conexion a internet", Toast.LENGTH_LONG).show();
                }
                else {
                    LibraryActivity libraryActivity1 = new LibraryActivity();
                    libraryActivity1.setArguments(args);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, libraryActivity1).commit();
                }
                return true;
            case (R.id.menuItemMyList):
                connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                networkInfo = connectivityManager.getActiveNetworkInfo();
                if ((networkInfo == null)) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,new NoconexionFragment()).commit();
                    Toast.makeText(this, "Parece que no tienes conexion a internet", Toast.LENGTH_LONG).show();
                }
                else {
                    MyListaActivity myListaActivity = new MyListaActivity();
                    myListaActivity.setArguments(args);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, myListaActivity).commit();
                }
                return true;
            /*case (R.id.menuItemServer):
                serverDialogSettings();
                return true;*/
        }
        return false;
    }

    public void serverDialogSettings(){
        AtomicReference<String> servidor = new AtomicReference<>("AnimeFLV");
        settingsDialog = new Dialog(this);
        settingsDialog.setContentView(R.layout.dialog_options);
        RadioButton animeflv = settingsDialog.findViewById(R.id.radioButton1);
        RadioButton JKanime = settingsDialog.findViewById(R.id.radioButton2);
        RadioButton monoschinos = settingsDialog.findViewById(R.id.radioButton3);
        settingsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        settingsDialog.show();
        settingsDialog.findViewById(R.id.button).setOnClickListener(v -> {
            if(animeflv.isChecked())
                servidor.set("AnimeFLV");

            if(JKanime.isChecked())
                servidor.set("JKanime");

            if(monoschinos.isChecked()){
                servidor.set("monoschinos");
            }

            saveSettings(estructureSettings(servidor.get()), FILE_NAME);

            Toast.makeText(this, "Se cambio el servidor a " + servidor, Toast.LENGTH_SHORT).show();
            this.settingsDialog.dismiss();
        });
    }

    private String estructureSettings(String servidor) {
        JSONObject json = new JSONObject();
        try{
            json.put("servidor",servidor);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public void saveSettings(String estructure, String file_name){
        try{
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(file_name,MODE_PRIVATE));
            outputStreamWriter.write(estructure);
            outputStreamWriter.flush();
            outputStreamWriter.close();
        } catch (IOException e) {
            Toast.makeText(this, "Could not store file", Toast.LENGTH_SHORT).show();
        }
    }

    public JSONObject readListFile(String file_name) {
        JSONObject retorno = new JSONObject();
        try {
            //Lectura del archivo, procesamiento para conversion a texto
            InputStreamReader inputStreamReader = new InputStreamReader(openFileInput(file_name));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String linea = bufferedReader.readLine();
            StringBuilder contenido = new StringBuilder();
            while (linea != null){
                contenido.append(linea).append("\n");
                linea = bufferedReader.readLine();
            }
            bufferedReader.close();
            inputStreamReader.close();

            //Conversion del contenido del archivo settings.txt a objeto json
            retorno = new JSONObject(contenido.toString());

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return retorno;
    }
}