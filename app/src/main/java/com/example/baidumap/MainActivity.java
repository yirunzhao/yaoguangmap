package com.example.baidumap;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapBaseIndoorMapInfo;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorInfo;
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
import java.util.List;

import mapapi.overlayutil.IndoorPoiOverlay;
import mapapi.overlayutil.IndoorRouteOverlay;
import mapapi.overlayutil.PoiOverlay;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private ArrayList<String> indoorFloors;
    private MapBaseIndoorMapInfo indoorMapInfo;
    private boolean isIndoor = false;
    private ListAdapter adapter;
    private PoiSearch mPoiSearch;
    private PoiIndoorResult myPoiIndoorResult;
    private RoutePlanSearch mSearch;
    private LocationClient mLocationClient;
    private Button btnSearch;
    private EditText etKeyword;
    private LatLng currentLocation,destinationLocation;
    private MyLocationListener mLocationListener = new MyLocationListener();
    private String currentFloor="1F",currentBuildingID,currentBuildingName,destinationFloor;
    private List<PoiIndoorInfo> indoorInfoList;

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            // 获取定位信息
            double latitude = bdLocation.getLatitude();
            double longitude = bdLocation.getLongitude();
            String floor = bdLocation.getFloor();
            String buildingID = bdLocation.getBuildingID();
            String buildingName = bdLocation.getBuildingName();

            // 赋值
            currentLocation = new LatLng(latitude,longitude);
            currentFloor = floor;
            currentBuildingID = buildingID;
            currentBuildingName = buildingName;
            mLocationClient.startIndoorMode();

            Log.d("定位lat","||"+latitude+"||");
            Log.d("定位building floor","|||"+floor);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        // 初始化地图
        initMapView();
        // 初始化室内地图
        initIndoorSettings();
        // 初始化室内搜索信息
        initIndoorRoutePlan();
        // 初始化定位
        initLocation();
        // 初始化室内poi
        initIndoorPoiSearch();

        btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startIndoorPoiSearch();
            }
        });

        etKeyword = findViewById(R.id.et_keyword);




    }

    private void initMapView(){
        //这是设置坐标
        SDKInitializer.setCoordType(CoordType.BD09LL);
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        //设置初始位置 39.917380 116.37978
        LatLng whu = new LatLng(30.533334,114.3617);
//        LatLng whu = new LatLng(39.917380,116.37978);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(whu,18.0f));


        //设置室内信息
        //myPoiSearchResultListener = new MyPoiSearchResultListener(mBaiduMap);
    }

    private void initLocation(){
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(mLocationListener);
        mLocationClient.start();
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
    private void switchFloor(String floor){
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
                    // 把之前的overlay移除
                    overlay.removeFromMap();
                    // 设置data
                    overlay.setData(indoorRouteResult.getRouteLines().get(0));
                    // 添加新的overlay
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
        // 设置listener
        mSearch.setOnGetRoutePlanResultListener(listener);
    }
    private void startIndoorRoutePlan(LatLng destination,String floor){
        // 首先获取当前所在位置
        IndoorPlanNode startNode = new IndoorPlanNode(currentLocation,currentFloor);
        // 根据传入的位置设置endNode
        IndoorPlanNode endNode = new IndoorPlanNode(destination, floor);
        // 进行路径规划
        mSearch.walkingIndoorSearch(new IndoorRoutePlanOption()
                .from(startNode)
                .to(endNode));
    }

    private void initIndoorPoiSearch(){
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                PoiOverlay poiOverlay = new PoiOverlay(mBaiduMap);
                poiOverlay.setData(poiResult);
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }

            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
                if(poiIndoorResult != null){
                    myPoiIndoorResult = poiIndoorResult;
                    indoorInfoList = poiIndoorResult.getArrayPoiInfo();

                    MyIndoorPoiOverlay indoorPoiOverlay = new MyIndoorPoiOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(indoorPoiOverlay);
                    indoorPoiOverlay.setData(poiIndoorResult);
                    indoorPoiOverlay.addToMap();
                    indoorPoiOverlay.zoomToSpan();
                    Log.d("TAG", "onGetPoiIndoorResult: 调用成功");
                    if(indoorInfoList == null){
                        Toast.makeText(MainActivity.this, "indoorinfolist还是null", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Log.d("poi的listener","出了问题");
                }
            }
        });
    }
    public class MyIndoorPoiOverlay extends IndoorPoiOverlay{

        /**
         * 构造函数
         *
         * @param baiduMap 该 IndoorPoiOverlay 引用的 BaiduMap 对象
         */
        public MyIndoorPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int i) {
            if(myPoiIndoorResult != null && myPoiIndoorResult.getArrayPoiInfo() != null){
                PoiIndoorInfo info = myPoiIndoorResult.getArrayPoiInfo().get(i);
                destinationLocation = info.latLng;
                destinationFloor = info.floor;
//                Toast.makeText(MainActivity.this, "出错在MyIndoorOverlay", Toast.LENGTH_SHORT).show();
                // 导航
//                startIndoorRoutePlan(destinationLocation,destinationFloor);
                showNavigationDialog(info);
                return true;

            }
            else{
                return false;
            }
        }
    }

    private void showNavigationDialog(final PoiIndoorInfo info){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("商店信息");
        StringBuilder sb = new StringBuilder();
        sb.append("商店名称: ").append(info.name).append("\n");
        sb.append("商店联系电话: ").append(info.phone).append("\n");
        sb.append("商店地址: ").append(info.address).append("\n");
        sb.append("商店人均消费: ").append(info.price==-1.0?"暂无信息":info.price).append("\n");
        sb.append("商店折扣情况: ").append(info.discount==-1?"暂无信息":info.discount).append("\n");
        sb.append("商店楼层: ").append(info.floor).append("\n");

        dialog.setMessage(sb.toString());

        dialog.setPositiveButton("开始导航", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 清楚所有marker
                mBaiduMap.clear();
                // 把poi再画上去
                mPoiSearch.searchPoiIndoor(new PoiIndoorOption().poiIndoorWd(etKeyword.getText().toString()).poiIndoorBid(mBaiduMap.getFocusedBaseIndoorMapInfo().getID()));
                // 画出起点终点以及导航
//                PaintHelper.makeInfoWindow(MainActivity.this,mBaiduMap,info.latLng,info.floor);
                PaintHelper.makeTextMarker(mBaiduMap,info.latLng,info.floor,255,12,0);
                PaintHelper.makePointMarker(mBaiduMap,info.latLng,R.drawable.icon_en);
                // 这里先设置成创意城的某一点 114.363585,30.532735
                LatLng creativeCity = new LatLng(30.532735,114.363585);
//                PaintHelper.makeInfoWindow(MainActivity.this,mBaiduMap,creativeCity,"F1");
                PaintHelper.makePointMarker(mBaiduMap,creativeCity,R.drawable.icon_st);
                // 实际上应该这样
//                PaintHelper.makePointMarker(mBaiduMap,currentLocation,R.drawable.icon_st);
                // 然后画出一条直线
                List<LatLng> line = new ArrayList<LatLng>();
                line.add(creativeCity);
                line.add(info.latLng);
                PaintHelper.drawMyRoute(mBaiduMap,line);
            }
        });

        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this,"你取消了导航", Toast.LENGTH_LONG);
            }
        });

        dialog.show();
    }
    private void startIndoorPoiSearch()
    {
        // 这是获得想去的poi的室内信息
        MapBaseIndoorMapInfo indoorInfo = mBaiduMap.getFocusedBaseIndoorMapInfo();
        if (indoorInfo != null){
            // 清除所有marker
            mBaiduMap.clear();
            PoiIndoorOption option = new PoiIndoorOption().poiIndoorWd(etKeyword.getText().toString()).poiIndoorBid(indoorInfo.getID());
            boolean ok = mPoiSearch.searchPoiIndoor(option);
            if(ok){
                Log.d("搜索室内poi","成功");
                StringBuilder sb = new StringBuilder();
                if(indoorInfoList != null){
                    sb.append(indoorInfoList.get(0).address);
                    sb.append(indoorInfoList.get(1).address);
                    sb.append(indoorInfoList.get(2).address);
                    Log.d("室内poi的哈哈哈",sb.toString());
                }

            }else{
                Log.d("搜索室内poi","失败");
            }
            Log.d("startindoorpoi","执行到这里了");
            Log.d("室内id",indoorInfo.getID());
        }else{
            Log.d("关键词:",etKeyword.getText().toString());
        }
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

