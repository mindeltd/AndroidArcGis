package com.example.androidarcgis.models;

public class LayerSettings {

    private String layerName;
//    private int recommendedMaxcScale;
//    private int recommendedMinSale;
    private int maxScale;
    private int minScale;
    private int active;
    private int color;

    public LayerSettings(String layerName, int maxScale, int minScale,int active, int color){
        this.layerName = layerName;
//        this.recommendedMaxcScale = recommendedMaxcScale;
//        this.recommendedMinSale = recommendedMinSale;
        this.maxScale = maxScale;
        this.minScale = minScale;
        this.active = active;
        this.color = color;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

//    public int getRecommendedMaxcScale() {
//        return recommendedMaxcScale;
//    }
//
//    public void setRecommendedMaxcScale(int recommendedMaxcScale) {
//        this.recommendedMaxcScale = recommendedMaxcScale;
//    }
//
//    public int getRecommendedMinSale() {
//        return recommendedMinSale;
//    }
//
//    public void setRecommendedMinSale(int recommendedMinSale) {
//        this.recommendedMinSale = recommendedMinSale;
//    }


    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(int maxScale) {
        this.maxScale = maxScale;
    }

    public int getMinScale() {
        return minScale;
    }

    public void setMinScale(int minScale) {
        this.minScale = minScale;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

}
