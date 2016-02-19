package net.huseyinsekmenoglu.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.huseyinsekmenoglu.database.Database;
import net.huseyinsekmenoglu.database.Vakit;
import net.huseyinsekmenoglu.namazvaktim.ApiConnect;
import net.huseyinsekmenoglu.namazvaktim.Helpers;
import net.huseyinsekmenoglu.namazvaktim.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class tVakit extends Fragment {
    View myInflatedView;
    Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myInflatedView = inflater.inflate(R.layout.fragment_home_vakit, container, false);
        mContext = container.getContext();
        RefreshVakit();
        return myInflatedView;
    }

    //refresh vakit
    public void RefreshVakit() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Database db = new Database(mContext);
        Helpers helpers = new Helpers(this.getActivity());
        //get textviews
        TextView txtImsak = (TextView) myInflatedView.findViewById(R.id.txtImsak),
                txtGunes = (TextView) myInflatedView.findViewById(R.id.txtGunes),
                txtOgle = (TextView) myInflatedView.findViewById(R.id.txtOgle),
                txtIkindi = (TextView) myInflatedView.findViewById(R.id.txtIkindi),
                txtAksam = (TextView) myInflatedView.findViewById(R.id.txtAksam),
                txtYatsi = (TextView) myInflatedView.findViewById(R.id.txtYatsi);
        //variables
        int town = Integer.parseInt(prefs.getString(getString(R.string.prefTownID), ""));
        SimpleDateFormat dfDate = new SimpleDateFormat(getString(R.string.dateFormat), Locale.ENGLISH);
        String today = dfDate.format((new Date()).getTime());//Returns 15/10/2012
        Vakit tablo = db.getVakit(town, today);
        //if no vakit found update again
        if (tablo.GetId() == 0) {


            //get preferences
            String countryID = prefs.getString(mContext.getString(R.string.prefCountryID), mContext.getString(R.string.defaultUlkeID)),
                    cityID = prefs.getString(mContext.getString(R.string.prefCityID), mContext.getString(R.string.defaultSehirID)),
                    townID = prefs.getString(mContext.getString(R.string.prefTownID), mContext.getString(R.string.defaultIlceID)),
                    updateLink;
            if (countryID.equals(cityID)) updateLink = countryID + "/" + townID;
            else updateLink = countryID + "/" + cityID + "/" + townID;
            AsyncTask<String, Integer, String> str_result = new ApiConnect(this.getActivity()).execute(updateLink, townID);


            tablo = db.getVakit(town, today);
        }
        //write values of namaz vakits
        txtImsak.setText(tablo.GetImsak());
        txtGunes.setText(tablo.GetGunes());
        txtOgle.setText(tablo.GetOgle());
        txtIkindi.setText(tablo.GetIkindi());
        txtAksam.setText(tablo.GetAksam());
        txtYatsi.setText(tablo.GetYatsi());
    }
}