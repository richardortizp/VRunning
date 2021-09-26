package com.rop.vrunning.views;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rop.vrunning.R;

import java.io.File;

public class ConfigurationFragment extends DialogFragment {
    private static final String TAG = ConfigurationFragment.class.getName() + "";
    // componentes de capacidad
    ProgressBar progressMemoria;
    TextView txtMemoria;
    String intentName;

    public ConfigurationFragment() {}

    public static ConfigurationFragment newInstance(String title) {
        ConfigurationFragment frag = new ConfigurationFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_configuration, container, false);
        progressMemoria = (ProgressBar) view.findViewById(R.id.PogressMemoria);
        txtMemoria = (TextView) view.findViewById(R.id.TxtMemoriaDisponible);

        progressMemoria.setMax((int) phone_storage_total());
        progressMemoria.setProgress((int) phone_storage_used());
        double espacioDisponible=((double)phone_storage_free()/1024000000);
        txtMemoria.setText(String.format("%.2f",espacioDisponible)+" "+"GB disponibles(s)");

        return view;
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