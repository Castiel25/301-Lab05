package com.example.lab5_starter;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {
    private FirebaseFirestore db;
    private CollectionReference citiesRef;


    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
                return;
            }
            if (value == null) return;

            cityArrayList.clear();

            for (QueryDocumentSnapshot snapshot : value) {
                String name = snapshot.getString("name");
                String province = snapshot.getString("province");

                if (name != null || province != null) {
                    cityArrayList.add(new City(name, province));
                }
            }
            cityArrayAdapter.notifyDataSetChanged();
        });
        // addDummyData();

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(), "Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(), "City Details");
        });

        cityListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            if (city == null) {
                return true;
            }
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete City")
                    .setMessage("Permanently delete " + city.getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteCity(city);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

    }

    @Override
    public void updateCity(City city, String cityName, String cityProvince) {


        city.setName(cityName);
        city.setProvince(cityProvince);

        cityArrayAdapter.notifyDataSetChanged();

        // Updating the database using delete + addition
    }

    @Override
    public void addCity(City city) {
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city).addOnSuccessListener(unused ->
                        Log.d("FireStore", "City added successfully!" + city.getName()))
                .addOnFailureListener(e ->
                        Log.e("FireStore", "Failed to add city", e));
    }

    public void deleteCity(City city) {
        cityArrayList.remove(city);
        cityArrayAdapter.notifyDataSetChanged();
        citiesRef.document(city.getName()).delete()
                .addOnSuccessListener(unused ->
                        Log.d("FireStore", city.getName() + " successfully deleted!"))
                .addOnFailureListener(e ->
                        Log.e("FireStore", "Failed to delete city", e));
    }





    /*public void addDummyData() {
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }*/
}