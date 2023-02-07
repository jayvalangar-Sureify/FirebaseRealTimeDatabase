package com.example.firebaserealtimedatabase;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
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
import androidx.core.content.FileProvider;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;

import kotlin.jvm.internal.Intrinsics;

public class UploadPDF extends AppCompatActivity {

    Button btn_upload;
    EditText et_upload_file_name;

    public static StorageReference storageReference;
    public static DatabaseReference databaseReference;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_pdf);

        context = getApplicationContext();

        btn_upload = findViewById(R.id.btn_upload);
        et_upload_file_name = findViewById(R.id.et_upload_file_name);



        createMyPDF(getApplicationContext(), "Test FEB 7, 12:25", "SKH_Bills.pdf");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myWorkManager(getApplicationContext());
        }

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                ContextWrapper contextWrapper = new ContextWrapper(context);
                File downloadDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadDirectory,"SKH_Bills.pdf");
                Uri file_surtikhaman_uri = FileProvider.getUriForFile(
                        context,
                        "com.surti.khaman.house.provider", //(use your app signature + ".provider" )
                        file);

                uploadFiles(file_surtikhaman_uri);
            }
        });

    }

    @RequiresApi(26)
    public static final void myWorkManager(Context context) {
        Constraints var10000 = (new Constraints.Builder()).setRequiresCharging(false).setRequiredNetworkType(NetworkType.NOT_REQUIRED).setRequiresCharging(false).setRequiresBatteryNotLow(true).build();
        Intrinsics.checkNotNullExpressionValue(var10000, "Constraints.Builder()\n  …rue)\n            .build()");
        Constraints constraints = var10000;
        WorkRequest var3 = ((androidx.work.PeriodicWorkRequest.Builder)(new androidx.work.PeriodicWorkRequest.Builder(MyWorker.class, 15L, TimeUnit.MINUTES)).setConstraints(constraints)).build();
        Intrinsics.checkNotNullExpressionValue(var3, "PeriodicWorkRequest.Buil…nts)\n            .build()");
        PeriodicWorkRequest myRequest = (PeriodicWorkRequest)var3;
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("my_id", ExistingPeriodicWorkPolicy.KEEP, myRequest);
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

    public static void uploadFiles(Uri data) {
//        final ProgressDialog progressDialog = new ProgressDialog(context);
//        progressDialog.setTitle("Uploading .....");
//        progressDialog.show();
        Log.i("test_response", "uploadFiles() : "+data.toString());




        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                initFirebase();

                // Database
                storageReference = FirebaseStorage.getInstance().getReference();
                databaseReference = FirebaseDatabase.getInstance().getReference("Uploads");
                StorageReference reference = storageReference.child("Uploads");
                reference.putFile(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Log.i("test_response", "uploadFiles() : "+data.toString());

                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isComplete());
                                Uri url = uriTask.getResult();

                                DownloadFirebaseModelClass pdfClass = new DownloadFirebaseModelClass("TEST_FEB_7", url.toString());
                                databaseReference.child(databaseReference.push().getKey()).setValue(pdfClass);

                                Log.i("test_response", "File Uploaded SUccessfully : "+data.toString());

//                                Toast.makeText(context, "File Uploaded SUccessfully", Toast.LENGTH_LONG).show();

//                        progressDialog.dismiss();
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                double progress = (100.0*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                                Log.i("test_response", "Upload:"+(int) progress+"%");
//                        double progress = (100.0*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
//                        progressDialog.setMessage("Upload:"+(int) progress+"%");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("test_response", "Error : "+e.getMessage().toString());
//                                Toast.makeText(context, "Failed To Upload : "+e.getMessage().toString(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });


    }

    private static void initFirebase() {
        FirebaseApp.initializeApp(context);

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance());
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