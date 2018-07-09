package com.tencent.mylibrary;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by fortunexiao on 2018/7/6.
 */

public class ImageLoader {

    public static final String TAG = ImageLoader.class.getSimpleName();

    private MemoryCache memoryCache;
    private DiskCache diskCache;
    private ImageDownloader imageDownloader ;


    private Handler handler = new Handler(Looper.getMainLooper());

    private Executor executor;

    private volatile static ImageLoader instance;

    /** Returns singleton class instance */
    public static ImageLoader getInstance() {
        if (instance == null) {
            synchronized (ImageLoader.class) {
                if (instance == null) {
                    instance = new ImageLoader();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        memoryCache = new MemoryCache();
        diskCache = new UnlimitedDiskCache(new File("/sdcard/simpleImageLoaderCache"));
        imageDownloader = new BaseImageDownloader(context);

        executor = Executors.newCachedThreadPool();
    }

//    public ImageLoader(Context context) {
//        imageDownloader = new BaseImageDownloader(context);
//    }

    public void display(ImageView iv, String uri) {
        Bitmap bitmap = memoryCache.get(uri);
        if (bitmap != null && !bitmap.isRecycled()) {
            iv.setImageBitmap(bitmap);
            return;
        }

        LoadTask lt = new LoadTask(memoryCache, diskCache, imageDownloader, iv, uri, handler);
        executor.execute(lt);
    }
}

class LoadTask implements Runnable {

    private final MemoryCache memoryCache;
    private final DiskCache diskCache;
    private final ImageDownloader imageDownloader ;


    private final ImageView iv;
    private final String uri;
    private final Handler handler;


    public LoadTask(MemoryCache memoryCache, DiskCache diskCache, ImageDownloader imageDownloader, ImageView iv, String uri, Handler handler) {
        this.memoryCache = memoryCache;
        this.diskCache = diskCache;
        this.imageDownloader = imageDownloader;
        this.iv = iv;
        this.uri = uri;
        this.handler = handler;
    }

    @Override
    public void run() {

        Bitmap bitmap = null;
        File diskFile = diskCache.get(uri);

        try {
            if (diskFile != null && diskFile.exists()) {

                InputStream imageStream = imageDownloader.getStream(ImageDownloader.Scheme.FILE.wrap(diskFile.getAbsolutePath()), null);
                bitmap = BitmapFactory.decodeStream(imageStream, null, null);
            }

            if (bitmap == null) {
                InputStream inputStream = imageDownloader.getStream(uri, null);
                diskCache.save(uri, inputStream, null);
                bitmap = BitmapFactory.decodeStream(inputStream, null, null);
            }
        } catch (IllegalArgumentException e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // 加载失败
                }
            });
            return;
        } catch (IOException e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // 加载失败
                }
            });
            return;
        }

        if (bitmap != null) {
            memoryCache.put(uri, bitmap);
            final  Bitmap bitmap1 = bitmap;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    iv.setImageBitmap(bitmap1);
                }
            });
        }
    }
}



@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
class MemoryCache {

    private LruCache<String, Bitmap> mCache = new LruCache<>(20);

    public void put(String key, Bitmap bitmap) {
        mCache.put(key, bitmap);
    }

    public Bitmap get(String key) {
        return mCache.get(key);
    }
}
