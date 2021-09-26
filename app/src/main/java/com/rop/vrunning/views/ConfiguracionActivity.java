package com.rop.vrunning.views;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rop.vrunning.R;

import java.io.File;

public class ConfiguracionActivity extends AppCompatActivity {

    ProgressBar progressMemoria;
    TextView txtMemoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        progressMemoria = (ProgressBar) findViewById(R.id.PogressMemoria);
        txtMemoria = (TextView) findViewById(R.id.TxtMemoriaDisponible);

        progressMemoria.setMax((int) phone_storage_total());
        progressMemoria.setProgress((int) phone_storage_used());
        double espacioDisponible=((double)phone_storage_free()/1024000000);
        txtMemoria.setText(String.format("%.2f",espacioDisponible)+" "+"GB disponibles(s)");

    }

    public static long phone_storage_free(){
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long free_memory = stat.getAvailableBlocksLong() * stat.getBlockSizeLong(); //return value is in bytes

        //Bytes
        return free_memory;
    }

    public static long phone_storage_used(){
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long free_memory = (stat.getBlockCountLong() - stat.getAvailableBlocksLong()) * stat.getBlockSizeLong(); //return value is in bytes

        //Bytes
        return free_memory/1024;
    }

    public static long phone_storage_total(){
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long free_memory = stat.getBlockCountLong() * stat.getBlockSizeLong(); //return value is in bytes

        //Bytes
        return free_memory/1024;
    }
}