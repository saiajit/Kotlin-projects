package tw.firemaples.onscreenocr.ocr.tesseract;

import android.content.Context;
import android.os.AsyncTask;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import tw.firemaples.onscreenocr.R;

/**
 * Created by firemaples on 2016/3/2.
 */
public class OcrInitAsyncTask extends AsyncTask<Void, String, Boolean> {
    private static final Logger logger = LoggerFactory.getLogger(OcrInitAsyncTask.class);

    private final Context context;
    private final TessBaseAPI baseAPI;
    private final String recognitionLang;

    private final File tessRootDir;

    private int pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_AUTO_OSD;
    private OnOcrInitAsyncTaskCallback callback;

    public OcrInitAsyncTask(Context context, OnOcrInitAsyncTaskCallback callback) {
        this.context = context.getApplicationContext();
        this.callback = callback;

        this.baseAPI = TesseractOCRManager.INSTANCE.getTessBaseAPI();
        this.recognitionLang = OCRLangUtil.INSTANCE.getSelectedLangCode();

        this.tessRootDir = OCRFileUtil.INSTANCE.getTessDataBaseDir();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onProgressUpdate(context.getString(R.string.progress_ocrInitializing));
        getPreferences();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        baseAPI.init(tessRootDir.getAbsolutePath(), recognitionLang, TessBaseAPI.OEM_DEFAULT);
        baseAPI.setPageSegMode(pageSegmentationMode);

        return true;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        logger.info(values[0]);
        if (callback != null) {
            callback.showMessage(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (callback != null) {
            callback.hideMessage();
        }
        if (result) {
            if (callback != null) {
                callback.onOcrInitialized();
            }
        }
    }

    private void getPreferences() {
        int searchIndex = OCRLangUtil.INSTANCE.getSelectedLangIndex();
        switch (searchIndex) {
            case 0:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_AUTO_OSD;
                break;
            case 1:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_AUTO;
                break;
            case 2:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK;
                break;
            case 3:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR;
                break;
            case 4:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_COLUMN;
                break;
            case 5:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_LINE;
                break;
            case 6:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_WORD;
                break;
            case 7:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK_VERT_TEXT;
                break;
            case 8:
                pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT;
                break;
        }
    }

    public interface OnOcrInitAsyncTaskCallback {
        void onOcrInitialized();

        void showMessage(String message);

        void hideMessage();
    }
}
