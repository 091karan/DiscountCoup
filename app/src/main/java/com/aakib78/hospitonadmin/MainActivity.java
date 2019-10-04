package com.aakib78.hospitonadmin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView signOut;
    private Button addNewStore;
    private DatabaseReference reference;
    private RecyclerView recyclerView;
    private PlacesAdapter adapter;
    private ArrayList<Store> storeList;
    private EditText etSearch;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching nearby stores..");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        init();

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                updateUI();
            }
        });

        addNewStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AddStore.class));
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.getFilter().filter(s);

            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });


    }

    public void fetchData(){
        progressDialog.show();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                storeList.clear();
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    Store s=dataSnapshot1.getValue(Store.class);
                    storeList.add(s);
                }
                adapter=new PlacesAdapter(MainActivity.this,storeList,reference);
                recyclerView.setAdapter(adapter);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            updateUI();
        }
    }
    private void updateUI() {
        progressDialog.dismiss();
        Intent homeIntent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(homeIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
        fetchData();
        adapter.notifyDataSetChanged();
    }

    private void init() {
        reference = FirebaseDatabase.getInstance().getReference("places");
        reference.keepSynced(true);
        etSearch = (EditText)findViewById(R.id.editText);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        storeList = new ArrayList<Store>();
        storeList.clear();
        adapter=new PlacesAdapter();
        addNewStore=(Button)findViewById(R.id.addNewStore);
        mAuth=FirebaseAuth.getInstance();
        signOut=(TextView)findViewById(R.id.signOut);
    }
}
