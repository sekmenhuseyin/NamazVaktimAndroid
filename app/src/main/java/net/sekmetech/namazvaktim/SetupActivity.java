package net.sekmetech.namazvaktim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import net.sekmetech.database.Database;
import net.sekmetech.database.Ilce;
import net.sekmetech.database.Language;
import net.sekmetech.database.LanguageChild;
import net.sekmetech.database.Sehir;
import net.sekmetech.database.Ulke;
import net.sekmetech.helpers.ExpandableListAdapter;
import net.sekmetech.helpers.ExpandableListWithImageAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by huseyin.sekmenoglu on 8.2.2016.
 * http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
 * http://emreardic.blogspot.com.tr/2014/08/diyanet-ezan-vakitleri-api.html
 */
public class SetupActivity extends AppCompatActivity {
    TextView title;
    //expandable listview adapter
    private ExpandableListWithImageAdapter ExpAdapter;
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    //parent and child lists
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    //usefull vars
    private SharedPreferences prefs;
    private int lastExpandedPosition = -1;
    private String nameCountry, nameCity, nameTown;
    private Database db = new Database(this);
    private String[] SehirliUlkeler, Diller, DilKodlari;
    private int[] images = {R.drawable.lang_tr, R.drawable.lang_en};
    private Boolean expWithImage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        // SharedPreferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Resources res = getResources();
        SehirliUlkeler = res.getStringArray(R.array.SehirliUlkeler);
        Diller = res.getStringArray(R.array.Diller);
        DilKodlari = res.getStringArray(R.array.DilKodlari);
        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExpSetup);
        title = (TextView) findViewById(R.id.lblListHeader);
        // preparing list data
        prepareListLanguageData();
        expListView.expandGroup(0);
        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastExpandedPosition != -1 && groupPosition != lastExpandedPosition)
                    expListView.collapseGroup(lastExpandedPosition);
                lastExpandedPosition = groupPosition;
            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (expWithImage) {
                    //if language clicked
                    expWithImage = false;
                    setLocale(DilKodlari[childPosition]);
                    //get country names
                    prepareListData("", "");
                    expListView.expandGroup(0);

                } else if (listDataHeader.get(groupPosition).equals(getString(R.string.Country))) {
                    // if country clicked
                    nameCountry = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                    prepareListData(nameCountry, "");
                    expListView.collapseGroup(0);
                    expListView.expandGroup(1);

                } else if (listDataHeader.get(groupPosition).equals(getString(R.string.City))) {
                    //if city clicked
                    nameCity = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                    if (nameCountry.equals(SehirliUlkeler[0]) || nameCountry.equals(SehirliUlkeler[1]) || nameCountry.equals(SehirliUlkeler[2])) {
                        prepareListData(nameCountry, nameCity);
                        expListView.collapseGroup(1);
                        expListView.expandGroup(2);
                    } else {
                        nameTown = nameCity;
                        nameCity = nameCountry;
                        expListView.collapseGroup(1);
                        SelectCity();
                    }

                } else if (listDataHeader.get(groupPosition).equals(getString(R.string.Town))) {
                    //if town clicked
                    expListView.collapseGroup(2);
                    nameTown = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                    SelectCity();
                }
                return false;
            }
        });
    }

    /*prepare language list for expandable listview*/
    private void prepareListLanguageData() {
        listDataHeader = new ArrayList<>();
        listDataHeader.add(getString(R.string.lang));
        //diller tanımlanır
        ArrayList<Language> list = new ArrayList<>();
        ArrayList<LanguageChild> ch_list = new ArrayList<>();
        //dillerin adları ve resimleri diziye atılır
        for (int j = 0; images.length > j; j++) {
            LanguageChild ch = new LanguageChild();
            ch.setName(Diller[j]);
            ch.setImage(images[j]);
            ch_list.add(ch);
        }
        //listview için son hazırlık
        Language lang = new Language();
        lang.setName(getString(R.string.lang));
        lang.setItems(ch_list);
        list.add(lang);
        //listview
        ExpAdapter = new ExpandableListWithImageAdapter(this, list);
        expListView.setAdapter(ExpAdapter);
    }

    /*
     * Preparing the list data
     */
    private void prepareListData(String country, String city) {
        // Adding main groups
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        listDataHeader.add(getString(R.string.Country));
        listDataChild.put(listDataHeader.get(0), prepareListCountry());
        if (!country.equals("")) {//şehir istenmişse şehir grubunu ekle
            List<String> tmp;
            if (country.equals(SehirliUlkeler[0]) || country.equals(SehirliUlkeler[1]) || country.equals(SehirliUlkeler[2]))
                tmp = prepareListCity(country);
            else tmp = prepareListTown(country);
            listDataHeader.add(getString(R.string.City));
            listDataChild.put(listDataHeader.get(1), tmp);
        }
        if (!city.equals("")) {//ilçe istenmişse ilçe grubunu ekle
            listDataHeader.add(getString(R.string.Town));
            listDataChild.put(listDataHeader.get(1), prepareListCity(country));
            listDataChild.put(listDataHeader.get(2), prepareListTown(city));
        }
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        listAdapter.notifyDataSetChanged();
        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    private List<String> prepareListCountry() {
        // Adding child data
        return db.getCountries();
    }

    private List<String> prepareListCity(String country) {
        if (country.equals("")) return new ArrayList<>();
        else return db.getCities(country);
    }

    private List<String> prepareListTown(String city) {
        if (city.equals("")) return new ArrayList<>();
        else return db.getTown(city);
    }

    //change language
    private void setLocale(String localeCode) {
        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        //save to preferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.prefLang), localeCode);
        editor.apply();
        //refresh title
        this.setTitle(getResources().getString(R.string.title_activity_setup));
    }

    /*end select city*/
    private void SelectCity() {
        ExpAdapter = null;
        listAdapter = null;
        String countryID = db.GetAnyID(Ulke.name, nameCountry),
                cityID = db.GetAnyID(Sehir.name, nameCity),
                townID = db.GetAnyID(Ilce.name, nameTown);
        //save to preferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.prefCountry), nameCountry);
        editor.putString(getString(R.string.prefCountryID), countryID);
        editor.putString(getString(R.string.prefCity), nameCity);
        editor.putString(getString(R.string.prefCityID), cityID);
        editor.putString(getString(R.string.prefTown), nameTown);
        editor.putString(getString(R.string.prefTownID), townID);
        editor.putBoolean(getString(R.string.prefSetup), true);
        editor.apply();
        //show home screen and update vakits there
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();
    }
}