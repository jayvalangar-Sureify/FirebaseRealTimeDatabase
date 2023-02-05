package com.example.firebaserealtimedatabase;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;

public class UploadPDF extends AppCompatActivity {

    Button btn_upload;
    EditText et_upload_file_name;

    StorageReference storageReference;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_pdf);

        btn_upload = findViewById(R.id.btn_upload);
        et_upload_file_name = findViewById(R.id.et_upload_file_name);

        // Database
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("Uploads");

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createMyPDF(getApplicationContext(), "jsn", "SKH_Bills.pdf");
                uploadFiles(Uri.fromFile(getFilePath()));
            }
        });

    }

    private void selectFiles() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF Files..."), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && (data != null) && (data.getData() != null) && ( resultCode == RESULT_OK)){
//            uploadFiles(data.getData());
        }
    }

    private void uploadFiles(Uri data) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading .....");
        progressDialog.show();


        StorageReference reference = storageReference.child("Uploads"+System.currentTimeMillis()+".pdf");
        reference.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isComplete());
                        Uri url = uriTask.getResult();

                        DownloadFirebaseModelClass pdfClass = new DownloadFirebaseModelClass(et_upload_file_name.getText().toString(), url.toString());
                        databaseReference.child(databaseReference.push().getKey()).setValue(pdfClass);


                        Toast.makeText(UploadPDF.this, "File Uploaded SUccessfully", Toast.LENGTH_LONG).show();

                        progressDialog.dismiss();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        double progress = (100.0*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                        progressDialog.setMessage("Upload:"+(int) progress+"%");
                    }
                });
    }



    // Create PDF
    //==============================================================================================
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void createMyPDF(Context context, String file_data, String file_name) {

        //Create the pdf page
        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);
        Paint myPaint = new Paint();

        //Initialize top and left margin for text
        int x = 10, y = 25;

        //Paint the string to the page
        for (String line : file_data.split("\n")) {
            myPage.getCanvas().drawText(line, x, y, myPaint);
            y += myPaint.descent() - myPaint.ascent();
        }

        //Finish writing/painting on the page
        myPdfDocument.finishPage(myPage);

        //Initialize the file with the name and path

        ContextWrapper contextWrapper = new ContextWrapper(context);
        File downloadDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadDirectory, file_name);
        try {
            myPdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(context, "File saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            //If file is not saved, print stack trace, clear edittext, and display toast message
            e.printStackTrace();
            Log.i("test_response", "Error : " + e.getMessage().toString());
            Toast.makeText(context, "File not saved... Possible permissions error", Toast.LENGTH_SHORT).show();
        }
        myPdfDocument.close();
    }
    //==============================================================================================

    public File getFilePath(){
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File downloadDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadDirectory, "SKH_Bills.pdf");
        return file;
    }
}