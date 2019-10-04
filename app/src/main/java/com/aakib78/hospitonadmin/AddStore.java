package com.aakib78.hospitonadmin;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddStore extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText strName;
    EditText strAddress;
    EditText ttlDiscount;
    EditText mnPurchase;
    EditText mxDiscount;
    EditText strLatitude;
    EditText strLongitude;
    EditText lckyUser;
    ImageView strImage;
    Spinner spinner;
    int luckyUser = 0;
    Boolean offerAvailable = true;
    Button addStore;
    List<String> categories;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mReference;
    private Store store;
    public Uri imageUri;
    private StorageReference storageReference;
    Bitmap bitmap;
    Boolean taskSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_store);
        init();

        FirebaseApp.initializeApp(this);
        categories = new ArrayList<String>();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        store = new Store();


        strImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });


        addStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (uploadImage(UUID.randomUUID())) {
                    Toast.makeText(AddStore.this, "One Store added.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void init() {
        strName = (EditText) findViewById(R.id.storeName);
        lckyUser = (EditText) findViewById(R.id.luckyUser);
        strAddress = (EditText) findViewById(R.id.address);
        ttlDiscount = (EditText) findViewById(R.id.totalDiscount);
        mnPurchase = (EditText) findViewById(R.id.minPurchaseAmount);
        mxDiscount = (EditText) findViewById(R.id.maximumDiscount);
        strLatitude = (EditText) findViewById(R.id.latitude);
        strLongitude = (EditText) findViewById(R.id.longitude);
        spinner = (Spinner) findViewById(R.id.spinner);
        addStore = (Button) findViewById(R.id.submit);
        strImage = (ImageView) findViewById(R.id.storeImage);
        firebaseDatabase = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child("ImageFolder");
        mReference = firebaseDatabase.getReference("places");
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                strImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean uploadImage(final UUID random) {
        if (imageUri != null) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading data....");
            progressDialog.show();
            final StorageReference ref = storageReference.child("image-" + random);
            final String storeName = strName.getText().toString().trim();
            final String lkyUsr=lckyUser.getText().toString();
            final String address = strAddress.getText().toString().trim();
            final String totalDiscount = ttlDiscount.getText().toString().trim();
            final String minPurchase = mnPurchase.getText().toString().trim();
            final String maxDiscount = mxDiscount.getText().toString().trim();
            final String storeLatitude = strLatitude.getText().toString().trim();
            final String storeLongitude = strLongitude.getText().toString().trim();
            final String category = spinner.getSelectedItem().toString();
            final String patternLatitude = "^(\\+|-)?(?:90(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-8][0-9])(?:(?:\\.[0-9]{1,6})?))$";
            final String patternLongitude = "^(\\+|-)?(?:180(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\\.[0-9]{1,6})?))$";
            final String qrKey = UUID.randomUUID().toString();
            final String ratingValue="0";
            final String totalUsersRated="0";
            final String storeId=mReference.push().getKey();

            if (TextUtils.isEmpty(lkyUsr)||TextUtils.isEmpty(storeName) || TextUtils.isEmpty(address) || TextUtils.isEmpty(totalDiscount) || TextUtils.isEmpty(minPurchase) || TextUtils.isEmpty(maxDiscount) || TextUtils.isEmpty(storeLatitude) || TextUtils.isEmpty(storeLongitude) || TextUtils.isEmpty(category)) {
                Toast.makeText(AddStore.this, "All fields required.", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return false;

            }
            if (!storeLatitude.matches(patternLatitude) || !storeLongitude.matches(patternLongitude)) {
                Toast.makeText(this, "Invalid latitude/longitude.", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return false;

            }
            if (Integer.parseInt(lckyUser.getText().toString()) <= 0) {
                Toast.makeText(AddStore.this, "No. of users should greater than zero.", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return false;
            }else {
                ref.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                luckyUser=Integer.parseInt(lckyUser.getText().toString());
                                store.setStoreName(storeName);
                                store.setAddress(address);
                                store.setLckyUser(luckyUser);
                                store.setTotalDiscount(totalDiscount);
                                store.setMinPurchase(minPurchase);
                                store.setMaxDiscount(maxDiscount);
                                store.setStoreLatitude(storeLatitude);
                                store.setStoreLongitude(storeLongitude);
                                store.setCategory(category);
                                store.setOfferAvailable(offerAvailable);
                                store.setQrKey(qrKey);
                                store.setRatingValue(ratingValue);
                                store.setTotalUserRated(totalUsersRated);
                                store.setStoreId(storeId);
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String storeImage = String.valueOf(uri);
                                        store.setStoreImage(storeImage);
                                        mReference.child(storeId).setValue(store);
                                        taskSuccess = true;
                                        progressDialog.dismiss();
                                        Toast.makeText(AddStore.this, "Upload Successful.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                taskSuccess = false;
                                Toast.makeText(AddStore.this, "Upload Failed.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            }
                        });
                return taskSuccess;
            }
        }
        Toast.makeText(this, "Please select image.", Toast.LENGTH_SHORT).show();
        return taskSuccess;
    }
}