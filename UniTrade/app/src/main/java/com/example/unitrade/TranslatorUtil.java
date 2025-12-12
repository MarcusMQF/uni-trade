package com.example.unitrade;

import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.HashMap;
import java.util.Map;

public class TranslatorUtil {

    private static final String TAG = "TranslatorUtil";
    private static TranslatorUtil instance;
    private final Map<String, Translator> translators = new HashMap<>();

    public static synchronized TranslatorUtil getInstance() {
        if (instance == null) {
            instance = new TranslatorUtil();
        }
        return instance;
    }

    // Method for translating TextViews, storing original text in a tag
    public void translate(final TextView textView, final String targetLanguage) {
        if (textView == null || textView.getText() == null || targetLanguage == null) {
            return;
        }

        String originalText = (String) textView.getTag(R.id.original_text_tag);
        if (originalText == null) {
            originalText = textView.getText().toString();
            textView.setTag(R.id.original_text_tag, originalText);
        }

        if (originalText.isEmpty()) {
            return;
        }

        if ("en".equals(targetLanguage)) {
            textView.setText(originalText);
            return;
        }

        translate(originalText, targetLanguage, textView::setText);
    }

    // Overloaded method for translating any CharSequence with a callback
    public void translate(final CharSequence text, final String targetLanguage, final OnSuccessListener<String> callback) {
        if (text == null || text.length() == 0 || targetLanguage == null || callback == null) {
            return;
        }

        final String originalText = text.toString();

        if ("en".equals(targetLanguage)) {
            callback.onSuccess(originalText);
            return;
        }

        Translator translator = getTranslator(targetLanguage);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(aVoid -> translator.translate(originalText)
                        .addOnSuccessListener(callback)
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error translating text: " + originalText, e);
                            callback.onSuccess(originalText); // Revert on failure
                        }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error downloading model for: " + targetLanguage, e);
                    callback.onSuccess(originalText); // Revert on failure
                });
    }

    private Translator getTranslator(String targetLanguage) {
        if (translators.containsKey(targetLanguage)) {
            return translators.get(targetLanguage);
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(targetLanguage)
                .build();

        Translator translator = Translation.getClient(options);
        translators.put(targetLanguage, translator);
        return translator;
    }
}