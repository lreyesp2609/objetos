package com.example.objetos;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.provider.MediaStore;
import android.view.View;
import android.Manifest;

import android.widget.Toast;

import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import com.google.mlkit.vision.text.Text;

import java.io.IOException;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements OnSuccessListener<Text>, OnFailureListener{

    public static int REQUEST_CAMERA = 111;
    public static int REQUEST_GALLERY = 222;

    ImageView mImageView;
    Bitmap mSelectedImage;
    TextView txtResults;

    private BarcodeScanner barcodeScanner;
    private BarcodeScannerOptions barcodeScannerOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.image_view);

        txtResults = findViewById(R.id.txtresults);

        mImageView = findViewById(R.id.image_view);

        barcodeScannerOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build();

        barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
    }

    public void abrirGaleria(View view) {
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_GALLERY);
    }


    public void abrirCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            try {
                if (requestCode == REQUEST_CAMERA)
                    mSelectedImage = (Bitmap) data.getExtras().get("data");
                else
                    mSelectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                mImageView.setImageBitmap(mSelectedImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public void onFailure(@NonNull Exception e) {
        txtResults.setText("Error al procesar imagen");
    }

    public void scanqr(View v) {
        if (mSelectedImage != null) {
            InputImage image = InputImage.fromBitmap(mSelectedImage, 0);

            barcodeScanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            if (barcodes.size() == 0) {
                                txtResults.setText("No se encontraron códigos QR o códigos de barras");
                            } else {
                                StringBuilder resultText = new StringBuilder();
                                for (Barcode barcode : barcodes) {
                                    if (barcode.getFormat() == Barcode.FORMAT_QR_CODE) {
                                        resultText.append("Contenido QR: ").append(barcode.getDisplayValue()).append("\n");
                                    } else {
                                        resultText.append("Código de barras: ").append(barcode.getDisplayValue()).append("\n");
                                    }
                                }
                                txtResults.setText(resultText.toString());
                            }
                        }
                    })
                    .addOnFailureListener(this);
        } else {
            Toast.makeText(this, "Seleccione una imagen antes de escanear", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSuccess(Text text) {
        List<Text.TextBlock> blocks = text.getTextBlocks();
        String resultados = "";
        if (blocks.size() == 0) {
            resultados = "No hay Texto";
        } else {
            for (int i = 0; i < blocks.size(); i++) {
                List<Text.Line> lines = blocks.get(i).getLines();
                for (int j = 0; j < lines.size(); j++) {
                    List<Text.Element> elements = lines.get(j).getElements();
                    for (int k = 0; k < elements.size(); k++) {
                        resultados = resultados + elements.get(k).getText() + " ";
                    }
                }
            }
            resultados = resultados + "\n";
        }
        txtResults.setText(resultados);
    }
}