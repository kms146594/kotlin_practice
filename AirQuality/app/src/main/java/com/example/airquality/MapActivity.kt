package com.example.airquality

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.airquality.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// OnMapReadyCallback 인터페이스를 구현해 사용해야 함
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    val binding by lazy { ActivityMapBinding.inflate(layoutInflater) }

    private var mMap: GoogleMap? = null
    // main에서 전달된 위도와 경도
    var currentLat: Double = 0.0
    var currentLong: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // MainAct에서 intent로 전달한 값을 가져옴
        currentLat = intent.getDoubleExtra("currentLat", 0.0)
        currentLong = intent.getDoubleExtra("currentLong", 0.0)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as
                SupportMapFragment?
        // 객체의 생명주기를 관리하는 supportMapFragment 객체를 mapFragment에 저장
        // getMapAsync()는 mapFragment에 OnMapReadyCallback 인터페이스를 등록
        // 지도가 준비되면 onMapReady가 자동 실행
        mapFragment?.getMapAsync(this)

        binding.btnCheckHere.setOnClickListener{
            mMap?.let {
                val intent = Intent()
                // 버튼이 눌린 시점의 카메라 포지션, 지도 중앙의 좌표값
                intent.putExtra("latitude", it.cameraPosition.target.latitude)
                intent.putExtra("longitude", it.cameraPosition.target.longitude)
                // main에서 정의한 onActivityResult()    함수 실행
                setResult(Activity.RESULT_OK, intent)
                // 지도 액티비티 종료
                finish()
            }
        }
    }
    // 지도가 준비되었을 때 실행되는 콜백
    // 메인에서 받은 위도,경도를 지도로 옮기고 마커를 세팅
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap?.let {
            val currentLocation = LatLng(currentLat, currentLong)
            it.setMaxZoomPreference(20.0f)  // 줌 최댓값 설정
            it.setMinZoomPreference(12.0f)  // 줌 최솟값 설정
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
        }
        setMarker()
        // 플로팅 액션 버튼이 눌리면 현재 위도,경도 정보를 가져와서 지도의 위치를 움직임
        binding.fabCurrentLocation.setOnClickListener {
            val locationProvider = LocationProvider(this@MapActivity)
            // 위도와 경도 정보를 가져옵니다.
            val latitude = locationProvider.getLocationLatitude()
            val longitude = locationProvider.getLocationLongitude()
            mMap?.moveCamera(CameraUpdateFactory
                .newLatLngZoom(LatLng(latitude, longitude), 16f))
            setMarker()
        }
    }
    // 마커 설정하는 함수
    private fun setMarker() {
        mMap?.let {
            it.clear()  // 지도에 있는 마커 삭제
            val markerOptions = MarkerOptions()
            markerOptions.position(it.cameraPosition.target)    // 마커 위치 설정
            markerOptions.title("마커 위치")    // 마커 이름 설정
            val marker = it.addMarker(markerOptions)    // 지도에 마커를 추가하고, 마커 객체 반환
            // 마커를 지도에 추가, setOnCameraMoveListener()를 통해 지도와 함께 이동
            it.setOnCameraIdleListener {
                marker?.let { marker ->
                    marker.setPosition(it.cameraPosition.target)
                }
            }
        }
    }
}