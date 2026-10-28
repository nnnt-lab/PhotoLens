package ru.photolens.app;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.util.AtomicFile;
import android.view.*;
import android.widget.*;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.*;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private final int BG=Color.rgb(246,245,250),INK=Color.rgb(37,34,57),MUTED=Color.rgb(115,111,132),
        ACCENT=Color.rgb(98,81,198),PALE=Color.rgb(236,232,252);
    private static final float THRESHOLD=.35f;
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    private volatile int generation=0;
    private boolean destroyed=false,busy=false;
    private Bitmap photo;
    private ImageLabeler labeler;
    private LinearLayout root,results;
    private ImageView preview;
    private TextView placeholder,status,choose,repeat,clear;
    private ProgressBar loading;
    private AtomicFile selectedFile(){return new AtomicFile(new File(getFilesDir(),"selected.jpg"));}

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
        try{
            labeler=ImageLabeling.getClient(new ImageLabelerOptions.Builder().setConfidenceThreshold(THRESHOLD).build());
        }catch(RuntimeException e){
            status.setText("Модель не удалось запустить. Перезапустите приложение.");
            choose.setEnabled(false);return;
        }
        if(selectedFile().getBaseFile().exists())load(Uri.fromFile(selectedFile().getBaseFile()),false);
    }
    private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
    private GradientDrawable background(int color,int radius){
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;
    }
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private TextView text(String value,int size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setLineSpacing(dp(3),1);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;
    }
    private void space(LinearLayout parent,int n){parent.addView(new View(this),new LinearLayout.LayoutParams(1,dp(n)));}
    private LinearLayout card(LinearLayout parent){
        LinearLayout c=column();c.setPadding(dp(20),dp(20),dp(20),dp(20));c.setBackground(background(Color.WHITE,24));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(14);parent.addView(c,p);return c;
    }
    private TextView button(String title,boolean primary,Runnable action){
        TextView t=text(title,15,primary?Color.WHITE:ACCENT,true);t.setGravity(Gravity.CENTER);t.setMinHeight(dp(52));
        t.setPadding(dp(14),dp(14),dp(14),dp(14));t.setBackground(background(primary?ACCENT:PALE,16));t.setOnClickListener(v->action.run());return t;
    }
    private void buildUi(){
        root=column();root.setBackgroundColor(BG);setContentView(root);
        if(Build.VERSION.SDK_INT>=30)root.setOnApplyWindowInsetsListener((v,insets)->{
            Insets b=insets.getInsets(WindowInsets.Type.systemBars());root.setPadding(b.left,b.top,b.right,b.bottom);return insets;
        });
        else root.setOnApplyWindowInsetsListener((v,insets)->{
            root.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;
        });
        root.requestApplyInsets();
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);root.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout page=column();page.setPadding(dp(22),dp(26),dp(22),dp(24));scroll.addView(page);
        page.addView(text("ФОТОСКАН  /  1.0",12,ACCENT,true));space(page,10);
        page.addView(text("Что на фотографии?",30,INK,true));space(page,8);
        page.addView(text("Выберите снимок — нейросеть распознает его содержимое прямо на телефоне.",15,MUTED,false));space(page,22);
        LinearLayout imageCard=card(page);imageCard.setPadding(dp(10),dp(10),dp(10),dp(10));
        FrameLayout frame=new FrameLayout(this);frame.setBackground(background(PALE,18));
        int height=Math.min(330,(int)(getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density*.72));
        imageCard.addView(frame,new LinearLayout.LayoutParams(-1,dp(height)));
        preview=new ImageView(this);preview.setScaleType(ImageView.ScaleType.FIT_CENTER);preview.setContentDescription("Выбранная фотография");frame.addView(preview,new FrameLayout.LayoutParams(-1,-1));
        placeholder=text("＋\nВаша фотография\nJPG · PNG · другие доступные форматы",17,ACCENT,true);
        placeholder.setGravity(Gravity.CENTER);placeholder.setPadding(dp(20),dp(20),dp(20),dp(20));frame.addView(placeholder,new FrameLayout.LayoutParams(-1,-1));
        choose=button("Выбрать фотографию",true,this::pick);page.addView(choose);space(page,10);
        LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);
        repeat=button("Распознать снова",false,()->{if(photo!=null&&!busy)classify(photo,++generation);});
        actions.addView(repeat,new LinearLayout.LayoutParams(0,-2,1));
        clear=button("Очистить",false,this::clearPhoto);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-2,-2);cp.leftMargin=dp(8);actions.addView(clear,cp);page.addView(actions);space(page,18);
        loading=new ProgressBar(this);loading.setIndeterminate(true);loading.setVisibility(View.GONE);page.addView(loading,new LinearLayout.LayoutParams(-1,dp(36)));
        status=text("Готово к работе • модель встроена в приложение",13,MUTED,false);page.addView(status);space(page,18);
        results=column();page.addView(results);
        LinearLayout info=card(page);info.setBackground(background(PALE,20));
        info.addView(text("Модель уже на телефоне",16,ACCENT,true));space(info,6);
        info.addView(text("ML Kit Image Labeling • 400+ категорий. Анализ фотографии работает без интернета.",13,INK,false));
        space(info,10);info.addView(text("Оценка уверенности — ответ модели, а не гарантия. На фото могут одновременно распознаваться предметы, животные и сцены. Имена людей модель не определяет.",12,MUTED,false));
        setBusy(false);
    }
    private void setBusy(boolean value){
        busy=value;loading.setVisibility(value?View.VISIBLE:View.GONE);
        repeat.setEnabled(!value&&photo!=null);repeat.setAlpha(repeat.isEnabled()?1:.45f);
        clear.setEnabled(photo!=null||value);clear.setAlpha(clear.isEnabled()?1:.45f);
    }
    private void pick(){
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE);
        try{startActivityForResult(intent,101);}catch(ActivityNotFoundException e){status.setText("На устройстве нет приложения для выбора файлов.");}
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==101&&result==RESULT_OK&&data!=null&&data.getData()!=null)load(data.getData(),true);
    }
    private void load(Uri uri,boolean persist){
        int token=++generation;setBusy(true);status.setText("Подготавливаю фотографию…");results.removeAllViews();
        io.execute(()->{
            Bitmap decoded=null;
            try{
                decoded=PhotoLoader.decode(this,uri);
                if(token!=generation){decoded.recycle();return;}
                if(persist){
                    AtomicFile file=selectedFile();FileOutputStream stream=null;
                    try{
                        stream=file.startWrite();
                        if(!decoded.compress(Bitmap.CompressFormat.JPEG,92,stream))throw new IOException("Не удалось сохранить фотографию");
                        file.finishWrite(stream);
                    }catch(IOException e){if(stream!=null)file.failWrite(stream);throw e;}
                }
                Bitmap ready=decoded;
                main.post(()->{
                    if(destroyed||token!=generation){ready.recycle();return;}
                    photo=ready;preview.setImageBitmap(ready);placeholder.setVisibility(View.GONE);
                    classify(ready,token);
                });
            }catch(Exception | OutOfMemoryError e){
                if(decoded!=null)decoded.recycle();
                main.post(()->{
                    if(destroyed||token!=generation)return;
                    setBusy(false);
                    status.setText(R.string.photo_open_error);
                    LinearLayout c=card(results);c.addView(text("Фотография не прочитана",20,INK,true));space(c,6);
                    c.addView(text("Файл может быть повреждён, слишком большим или недоступным. Распознавание не выполнялось.",14,MUTED,false));
                });
            }
        });
    }
    private void classify(Bitmap bitmap,int token){
        if(destroyed||labeler==null)return;
        setBusy(true);results.removeAllViews();status.setText("Нейросеть анализирует фотографию…");
        long started=SystemClock.elapsedRealtime();
        try{
            labeler.process(InputImage.fromBitmap(bitmap,0))
                .addOnSuccessListener(labels->{
                    if(destroyed||token!=generation)return;
                    List<Labels.Prediction> predictions=new ArrayList<>();
                    for(ImageLabel label:labels)predictions.add(new Labels.Prediction(label.getText(),label.getConfidence()));
                    showResults(Labels.top(predictions,THRESHOLD,5),SystemClock.elapsedRealtime()-started);
                })
                .addOnFailureListener(error->{
                    if(destroyed||token!=generation)return;
                    setBusy(false);status.setText("Модель не смогла обработать снимок. Нажмите «Распознать снова» или выберите другое фото.");
                });
        }catch(RuntimeException e){setBusy(false);status.setText("Не удалось запустить анализ. Попробуйте другую фотографию.");}
    }
    private void showResults(List<Labels.Prediction> predictions,long millis){
        setBusy(false);results.removeAllViews();
        status.setText(getString(R.string.analysis_complete,millis,photo.getWidth(),photo.getHeight()));
        if(predictions.isEmpty()){
            LinearLayout c=card(results);c.addView(text("Нет уверенного ответа",23,INK,true));space(c,8);
            c.addView(text("Ни одна категория не набрала 35%. Попробуйте чёткое фото с хорошо видимым объектом.",14,MUTED,false));return;
        }
        Labels.Prediction first=predictions.get(0);LinearLayout mainCard=card(results);
        mainCard.setBackground(background(ACCENT,24));
        mainCard.addView(text(first.confidence>=.7f?"ПОХОЖЕ НА":"ВОЗМОЖНЫЙ ВАРИАНТ",11,Color.rgb(221,215,255),true));space(mainCard,10);
        mainCard.addView(text(Labels.russian(first.label),28,Color.WHITE,true));space(mainCard,6);
        mainCard.addView(text("Уверенность модели: "+percent(first.confidence),15,Color.WHITE,false));
        LinearLayout all=card(results);all.addView(text("Что распознала модель",19,INK,true));space(all,14);
        for(Labels.Prediction prediction:predictions){
            String translated=Labels.russian(prediction.label);
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
            row.addView(text(translated,16,INK,true),new LinearLayout.LayoutParams(0,-2,1));
            row.addView(text(percent(prediction.confidence),15,ACCENT,true));all.addView(row);
            if(!translated.equals(prediction.label))all.addView(text(prediction.label+" • исходная метка",11,MUTED,false));
            else all.addView(text("Метка предобученной модели",11,MUTED,false));
            space(all,6);ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
            bar.setMax(1000);bar.setProgress(Math.round(prediction.confidence*1000));
            bar.setProgressTintList(android.content.res.ColorStateList.valueOf(ACCENT));all.addView(bar);space(all,14);
        }
    }
    private String percent(float value){return String.format(new Locale("ru"),"%.1f %%",value*100);}
    private void clearPhoto(){
        generation++;photo=null;preview.setImageDrawable(null);placeholder.setVisibility(View.VISIBLE);results.removeAllViews();setBusy(false);
        status.setText("Выберите фотографию для нового анализа.");
        io.execute(()->selectedFile().delete());
    }
    @Override protected void onDestroy(){
        destroyed=true;generation++;io.shutdown();
        if(labeler!=null)labeler.close();main.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
