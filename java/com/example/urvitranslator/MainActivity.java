package com.example.urvitranslator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_PERM = 1001;

    private Button btnUrToVi, btnViToUr, btnSpeakOut;
    private EditText inputText;
    private TextView outputText, status;
    private TextToSpeech ttsUr, ttsVi;
    private SpeechRecognizer speechRecognizer;
    private boolean isUrduToVietnamese = true;
    private JSONObject dictionary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadDictionary();

        btnUrToVi = findViewById(R.id.btnUrToVi);
        btnViToUr = findViewById(R.id.btnViToUr);
        btnSpeakOut = findViewById(R.id.btnSpeakOut);
        inputText = findViewById(R.id.inputText);
        outputText = findViewById(R.id.outputText);
        status = findViewById(R.id.status);

        ensureMicPermission();

        ttsUr = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) ttsUr.setLanguage(new Locale("ur"));
            }
        });
        ttsVi = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) ttsVi.setLanguage(new Locale("vi"));
            }
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        btnUrToVi.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                isUrduToVietnamese = true;
                startListening("ur");
            }
        });
        btnViToUr.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                isUrduToVietnamese = false;
                startListening("vi");
            }
        });
        btnSpeakOut.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                speakOut();
            }
        });
    }

    private void ensureMicPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQ_PERM);
        }
    }

    private void startListening(String langCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, langCode);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) { status.setText("Listening…"); }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int error) { status.setText("Error: " + error); }
            @Override public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spoken = matches.get(0);
                    inputText.setText(spoken);
                    translate(spoken);
                }
                status.setText("Tap a mic and speak…");
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(intent);
    }

    private void translate(String text) {
        String t = text == null ? "" : text.trim();
        String translated = "(no translation)";
        try {
            JSONObject map = dictionary.getJSONObject(isUrduToVietnamese ?
                    "urdu_to_vietnamese" : "vietnamese_to_urdu");
            translated = map.optString(t, translated);
        } catch (Exception ignore) {}
        outputText.setText(translated);
        speakTranslation(translated);
    }

    private void speakTranslation(String text) {
        if (isUrduToVietnamese) {
            ttsVi.speak(text, TextToSpeech.QUEUE_FLUSH, null, "viOut");
        } else {
            ttsUr.speak(text, TextToSpeech.QUEUE_FLUSH, null, "urOut");
        }
    }

    private void speakOut() {
        String text = outputText.getText().toString();
        if (text == null || text.length() == 0) {
            Toast.makeText(this, "Nothing to speak", Toast.LENGTH_SHORT).show();
        } else {
            speakTranslation(text);
        }
    }

    private void loadDictionary() {
        try {
            InputStream is = getAssets().open("words.json");
            byte[] buf = new byte[is.available()];
            is.read(buf); is.close();
            dictionary = new JSONObject(new String(buf, StandardCharsets.UTF_8));
        } catch (Exception e) {
            dictionary = new JSONObject();
            Toast.makeText(this, "Dictionary not found (using empty map).", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) speechRecognizer.destroy();
        if (ttsUr != null) ttsUr.shutdown();
        if (ttsVi != null) ttsVi.shutdown();
    }
}
