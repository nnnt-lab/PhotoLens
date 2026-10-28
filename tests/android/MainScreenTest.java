package ru.photolens.app;
import android.app.Activity;
import android.content.Intent;
import android.view.*;
import android.widget.TextView;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=33)
public class MainScreenTest {
 TextView find(View v,String value){
  if(v instanceof TextView&&value.contentEquals(((TextView)v).getText()))return (TextView)v;
  if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){
   TextView found=find(((ViewGroup)v).getChildAt(i),value);if(found!=null)return found;
  }
  return null;
 }
 @Test public void realModelClientInitializesAndUserCanOpenPhotoPicker(){
  android.content.pm.ProviderInfo info=new android.content.pm.ProviderInfo();
  info.authority="ru.photolens.app.mlkitinitprovider";
  new com.google.mlkit.common.internal.MlKitInitProvider().attachInfo(RuntimeEnvironment.getApplication(),info);
  try(org.robolectric.android.controller.ActivityController<MainActivity> ctl=Robolectric.buildActivity(MainActivity.class).create().start().resume()){
   MainActivity app=ctl.get();
   TextView choose=find(app.getWindow().getDecorView(),"Выбрать фотографию");
   assertNotNull(choose);assertTrue("Bundled ML Kit client must initialize",choose.isEnabled());
   choose.performClick();
   Intent intent=shadowOf(app).getNextStartedActivityForResult().intent;
   assertEquals(Intent.ACTION_OPEN_DOCUMENT,intent.getAction());assertEquals("image/*",intent.getType());
  }
 }
}
