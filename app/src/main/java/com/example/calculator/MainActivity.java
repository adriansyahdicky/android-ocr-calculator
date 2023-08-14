package com.example.calculator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.mariuszgromada.math.mxparser.Expression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ImageView clear, getImage, copy;
    EditText recgText;
    Uri imageUri;
    TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clear = findViewById(R.id.clear);
        getImage = findViewById(R.id.getImage);
        copy = findViewById(R.id.copy);
        recgText = findViewById(R.id.recgText);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        getImage.setOnClickListener(view -> ImagePicker.with(MainActivity.this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start());

        copy.setOnClickListener(view -> {
            String text = recgText.getText().toString();
            if(text.isEmpty()){
                Toast.makeText(MainActivity.this, "There is no text to copy", Toast.LENGTH_SHORT).show();
            }else{
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Data", text);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(MainActivity.this, "Text copy to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        clear.setOnClickListener(view -> {
            String text = recgText.getText().toString();
            if(text.isEmpty()){
                Toast.makeText(MainActivity.this, "There is no text to clear", Toast.LENGTH_SHORT).show();
            }else{
                recgText.setText("");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && data != null){
            imageUri = data.getData();
            Toast.makeText(this, "image selected", Toast.LENGTH_SHORT).show();
            recognizeText();
        }else{
            Toast.makeText(this, "image not selected", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void recognizeText(){
        try {
            if (imageUri != null) {
                InputImage inputImage=InputImage.fromFilePath(MainActivity.this, imageUri);
                textRecognizer.process(inputImage)
                        .addOnSuccessListener(text -> {
                            String recognizeText = text.getText();

                            if(isValidMathExpression(recognizeText)){
                                recgText.setText("Result From "+ recognizeText +" = "+
                                        calculateExpression(recognizeText));
                            }else{
                                Toast.makeText(MainActivity.this, "invalid math expression", Toast.LENGTH_SHORT).show();
                            }

                        }).addOnFailureListener(e ->
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                                        .show());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private boolean isValidMathExpression(String request){
        String regex = "^[\\d\\s()+\\-*/.]+$"; // Regex for valid math expression
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(request);
        return matcher.matches();
    }

    private double calculateExpression(String expression) {
        Expression exp = new Expression(expression);
        return exp.calculate();
    }
}