/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.sketch;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import me.xiaopan.sketch.util.SketchUtils;

public class RecycleBitmapDrawable extends FixedBitmapDrawable implements RecycleDrawableInterface {
    private static final String NAME = "RecycleBitmapDrawable";

    private int cacheRefCount;
    private int displayRefCount;
    private int waitDisplayRefCount;
    private String mimeType;
    private boolean allowRecycle = true;

    public RecycleBitmapDrawable(Bitmap bitmap) {
        super(bitmap);
    }

    @Override
    public void setIsDisplayed(String callingStation, boolean displayed) {
        synchronized (this) {
            if (displayed) {
                displayRefCount++;
            } else {
                if(displayRefCount > 0){
                    displayRefCount--;
                }
            }
        }
        tryRecycle((displayed ? "display" : "hide"), callingStation);
    }

    @Override
    public void setIsCached(String callingStation, boolean cached) {
        synchronized (this) {
            if (cached) {
                cacheRefCount++;
            } else {
                if(cacheRefCount > 0){
                    cacheRefCount--;
                }
            }
        }
        tryRecycle((cached ? "putToCache" : "removedFromCache"), callingStation);
    }

    @Override
    public void setIsWaitDisplay(String callingStation, boolean waitDisplay) {
        synchronized (this) {
            if (waitDisplay) {
                waitDisplayRefCount++;
            } else {
                if(waitDisplayRefCount > 0){
                    waitDisplayRefCount--;
                }
            }
        }
        tryRecycle((waitDisplay ? "waitDisplay" : "displayed"), callingStation);
    }

    @Override
    public int getByteCount() {
        return getByteCount(getBitmap());
    }

    @Override
    public boolean isRecycled() {
        Bitmap bitmap = getBitmap();
        return bitmap == null || bitmap.isRecycled();
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public void recycle() {
        Bitmap bitmap = getBitmap();
        if(bitmap != null){
            bitmap.recycle();
        }
    }

    @Override
    public String getSize() {
        Bitmap bitmap = getBitmap();
        if(bitmap != null){
            return SketchUtils.concat(bitmap.getWidth(), "x", bitmap.getHeight());
        }else{
            return null;
        }
    }

    @Override
    public String getConfig(){
        Bitmap bitmap = getBitmap();
        if(bitmap != null && bitmap.getConfig() != null){
            return bitmap.getConfig().name();
        }else{
            return null;
        }
    }

    @Override
    public String getInfo() {
        Bitmap bitmap = getBitmap();
        if(bitmap != null){
            return SketchUtils.concat("RecycleBitmapDrawable(mimeType=", mimeType, "; hashCode=", Integer.toHexString(bitmap.hashCode()), "; size=", bitmap.getWidth(), "x", bitmap.getHeight(), "; config=", bitmap.getConfig() != null ? bitmap.getConfig().name() : null, "; byteCount=", getByteCount(), ")");
        }else{
            return null;
        }
    }

    @Override
    public boolean canRecycle(){
        return allowRecycle && getBitmap() != null && !getBitmap().isRecycled();
    }

    @Override
    public void setAllowRecycle(boolean allowRecycle) {
        this.allowRecycle = allowRecycle;
    }

    private synchronized void tryRecycle(String type, String callingStation) {
        if (cacheRefCount <= 0 && displayRefCount <= 0 && waitDisplayRefCount <= 0 && canRecycle()) {
            if(Sketch.isDebugMode()){
                Log.w(Sketch.TAG, SketchUtils.concat(NAME, " - ", "recycled bitmap", " - ", callingStation, ":", type, " - ", getInfo()));
            }
            getBitmap().recycle();
        }else{
            if(Sketch.isDebugMode()){
                Log.d(Sketch.TAG, SketchUtils.concat(NAME, " - ", "can't recycled bitmap", " - ", callingStation, ":", type, " - ", getInfo(), " - ", "references(cacheRefCount=", cacheRefCount, "; displayRefCount=", displayRefCount, "; waitDisplayRefCount=", waitDisplayRefCount, "; canRecycle=", canRecycle(), ")"));
            }
        }
    }

    public static String getInfo(Bitmap bitmap, String mimeType) {
        if(bitmap != null){
            return SketchUtils.concat("Bitmap(mimeType=", mimeType, "; hashCode=", Integer.toHexString(bitmap.hashCode()), "; size=", bitmap.getWidth(), "x", bitmap.getHeight(), "; config=", bitmap.getConfig() != null ? bitmap.getConfig().name() : null, "; byteCount=", getByteCount(bitmap), ")");
        }else{
            return null;
        }
    }

    public static int getByteCount(Bitmap bitmap) {
        if(bitmap == null){
            return 0;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return  bitmap.getByteCount();
        }else{
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }
}