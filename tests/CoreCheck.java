package ru.photolens.app;
import java.util.*;
public class CoreCheck {
 static int checks;
 static void check(boolean ok,String name){checks++;if(!ok)throw new AssertionError(name);}
 public static void main(String[] args){
  check(Arrays.equals(PhotoMath.fit(3000,4000,1280),new int[]{960,1280}),"portrait preserves aspect ratio");
  check(Arrays.equals(PhotoMath.fit(4000,3000,1280),new int[]{1280,960}),"landscape preserves aspect ratio");
  check(Arrays.equals(PhotoMath.fit(320,240,1280),new int[]{320,240}),"small photos are not upscaled");
  check(Arrays.equals(PhotoMath.fit(1,10000,1280),new int[]{1,1280}),"thin panorama stays nonzero");
  boolean invalid=false;try{PhotoMath.fit(0,10,1280);}catch(IllegalArgumentException e){invalid=true;}check(invalid,"invalid dimensions rejected");
  check(PhotoMath.sample(10000,5000,1280)==4,"bounded legacy bitmap sampling");
  check(PhotoMath.sample(1280,960,1280)==1,"no unnecessary sampling");
  List<Labels.Prediction> input=Arrays.asList(new Labels.Prediction("Dog",.7f),new Labels.Prediction("Cat",.9f),new Labels.Prediction("cat",.5f),new Labels.Prediction("Tree",.2f),new Labels.Prediction("Bad",Float.NaN),new Labels.Prediction("Invalid",1.1f));
  List<Labels.Prediction> result=Labels.top(input,.35f,5);
  check(result.size()==2,"remove low-confidence invalid and duplicate predictions");
  check(result.get(0).label.equals("Cat"),"sort strongest prediction first");
  check(result.get(1).label.equals("Dog"),"retain valid alternatives");
  check(Labels.top(input,.35f,1).size()==1,"result limit");
  check(Labels.top(input,.35f,0).isEmpty(),"zero result limit");
  check(Labels.top(Collections.emptyList(),.35f,5).isEmpty(),"empty inference output");
  check(Labels.russian("Cat").equals("Кошка"),"common labels translated");
  check(Labels.russian("Unmapped category").equals("Unmapped category"),"unknown labels retain model output");
  System.out.println("PASS: "+checks+" image preprocessing and prediction checks");
 }
}
