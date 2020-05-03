package com.example.baidumap;

import android.app.Activity;
import android.widget.Button;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

//这个类封装百度的各种绘制操作
//包括marker / overlay等等
public class PaintHelper {
    /**
     *
     * @param mBaiduMap     传入需要显示的地图
     * @param point         生成点marker的经纬度
     * @param drawableResource  要绘制的icon
     */
    public static void makePointMarker(BaiduMap mBaiduMap, LatLng point, final int drawableResource){
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(drawableResource);
        //构建MarkerOption
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker
        mBaiduMap.addOverlay(option);
    }

    //用来画出自己的路线，传入需要现实的地图和坐标点即可
    public static void drawMyRoute(BaiduMap mBaiduMap, List<LatLng> points){
        //创建纹理显示
        BitmapDescriptor mRedTexture = BitmapDescriptorFactory.fromAsset("Icon_road_red_arrow.png");
        BitmapDescriptor mBlueTexture = BitmapDescriptorFactory.fromAsset("Icon_road_blue_arrow.png");
        BitmapDescriptor mGreenTexture = BitmapDescriptorFactory.fromAsset("Icon_road_green_arrow.png");
        //创建纹理列表
        List<BitmapDescriptor> textureList = new ArrayList<>();
        textureList.add(mRedTexture);
        textureList.add(mBlueTexture);
        textureList.add(mGreenTexture);
        //添加纹理索引
        List<Integer> textureIndexList = new ArrayList<>();
        textureIndexList.add(0);
        textureIndexList.add(1);
        textureIndexList.add(2);

        OverlayOptions options = new PolylineOptions()
                .width(20)
                .dottedLine(true)
                .points(points)
                .customTextureList(textureList)
                .textureIndex(textureIndexList);
        mBaiduMap.addOverlay(options);
    }

    //创建一个text覆盖物
    public static void makeTextMarker(BaiduMap mBaiduMap, LatLng point, String text, int bgColor, int fontSize, int fontColor){
        OverlayOptions option = new TextOptions()
                .text(text)
                .bgColor(bgColor)
                .fontSize(fontSize)
                .fontColor(fontColor)
                .position(point);
        mBaiduMap.addOverlay(option);
    }

    //创建一个info window
    //因为使用Button创建，为了设置Button的点击事件，所以返回那个Button对象，需要注意接收，否则不能设置点击事件
    public static Button makeInfoWindow(Activity activity, BaiduMap mBaiduMap, LatLng point, String text){
        //使用Button创建
        Button button = new Button(activity.getApplicationContext());
        button.setBackgroundResource(R.drawable.button_info_window);
        button.setText(text);

        InfoWindow mInfowWindow = new InfoWindow(button,point,-100);
        mBaiduMap.showInfoWindow(mInfowWindow);
        return button;
    }


}
