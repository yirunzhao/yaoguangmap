package com.example.baidumap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapBaseIndoorMapInfo;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorPlanNode;
import com.baidu.mapapi.search.route.IndoorRoutePlanOption;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.util.ArrayList;

import mapapi.overlayutil.IndoorRouteOverlay;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private ArrayList<String> indoorFloors;
    private MapBaseIndoorMapInfo indoorMapInfo;
    private boolean isIndoor = false;
    private ListAdapter adapter;
    private PoiSearch mPoiSearch;
    private PoiResult poiIndoorResult;
    private RoutePlanSearch mSearch;

    private Button btnSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        // 初始化地图
        initMapView();
        // 初始化室内地图
        initIndoorSettings();
        // 初始化定位信息

        btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initIndoorRoutePlan();
            }
        });




    }

    private void initMapView(){
        //这是设置坐标
        SDKInitializer.setCoordType(CoordType.BD09LL);
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        //设置初始位置 39.917380 116.37978
//        LatLng whu = new LatLng(30.533334,114.3617);
        LatLng whu = new LatLng(39.917380,116.37978);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(whu));

        //设置室内信息
        //myPoiSearchResultListener = new MyPoiSearchResultListener(mBaiduMap);
    }

    private void initLocation(){
        mBaiduMap.setMyLocationEnabled(true);

    }

    private void initIndoorSettings(){
        mBaiduMap.setIndoorEnable(true);
        mBaiduMap.setOnBaseIndoorMapListener(new BaiduMap.OnBaseIndoorMapListener() {
            @Override
            public void onBaseIndoorMapMode(boolean b, MapBaseIndoorMapInfo mapBaseIndoorMapInfo) {
                if (b && mapBaseIndoorMapInfo != null){
                    Log.d("进入室内图","ok");
                    isIndoor = true;
                    indoorMapInfo = mapBaseIndoorMapInfo;
                    indoorFloors = mapBaseIndoorMapInfo.getFloors();

                }else{
                    Log.d("出了室内地图","asdasd");
                }
            }
        });

        // 设置点击室内地图事件
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(isIndoor){
                    showIndoorDialog();
                }
            }

            @Override
            public void onMapPoiClick(MapPoi mapPoi) {

            }
        });
    }
    private void showIndoorDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("楼层选择");

        final String[] floors = indoorFloors.toArray(new String[]{});
        Log.d("楼层",floors.length+"|");
        builder.setItems(floors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switchFloor(floors[which]);
            }
        });

        builder.show();
    }
    public void switchFloor(String floor){
        MapBaseIndoorMapInfo.SwitchFloorError switchFloorError =  mBaiduMap.switchBaseIndoorMapFloor(floor, indoorMapInfo.getID()); // 切换楼层信息
        switch (switchFloorError) {
            case SWITCH_OK:
                Log.d("OK","切换成功");
                break;
            case FLOOR_INFO_ERROR:
                Log.d("ERROR","ID错误");
                break;
            case FLOOR_OVERLFLOW:
                Log.d("OVERFLOW","切换楼层室内ID与当前聚焦室内ID不匹配");
                break;
            case FOCUSED_ID_ERROR:
                Log.d("ERROR","切换楼层室内ID与当前聚焦室内ID不匹配");
                break;
            case SWITCH_ERROR:
                Log.d("ERROR","切换楼层错误");
                break;
            default:
                break;
        }
    }

    private void initIndoorRoutePlan(){
        mSearch = RoutePlanSearch.newInstance();
        OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
                IndoorRouteOverlay overlay = new IndoorRouteOverlay(mBaiduMap);
                if(indoorRouteResult.getRouteLines() != null && indoorRouteResult.getRouteLines().size() > 0){
                    Log.d("在室内搜索","进入了这里");
                    overlay.setData(indoorRouteResult.getRouteLines().get(0));
                    overlay.addToMap();
                }else{
                    Toast.makeText(MainActivity.this, "这里是else", Toast.LENGTH_SHORT).show();
                    Log.d("室内导航错误信息",indoorRouteResult.error.toString());
                    Log.d("室内搜索的路径条数", indoorRouteResult.getRouteLines()==null?"是null":indoorRouteResult.getRouteLines().size()+"");
                }
            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        };
        mSearch.setOnGetRoutePlanResultListener(listener);
        IndoorPlanNode startNode = new IndoorPlanNode(new LatLng(39.917380, 116.37978), "F1");
        IndoorPlanNode endNode = new IndoorPlanNode(new LatLng(39.917239, 116.37955), "F6");
        mSearch.walkingIndoorSearch(new IndoorRoutePlanOption()
                .from(startNode)
                .to(endNode));
    }

    private void initIndoorPoiSearch(){
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                poiIndoorResult = poiResult;
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }

            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        });
//        mPoiSearch.searchPoiIndoor(new PoiIndoorOption().bid(poiIndoorResult.get))
    }


    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mSearch.destroy();
    }

}

