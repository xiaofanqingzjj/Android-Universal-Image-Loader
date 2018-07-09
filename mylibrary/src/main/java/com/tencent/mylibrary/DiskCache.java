package com.tencent.mylibrary;

import android.graphics.Bitmap;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface DiskCache {
    /**
	 * 硬盘cache的根目录
	 * @return Root directory of disk cache
	 */
	File getDirectory();

	/**
	 * 返回指定uri的cache文件
	 * @param imageUri Original image URI
	 * @return File of cached image or <b>null</b> if image wasn't cached
	 */
	File get(String imageUri);

	/**
	 * 保存指定uri的图片流
	 *
	 * @param imageUri    Original image URI
	 * @param imageStream Input stream of image (shouldn't be closed in this method)
	 * @param listener    Listener for saving progress, can be ignored if you don't use
	 *                    {@linkplain com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener
	 *                    progress listener} in ImageLoader calls
	 * @return <b>true</b> - if image was saved successfully; <b>false</b> - if image wasn't saved in disk cache.
	 * @throws IOException
	 */
	boolean save(String imageUri, InputStream imageStream, IoUtils.CopyListener listener) throws IOException;

	/**
	 * 保存指定uri的图片对象
	 * Saves image bitmap in disk cache.
	 *
	 * @param imageUri Original image URI
	 * @param bitmap   Image bitmap
	 * @return <b>true</b> - if bitmap was saved successfully; <b>false</b> - if bitmap wasn't saved in disk cache.
	 * @throws IOException
	 */
	boolean save(String imageUri, Bitmap bitmap) throws IOException;

	/**
	 * 删除特定的uri的cache文件
	 * Removes image file associated with incoming URI
	 *
	 * @param imageUri Image URI
	 * @return <b>true</b> - if image file is deleted successfully; <b>false</b> - if image file doesn't exist for
	 * incoming URI or image file can't be deleted.
	 */
	boolean remove(String imageUri);

	/** Closes disk cache, releases resources. */
	void close();

	/** Clears disk cache. */
	void clear();
}
