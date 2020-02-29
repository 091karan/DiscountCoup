package com.aakib78.hospitonadmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder> implements Filterable {
    private Context context;
    private ArrayList<Store> storeList;
    private ArrayList<Store> storeListFiltered;
    private DatabaseReference reference;

    public PlacesAdapter() {
    }

    PlacesAdapter(Context context, ArrayList<Store> storeList, DatabaseReference mReference) {
        this.context = context;
        this.storeList = storeList;
        this.storeListFiltered = storeList;
        reference = mReference;
    }

    @NonNull
    @Override
    public PlacesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recycler_item, viewGroup, false);
        return new PlacesViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final PlacesViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.storeName.setText(storeListFiltered.get(position).getStoreName());
        holder.totalDiscount.setText(storeListFiltered.get(position).getTotalDiscount() + "%");
        holder.address.setText(storeListFiltered.get(position).getAddress());
        holder.maxUserAvailOffer.setText(String.valueOf(storeListFiltered.get(position).getLckyUser()));
        holder.offerText.setText("User can get " + storeListFiltered.get(position).getTotalDiscount() + "% discount upto " + storeListFiltered.get(position).getMaxDiscount() + "Rs on min purchase of " + storeListFiltered.get(position).getMinPurchase() + "Rs.");
        Glide.with(context).load(storeListFiltered.get(position).getStoreImage()).into(holder.storeImage);
        holder.category.setText(storeListFiltered.get(position).getCategory());
        if (storeListFiltered.get(position).getLckyUser() == 0) {
            holder.downloadQrCode.setEnabled(false);
            holder.downloadQrCode.setText("OFFER EXPIRED");
            holder.downloadQrCode.setBackgroundColor(Color.GRAY);
        }
        holder.deleteStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setMessage("Are you sure, You wanted to delete this store?");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(storeListFiltered.get(position).getStoreImage());
                                photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // File deleted successfully
                                            reference.child(storeListFiltered.get(position).getStoreId()).removeValue();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Uh-oh, an error occurred!
                                        Log.d("Delete Image", "onFailure: did not delete file");
                                    }
                                });


                            }
                        });
                alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
        holder.downloadQrCode.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if (!storeListFiltered.get(position).getQrKey().isEmpty()) {
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    BitMatrix bitMatrix = null;
                    try {
                        bitMatrix = multiFormatWriter.encode(storeListFiltered.get(position).getQrKey(), BarcodeFormat.QR_CODE, 500, 500);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    PdfDocument pdfDocument = new PdfDocument();
                    PdfDocument.PageInfo pi = new PdfDocument.PageInfo.Builder(720, 1080, 1).create();
                    PdfDocument.Page page = pdfDocument.startPage(pi);
                    Canvas canvas = page.getCanvas();
                    Paint paint = new Paint();
                    paint.setColor(Color.parseColor("#FFFFFF"));
                    canvas.drawPaint(paint);

                    Typeface typeface = ResourcesCompat.getFont(context, R.font.aladin);
                    TextPaint textPaint = new TextPaint();
                    textPaint.setTypeface(typeface);
                    textPaint.setColor(Color.parseColor("#D81B60"));
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    textPaint.setTextSize(78);
                    float textHeight = textPaint.descent() - textPaint.ascent();
                    float textOffset = (textHeight / 2) - textPaint.descent();
                    RectF bounds = new RectF(0, 30, canvas.getWidth(),100 );
                    canvas.drawText("Hospiton", bounds.centerX(), bounds.centerY() + textOffset, textPaint);

                    Paint linePaint = new Paint();
                    linePaint.setColor(Color.RED);
                    linePaint.setStyle(Paint.Style.STROKE);
                    linePaint.setStrokeWidth(4);
                    linePaint.setAntiAlias(true);
                    int offset = 50;
                    canvas.drawLine(offset,140,canvas.getWidth()-offset,140,linePaint);

                    Typeface typefc = ResourcesCompat.getFont(context, R.font.roboto_bold);
                    TextPaint text = new TextPaint();
                    text.setTypeface(typefc);
                    text.setColor(Color.parseColor("#5c5cff"));
                    text.setTextAlign(Paint.Align.CENTER);
                    text.setTextSize(48);
                    float textHght = text.descent() - textPaint.ascent();
                    float textOfst = (textHght / 2) - textPaint.descent();
                    RectF bnds = new RectF(0, 180, canvas.getWidth(),200 );
                    canvas.drawText("Pay To "+storeListFiltered.get(position).getStoreName(), bnds.centerX(), bnds.centerY() + textOfst, text);

                    TextPaint txt = new TextPaint();
                    txt.setColor(Color.parseColor("#5c5cff"));
                    txt.setTextAlign(Paint.Align.CENTER);
                    txt.setTextSize(34);
                    float txtHght = txt.descent() - textPaint.ascent();
                    float txtOfst = (txtHght / 2) - textPaint.descent();
                    RectF bonds = new RectF(0, 220, canvas.getWidth(),280 );
                    canvas.drawText("Scan this QR Code", bonds.centerX(), bonds.centerY() + txtOfst, txt);


                    bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
                    paint.setColor(Color.BLUE);
                    canvas.drawBitmap(bitmap, (int)(canvas.getWidth()/2-bitmap.getWidth()/2), 300, null);

                    TextPaint txtOffer = new TextPaint();
                    txtOffer.setColor(Color.parseColor("#0b7b8c"));
                    txtOffer.setTextAlign(Paint.Align.CENTER);
                    txtOffer.setTextSize(42);
                    txtOffer.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    float txtHeght = txt.descent() - textPaint.ascent();
                    float txtOffst = (txtHeght / 2) - textPaint.descent();
                    RectF bds = new RectF(0, (int)(canvas.getHeight())+250, canvas.getWidth(),280 );
                    canvas.drawText("Hurry Up and Snag "+storeListFiltered.get(position).getTotalDiscount()+"% Off.", bds.centerX(), bds.centerY() + txtOffst, txtOffer);

                    Paint line2Paint = new Paint();
                    line2Paint.setColor(Color.RED);
                    line2Paint.setStyle(Paint.Style.STROKE);
                    line2Paint.setStrokeWidth(4);
                    line2Paint.setAntiAlias(true);
                    canvas.drawLine(offset,canvas.getHeight()-150,canvas.getWidth()-offset,canvas.getHeight()-150,line2Paint);


                    pdfDocument.finishPage(page);
                    File root = new File(Environment.getExternalStorageDirectory(), "Hospiton-QR");
                    //File root = new File(Environment.getDataDirectory(), "Hospiton-QR");
                    if (!root.exists()) {
                        root.mkdir();
                    }
                    File file = new File(root, UUID.randomUUID().toString() + ".pdf");
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        pdfDocument.writeTo(fileOutputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    pdfDocument.close();
                    Toast.makeText(context, "PDF Generated Successfully.", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return storeListFiltered.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String key = constraint.toString();
                if (key.isEmpty()) {
                    storeListFiltered = storeList;
                } else {
                    ArrayList<Store> filteredList = new ArrayList<>();
                    for (Store storeList1 : storeList) {
                        if (storeList1.getStoreName().toLowerCase().contains(key.toLowerCase())) {
                            filteredList.add(storeList1);
                        }else if(storeList1.getCategory().toLowerCase().contains(key.toLowerCase())){
                            filteredList.add(storeList1);
                        }
                    }
                    storeListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = storeListFiltered;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                storeListFiltered = (ArrayList<Store>) results.values;
                notifyDataSetChanged();

            }
        };

    }

    public class PlacesViewHolder extends RecyclerView.ViewHolder {
        TextView storeName;
        TextView totalDiscount;
        TextView address;
        TextView offerText;
        TextView maxUserAvailOffer;
        TextView category;
        TextView deleteStore;
        Button downloadQrCode;
        ImageView storeImage;

        public PlacesViewHolder(@NonNull View itemView) {
            super(itemView);
            deleteStore = (TextView) itemView.findViewById(R.id.deleteItem);
            maxUserAvailOffer = (TextView) itemView.findViewById(R.id.maxUserAvailOffer);
            totalDiscount = (TextView) itemView.findViewById(R.id.totalDiscount);
            storeImage = (ImageView) itemView.findViewById(R.id.storeImage);
            storeName = (TextView) itemView.findViewById(R.id.storeName);
            category = (TextView) itemView.findViewById(R.id.category);
            address = (TextView) itemView.findViewById(R.id.address);
            offerText = (TextView) itemView.findViewById(R.id.offerText);
            downloadQrCode = (Button) itemView.findViewById(R.id.getQrCodePdf);
        }

    }
}
