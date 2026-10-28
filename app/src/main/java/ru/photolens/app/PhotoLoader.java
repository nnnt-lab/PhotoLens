package ru.photolens.app;

import android.content.Context;
import android.graphics.*;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import java.io.*;

public final class PhotoLoader {
    private PhotoLoader(){}
    public static Bitmap decode(Context context,Uri uri)throws IOException {
        Bitmap bitmap;
        if(Build.VERSION.SDK_INT>=28) {
            bitmap=ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.getContentResolver(),uri),(decoder,info,source)->{
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB));
                int[] size=PhotoMath.fit(info.getSize().getWidth(),info.getSize().getHeight(),1280);
                decoder.setTargetSize(size[0],size[1]);
            });
        }else{
            BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;
            try(InputStream stream=open(context,uri)){BitmapFactory.decodeStream(stream,null,bounds);}
            if(bounds.outWidth<=0||bounds.outHeight<=0)throw new IOException("Формат изображения не поддерживается");
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize=PhotoMath.sample(bounds.outWidth,bounds.outHeight,1280);
            options.inPreferredConfig=Bitmap.Config.ARGB_8888;
            try(InputStream stream=open(context,uri)){bitmap=BitmapFactory.decodeStream(stream,null,options);}
            if(bitmap==null)throw new IOException("Не удалось прочитать изображение");
            int orientation=ExifInterface.ORIENTATION_NORMAL;
            try(InputStream stream=open(context,uri)){
                orientation=new ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            }catch(IOException ignored){ /* Formats without EXIF remain unrotated. */ }
            Matrix m=orientationMatrix(orientation);
            Bitmap rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),m,true);
            if(rotated!=bitmap)bitmap.recycle();bitmap=rotated;
        }
        int[] size=PhotoMath.fit(bitmap.getWidth(),bitmap.getHeight(),1280);
        Bitmap scaled=Bitmap.createScaledBitmap(bitmap,size[0],size[1],true);
        if(scaled!=bitmap)bitmap.recycle();
        Bitmap rgb=Bitmap.createBitmap(scaled.getWidth(),scaled.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(rgb);canvas.drawColor(Color.WHITE);canvas.drawBitmap(scaled,0,0,null);
        scaled.recycle();return rgb;
    }
    private static InputStream open(Context context,Uri uri)throws IOException {
        InputStream stream=context.getContentResolver().openInputStream(uri);
        if(stream==null)throw new FileNotFoundException("Фотография недоступна");return stream;
    }
    static Matrix orientationMatrix(int orientation){
        Matrix m=new Matrix();
        switch(orientation){
            case 2:m.setScale(-1,1);break;
            case 3:m.setRotate(180);break;
            case 4:m.setScale(1,-1);break;
            case 5:m.setRotate(90);m.postScale(-1,1);break;
            case 6:m.setRotate(90);break;
            case 7:m.setRotate(270);m.postScale(-1,1);break;
            case 8:m.setRotate(270);break;
            default:break;
        }
        return m;
    }
}
