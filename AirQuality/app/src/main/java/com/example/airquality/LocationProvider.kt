package com.example.airquality

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.content.ContextCompat

// Location은 위도, 경도, 고도와 같이 위치에 관련된 정보를 가지고 있는 클래스
class LocationProvider(val context: Context) {
    private var location: Location? = null
    // 시스템 위치 서비스에 접근을 제공하는 클래스
    private var locationManager: LocationManager? = null

    init {
        // 초기화 시에 위치를 가져옴
        getLocation();
    }

    private fun getLocation(): Location? {
        try {
            // 위치 시스템 서비스 호출
            locationManager = context.getSystemService(
                Context.LOCATION_SERVICE) as LocationManager

            var gpsLocation: Location? = null
            var networkLocation: Location? = null

            // GPS Provider와 Network Provider 활성 여부 확인
            val isGPSEnabled: Boolean = locationManager!!.isProviderEnabled(
                LocationManager.GPS_PROVIDER)
            val isNetworkEnabled: Boolean = locationManager!!.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER)

            // GPS, Network Provider 둘 다 사용 불가능한 상황이면 null 반환
            if (!isGPSEnabled && !isNetworkEnabled) {
                return null
            } else {
                // ACCESS_COARSE_LOCATION보다 더 정밀한 위치 정보 얻기
                val hasFineLocationPermission =
                    ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                // 도시 block 단위의 정밀도와 위치 정보를 얻기
                val hasCoarseLocationPermission =
                    ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                // 위 2개의 권한이 없다면 null을 반환
                if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED)
                    return null
                // 네트워크를 통한 위치 파악이 가능한 경우에 위치를 가져옴
                if (isNetworkEnabled) {
                    networkLocation = locationManager?.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER)
                }
                // GPS를 통한 위치 파악이 가능한 경우 위치를 가져옴
                if (isGPSEnabled) {
                    gpsLocation = locationManager?.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER)
                }

                // 2개 위치가 있다면 정확도가 높은 것으로 선택
                if (gpsLocation != null && networkLocation != null) {
                    if(gpsLocation.accuracy > networkLocation.accuracy) {
                        location = gpsLocation
                        return gpsLocation
                    } else {
                        location = networkLocation
                        return  networkLocation
                    }
                } else {    // 가능한 위치 정보가 한개만 있는 경우
                    if (gpsLocation != null){
                        location = gpsLocation
                    }
                    if (networkLocation != null){
                        location = networkLocation
                    }
                }
            }
        } catch (e : Exception) {
            e.printStackTrace() // 에러 출력
        }
        return location
    }

    // 위도 정보를 가져오는 함수
    fun getLocationLatitude(): Double {
        return location?.latitude ?: 0.0    // null이면 0.0 반환
    }
    // 경도 정보를 가져오는 함수
    fun getLocationLongitude(): Double {
        return location?.longitude ?: 0.0
    }
}