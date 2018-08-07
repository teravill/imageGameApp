package teravainen.imagegameapp;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;


public class FragmentAbout extends android.support.v4.app.Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about, container, false);


        //check if file exists, if it does, create the imagepreview
        String pathValue = Environment.getExternalStorageDirectory()+"/test.jpg";;
        File file = new File(pathValue);
        if(file.exists()){
            ImageView myImage = view.findViewById(R.id.lastPicturePreview);
            myImage.setImageBitmap(BitmapFactory.decodeFile(pathValue));
        }

        //Update the labels found text to match the image
        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String labelInfo = mySharedPref.getString("imageLabels", "");
        TextView myLabel = view.findViewById(R.id.labelData);
        myLabel.setText(labelInfo);




        return view;
    }
}
