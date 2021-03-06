package com.rhomobile.rhodes.webview;

import java.io.FileOutputStream;

import com.rhomobile.rhodes.Logger;
import com.rhomobile.rhodes.extmanager.IRhoWebView;
import com.rhomobile.rhodes.osfunctionality.AndroidFunctionalityManager;
import com.rhomobile.rhodes.util.PerformOnUiThread;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

public class GoogleWebView implements IRhoWebView {
    private static final String TAG = GoogleWebView.class.getSimpleName(); 

    private WebChromeClient mChromeClient;
    private static WebViewClient mWebViewClient;
    private static Boolean mInitialized = false;

    private android.webkit.WebView mWebView;
    private ViewGroup mContainerView;
    private TextZoom mTextZoom = TextZoom.NORMAL;

    public GoogleWebView(Activity activity) {
        synchronized(mInitialized) {
            if (!mInitialized) {
                initWebStuff(activity);
            }
        }
        mWebView = new android.webkit.WebView(activity);
    }

    private static void initWebStuff(Activity activity) {
        mWebViewClient = new RhoWebViewClient();
        mInitialized = true;
    }
    
    public void applyWebSettings() {
        PerformOnUiThread.exec(new Runnable() {
            @Override
            public void run() {
                mWebView.setVerticalScrollBarEnabled(true);
                mWebView.setHorizontalScrollBarEnabled(true);
                mWebView.setVerticalScrollbarOverlay(true);
                mWebView.setHorizontalScrollbarOverlay(true);
                mWebView.setFocusableInTouchMode(true);
                AndroidFunctionalityManager.getAndroidFunctionality().applyWebSettings(mWebView);
                if (mChromeClient != null) {
                    mWebView.setWebChromeClient(mChromeClient);
                }
                mWebView.setWebViewClient(mWebViewClient);
                mWebView.clearCache(true);
            }
        });
    }

    @Override
    public void setWebClient(Activity activity) {
        Logger.I(TAG, "Creating new RhoWebChromeClient");
        mChromeClient = new RhoWebChromeClient(activity);
        PerformOnUiThread.exec(new Runnable() {
            @Override
            public void run() {
                Logger.I(TAG, "Setting RhoWebChromeClient");
                mWebView.setWebChromeClient(mChromeClient);
            }
        });
    }

    @Override
    public View getView() {
        return mWebView;
    }

    @Override
    public void setContainerView(ViewGroup view) {
        mContainerView = view;
    }

    @Override
    public ViewGroup getContainerView() {
        return mContainerView;
    }

    @Override
    public boolean canGoBack() {
        return mWebView.canGoBack();
    }

    @Override
    public void goBack() {
        mWebView.goBack();
    }

    @Override
    public void goForward() {
        mWebView.goForward();
    }

    @Override
    public void reload() {
        mWebView.reload();
    }

    @Override
    public void clear() {
        mWebView.clearView();
        mWebView.clearCache(true);
        mWebView.invalidate();
        mWebView.loadData("", "", "");
    }

    @Override
    public String getUrl() {
        return mWebView.getUrl();
    }

    @Override
    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    @Override
    public void loadData(String data, String mime, String encoding) {
        mWebView.loadData(data, mime, encoding);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        mWebView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    @Override
    public void stopLoad() {
        mWebView.stopLoading();
    }

    @Override
    public void setZoom(double scale) {
        mWebView.setInitialScale((int)(scale * 100));
    }

    @Override
    public void setTextZoom(TextZoom zoom) {
        WebSettings.TextSize googleTextZoom = WebSettings.TextSize.NORMAL;
        mTextZoom = TextZoom.NORMAL;
        if (zoom == TextZoom.SMALLEST)
        {
            googleTextZoom = WebSettings.TextSize.SMALLEST;
            mTextZoom = TextZoom.SMALLEST;
        }
        else if (zoom == TextZoom.SMALLER)
        {
            googleTextZoom = WebSettings.TextSize.SMALLER;
            mTextZoom = TextZoom.SMALLER;
        }
        else if (zoom == IRhoWebView.TextZoom.NORMAL)
        {
            googleTextZoom = WebSettings.TextSize.NORMAL;
            mTextZoom = TextZoom.NORMAL;
        }
        else if (zoom == IRhoWebView.TextZoom.LARGER)
        {
            googleTextZoom = WebSettings.TextSize.LARGER;
            mTextZoom = TextZoom.LARGER;
        }
        else if (zoom == IRhoWebView.TextZoom.LARGEST)
        {
            googleTextZoom = WebSettings.TextSize.LARGEST;
            mTextZoom = TextZoom.LARGEST;
        }
        mWebView.getSettings().setTextSize(googleTextZoom);
    }
    
    @Override
    public TextZoom getTextZoom()
    {
	return mTextZoom;
    }

    @Override
    public String getEngineId() {
        return "WEBKIT/GOOGLE/" + Build.VERSION.RELEASE;
    }

    @Override
    public void onPause() {
        AndroidFunctionalityManager.getAndroidFunctionality().pauseWebView(mWebView,true);
    }

    @Override
    public void onResume() {
        AndroidFunctionalityManager.getAndroidFunctionality().pauseWebView(mWebView,false);
    }

    @Override
    public void destroy() {
        mWebView.destroy();
    }

    @Override
    public void capture(CaptureFormat format, String path) {
        switch (format) {
        case CAPTURE_FORMAT_HTML:
            Logger.T(TAG, "Capturing current page as HTML archive: " + path);
            mWebView.saveWebArchive(path);
            break;
        case CAPTURE_FORMAT_JPEG:
            Logger.T(TAG, "Capturing current page as JPEG image: " + path);
            saveJpeg(path);
            break;
        default:
            //Should never happen
            Logger.E(TAG, "Wrong capture format.");
            break;
        }
    }
    
    private void saveJpeg(String path) {
        Picture picture = mWebView.capturePicture(); 
        Bitmap bitmap = Bitmap.createBitmap( picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888); 
        Canvas canvas = new Canvas(bitmap); 
        picture.draw(canvas); 
        FileOutputStream fos = null; 
        try { 
            fos = new FileOutputStream(path); 
            if ( fos != null ) 
            { 
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos ); 
                fos.close(); 
            } 
        }
        catch (Throwable e){
            Logger.E(TAG, e);
        }
    }

    @Override
    public void addJSInterface(Object obj, String name) {
        Logger.I(TAG, "Adding new JS interface: " + name);
        mWebView.addJavascriptInterface(obj, name);
    }
}
