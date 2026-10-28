package ru.photolens.app;

public final class PhotoMath {
    private PhotoMath(){}
    public static int[] fit(int width,int height,int max){
        if(width<=0||height<=0||max<=0)throw new IllegalArgumentException("Некорректный размер изображения");
        double ratio=Math.min(1.0,(double)max/Math.max(width,height));
        return new int[]{Math.max(1,(int)Math.round(width*ratio)),Math.max(1,(int)Math.round(height*ratio))};
    }
    public static int sample(int width,int height,int max){
        if(width<=0||height<=0||max<=0)throw new IllegalArgumentException("Некорректный размер изображения");
        int sample=1;
        while(Math.max(width,height)/(long)sample>max*2L)sample*=2;
        return sample;
    }
}
