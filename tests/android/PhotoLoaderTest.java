package ru.photolens.app;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.io.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=33)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class PhotoLoaderTest {
 Context context;
 @Before public void setup(){context=RuntimeEnvironment.getApplication();}
 File image(int w,int h)throws Exception {
  Bitmap bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
  File file=File.createTempFile("photo",".png",context.getCacheDir());
  try(FileOutputStream out=new FileOutputStream(file)){assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));}
  bitmap.recycle();return file;
 }
 @Test public void selectedPngDecodesAndTransparentPixelsBecomeWhite()throws Exception {
  Bitmap result=PhotoLoader.decode(context,Uri.fromFile(image(40,20)));
  assertEquals(40,result.getWidth());assertEquals(20,result.getHeight());assertEquals(Color.WHITE,result.getPixel(0,0));
 }
 @Test public void hugePhotoIsReducedBeforeInference()throws Exception {
  Bitmap result=PhotoLoader.decode(context,Uri.fromFile(image(1500,3000)));
  assertEquals(640,result.getWidth());assertEquals(1280,result.getHeight());
 }
 @Test public void invalidPhotoReportsFailure()throws Exception {
  File file=File.createTempFile("invalid",".png",context.getCacheDir());
  try(FileOutputStream out=new FileOutputStream(file)){out.write(new byte[]{1,2,3});}
  boolean failed=false;try{PhotoLoader.decode(context,Uri.fromFile(file));}catch(IOException|RuntimeException e){failed=true;}
  assertTrue(failed);
 }
 @Test public void cameraRotationAndMirroringMatricesAreApplied(){
  float[] point={2,3};PhotoLoader.orientationMatrix(6).mapPoints(point);
  assertEquals(-3,point[0],.001);assertEquals(2,point[1],.001);
  float[] mirror={2,3};PhotoLoader.orientationMatrix(2).mapPoints(mirror);
  assertEquals(-2,mirror[0],.001);assertEquals(3,mirror[1],.001);
 }
}
