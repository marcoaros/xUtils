/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lidroid.xutils.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.lidroid.xutils.bitmap.callback.BitmapSetter;
import com.lidroid.xutils.bitmap.core.BitmapSize;
import com.lidroid.xutils.util.LogUtils;

import java.io.File;
import java.lang.reflect.Field;

public class BitmapCommonUtils {

    /**
     * @param context
     * @param dirName Only the folder name, not contain full path.
     * @return app_cache_path/dirName
     */
    public static String getDiskCacheDir(Context context, String dirName) {
        final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ?
                context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();

        return cachePath + File.separator + dirName;
    }

    public static long getAvailableSpace(File dir) {
        try {
            final StatFs stats = new StatFs(dir.getPath());
            return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
            return -1;
        }

    }

    private static BitmapSize screenSize = null;

    public static BitmapSize getScreenSize(Context context) {
        if (screenSize == null) {
            screenSize = new BitmapSize();
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            screenSize.setWidth(displayMetrics.widthPixels);
            screenSize.setHeight(displayMetrics.heightPixels);
        }
        return screenSize;
    }

    public static BitmapSize optimizeMaxSizeByView(View view, int maxImageWidth, int maxImageHeight) {
        final BitmapSize screenSize = getScreenSize(view.getContext());

        final ViewGroup.LayoutParams params = view.getLayoutParams();
        int width = (params != null && params.width == ViewGroup.LayoutParams.WRAP_CONTENT) ? 0 : view.getWidth(); // Get actual image width
        if (width <= 0 && params != null) width = params.width; // Get layout width parameter
        if (width <= 0) width = getFieldValue(view, "mMaxWidth"); // Check maxWidth parameter
        if (width <= 0) width = maxImageWidth;
        if (width <= 0) width = screenSize.getWidth();

        int height = (params != null && params.height == ViewGroup.LayoutParams.WRAP_CONTENT) ? 0 : view.getHeight(); // Get actual image height
        if (height <= 0 && params != null) height = params.height; // Get layout height parameter
        if (height <= 0) height = getFieldValue(view, "mMaxHeight"); // Check maxHeight parameter
        if (height <= 0) height = maxImageHeight;
        if (height <= 0) height = screenSize.getHeight();

        return new BitmapSize(width, height);
    }

    public static final ImageViewSetter sDefaultImageViewSetter = new ImageViewSetter();

    public static class ImageViewSetter implements BitmapSetter<ImageView> {

        @Override
        public void setBitmap(ImageView container, Bitmap bitmap) {
            container.setImageBitmap(bitmap);
        }

        @Override
        public void setDrawable(ImageView container, Drawable drawable) {
            container.setImageDrawable(drawable);
        }

        @Override
        public Drawable getDrawable(ImageView container) {
            return container.getDrawable();
        }
    }

    private static int getFieldValue(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Throwable e) {
        }
        return value;
    }
}