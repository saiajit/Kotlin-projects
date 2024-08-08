package tw.firemaples.onscreenocr.tts;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatings.screencrop.DialogView;

/**
 * Created by firemaples on 30/04/2017.
 */

public class AndroidTTSManager {
    private static final Logger logger = LoggerFactory.getLogger(AndroidTTSManager.class);

    private static AndroidTTSManager _instance;

    private static final String PATH_TTS_FILE = "tts";
    private static final String PATH_SILENCE_FILE = "silence.wav";

    private final Context context;

    private TextToSpeech tts;

    private boolean ttsReady;

    private File ttsFolder;
    private File silenceFile;

    private HashMap<String, AndroidTTSManagerCallback> callbackHashMap = new HashMap<>();
    private HashMap<String, File> fileHashMap = new HashMap<>();

    private AndroidTTSManager(Context context) {
        this.context = context;

        ttsFolder = new File(context.getExternalCacheDir(), PATH_TTS_FILE);
        if (!ttsFolder.exists()) {
            ttsFolder.mkdirs();
        }
        silenceFile = new File(ttsFolder, PATH_SILENCE_FILE);
//        if (!silenceFile.exists()) {
        new CreateSilenceFileTask().execute();
//        }
    }

    public static AndroidTTSManager getInstance(Context context) {
        if (_instance == null) {
            _instance = new AndroidTTSManager(context);
        }

        return _instance;
    }

    public void init() {
        if (tts == null) {
            ttsReady = false;
            tts = new TextToSpeech(context.getApplicationContext(), onInitListener);
            tts.setOnUtteranceProgressListener(utteranceProgressListener);
        }
    }

    private File getTTSFile(String lang, String ttsContent) {
        String fileName = lang + "_" + ttsContent.hashCode() + ".wav";
        File ttsFile = new File(ttsFolder.getAbsolutePath(), fileName);
        return ttsFile;
    }

    public File getSilenceFile() {
        if (silenceFile != null && silenceFile.exists()) {
            return silenceFile;
        }
        return null;
    }

    public void setCallback(String requestId, AndroidTTSManagerCallback callback) {
        callbackHashMap.put(requestId, callback);
    }

    private AndroidTTSManagerCallback getCallback(String requestId) {
        return callbackHashMap.get(requestId);
    }

    public synchronized void retrieveTTSFile(String lang, String ttsContent, String requestId) throws LanguageNotSupportException {
        if (ttsContent == null) {
            AndroidTTSManagerCallback callback = callbackHashMap.get(requestId);
            callback.onError();
            return;
        }
        if (ttsReady) {
            int setLangResult = tts.setLanguage(new Locale(lang));
            if (setLangResult == TextToSpeech.LANG_MISSING_DATA ||
                    setLangResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Lanuage data is missing or the language is not supported.
                logger.error("retrieveTTSFile failed, Language is not available.");
                throw new LanguageNotSupportException(String.format(Locale.getDefault(), "Language [%s] is not available.", lang));
            } else {
                File ttsFile = getTTSFile(lang, ttsContent);
                if (ttsFile.exists()) {
                    logger.info("TTSFile has exist: " + ttsFile.getAbsolutePath());
                    if (getCallback(requestId) != null) {
                        getCallback(requestId).onDone(ttsFile);
                    }
                } else {
                    fileHashMap.put(requestId, ttsFile);
                    int requestFileResult = tts.synthesizeToFile(ttsContent, null, ttsFile, requestId);
                    if (requestFileResult != TextToSpeech.SUCCESS) {
                        logger.error("retrieveTTSFile failed, failed to synthesizeToFile.");
                    }
                }
            }
        } else {
            tts = null;
            init();
        }
    }

    private TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            logger.info("onInit()");
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true;
            } else {
                ttsReady = false;
                // Initialization failed.
                logger.error("Could not initialize TextToSpeech.");
                // May be its not installed so we prompt it to be installed
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                try {
                    context.startActivity(installIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            DialogView dialogView = new DialogView(context);
                            dialogView.reset();
                            dialogView.setType(DialogView.Type.CONFIRM_ONLY);
                            dialogView.setTitle(context.getString(R.string.error));
                            dialogView.setContentMsg(context.getString(R.string.msg_ttsEngineInitFailed));
                            dialogView.attachToWindow();
                        }
                    });
                }
            }
        }
    };

    private UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            logger.info("utteranceProgressListener#onStart()");
        }

        @Override
        public void onDone(String utteranceId) {
            logger.info("utteranceProgressListener#onDone()");
            if (getCallback(utteranceId) != null && fileHashMap.containsKey(utteranceId) && fileHashMap.get(utteranceId) != null) {
                AndroidTTSManagerCallback callback = getCallback(utteranceId);
                File file = fileHashMap.get(utteranceId);
                callback.onDone(file);
            }
        }

        @Override
        public void onError(String utteranceId) {
            logger.error("utteranceProgressListener#onError()");

            if (getCallback(utteranceId) != null) {
                AndroidTTSManagerCallback callback = getCallback(utteranceId);
                callback.onError();
            }
        }
    };

    private class CreateSilenceFileTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            if (silenceFile != null) {
                if (silenceFile.exists()) {
                    if (!silenceFile.delete()) {
                        return false;
                    }
                }

                AssetManager assetManager = context.getAssets();
                try {
                    InputStream stream = assetManager.open(PATH_SILENCE_FILE);
                    if (createFileFromInputStream(stream, silenceFile)) {
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }

        private boolean createFileFromInputStream(InputStream inputStream, File outputFile) {

            try {
                OutputStream outputStream = new FileOutputStream(outputFile);
                byte buffer[] = new byte[1024];
                int length;

                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.close();
                inputStream.close();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }
    }

    public class LanguageNotSupportException extends Exception {
        LanguageNotSupportException(String msg) {
            super(msg);
        }
    }

    public interface AndroidTTSManagerCallback {
        void onDone(File file);

        void onError();
    }
}
