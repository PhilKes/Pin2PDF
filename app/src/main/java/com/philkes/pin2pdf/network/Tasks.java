package com.philkes.pin2pdf.network;

import android.os.AsyncTask;
import android.util.Log;

import com.philkes.pin2pdf.mock.MockData;
import com.philkes.pin2pdf.model.Pin;

import java.util.ArrayList;
import java.util.List;

public class Tasks {
    public static class GetPinsTask extends AsyncTask<String, Void, List<Pin>> {

        private static final String TAG="GetPinsTask";

        protected List<Pin> doInBackground(String... urls) {
            String urldisplay = urls[0];
            List<Pin> pins= new ArrayList<>();
          /*  try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bmp;*/
            for(int i=0; i<3; i++) {
                pins.addAll(MockData.MockPins);
            }
            return pins;
        }
        protected void onPostExecute(List<Pin> result) {
            Log.d(TAG, String.format("Loaded %d Pins",result.size() ));
        }
    }
}
