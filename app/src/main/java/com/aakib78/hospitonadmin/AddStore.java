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
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.tasks.Continuation;
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

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.github.gcacace.signaturepad.views.SignaturePad;

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
    SignaturePad signaturePad;
    CheckBox checkBox;

    private Boolean var1 = false, var2 = false, var3 = false;

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

        String checkBoxText = "I agree to all the <a href='http://www.redbus.in/mob/mTerms.aspx' > Terms and Conditions</a>";

        checkBox.setText(Html.fromHtml(checkBoxText));
        checkBox.setMovementMethod(LinkMovementMethod.getInstance());


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
                uploadImage(UUID.randomUUID());
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
        signaturePad = (SignaturePad) findViewById(R.id.signaturePad);
        checkBox  = findViewById(R.id.checkBox);
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

    //button click
    //upload signature to firebase storage -> url
    //create the Store object , upload to database

    public void uploadImage(final UUID random) {
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
            final Bitmap signature =  signaturePad.getTransparentSignatureBitmap();

            if (TextUtils.isEmpty(lkyUsr)||TextUtils.isEmpty(storeName) || TextUtils.isEmpty(address) || TextUtils.isEmpty(totalDiscount) || TextUtils.isEmpty(minPurchase) || TextUtils.isEmpty(maxDiscount) || TextUtils.isEmpty(storeLatitude) || TextUtils.isEmpty(storeLongitude) || TextUtils.isEmpty(category)) {
                Toast.makeText(AddStore.this, "All fields required.", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return;

            }
            if (!storeLatitude.matches(patternLatitude) || !storeLongitude.matches(patternLongitude)) {
                Toast.makeText(this, "Invalid latitude/longitude.", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return;

            }
            if (Integer.parseInt(lckyUser.getText().toString()) <= 0) {
                Toast.makeText(AddStore.this, "No. of users should greater than zero.", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return;
            }

            if (!checkBox.isChecked()){
                Toast.makeText(this, "Agree to all the terms and conditions", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            signature.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] signatureData = baos.toByteArray();

            UploadTask uploadTask = ref.putBytes(signatureData);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final String downloadUri = task.getResult().toString();

                        var1 = true;
                        upload();


                    } else {
                        progressDialog.dismiss();
                        taskSuccess = false;
                        Toast.makeText(AddStore.this, "Upload Failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ref.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            var2 = true;
                            upload();
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

        }
    }

    private void upload() {
        if (var1 && var2 && var3) {
            luckyUser = Integer.parseInt(lckyUser.getText().toString());
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
            store.setStoreImage(downloadUri);
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
    }

}