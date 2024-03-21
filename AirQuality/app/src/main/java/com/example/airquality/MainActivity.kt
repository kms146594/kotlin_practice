package com.example.airquality

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import com.example.airquality.databinding.ActivityMainBinding
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    // 런타임 권한 요청 시 필요한 요청 코드
    private val PERMISSION_REQUEST_CODE = 100
    // 요청할 권한 목록
    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    // 위치 서비스 요청 시 필요한 런처
    lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>

    // 위도와 경도를 가져올 때 필요 locationProvider
    lateinit var locationProvider: LocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // 권한 확인
        checkAllPermission()
        updateUI()
    }
    // 권한 확인
    private fun checkAllPermission() {
        // 위치서비스(GPS)가 켜져있는지 확인
        if (!isLocationServicesAvailable()) {
            showDialogForLocationServiceSetting();
        // 런타임 앱 권한이 모두 허용되어 있는지 확인
        } else {
            isRunTimePermissionsGranted();
        }
    }
    // 위치 서비스가 켜져잇는지 확인 -> 둘 중 하나가 있다면 true
    fun isLocationServicesAvailable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }
    // 런타임 앱 권한 확인
    fun isRunTimePermissionsGranted() {
        // 위치 퍼미션을 가지고 있는지 체크
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
        // 권한이 하나라도 없다면 퍼미션 요청
        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
            hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity,
                REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
        }
    }
    // 모든 퍼미션이 허용되었는지 확인 -> 미허용 권한 존재 시 앱 종료
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // 요청 코드가 PERMISSION_CODE이고 요청한 퍼미션 개수 만큼 수신된 경우
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size)
        {
            var checkResult = true
            // 모든 퍼미션을 허용했는지 체크
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }
            // 위젯값을 가져올 수 있음
            if (checkResult) {

            } else {
                // 퍼미션 거부 시 앱 종료
                Toast.makeText(this@MainActivity,
                    "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.",
                    Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    // 위치 서비스가 꺼져 있다면 다이얼로그를 사용하여 위치 서비스를 설정하도록 하는 함수
    private fun showDialogForLocationServiceSetting() {
        // ActivityResultLauncher를 설정
        // 결과값을 반환해야 하는 인텐트를 실행할 수 있음
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            // 결과 값을 받았을 때 로직
            if (result.resultCode == Activity.RESULT_OK) {
                // GPS 활성화 여부 확인
                if (isLocationServicesAvailable()) {
                    isRunTimePermissionsGranted()   // 런타임 권한 확인
                } else {
                    // 위치 서비스가 허용되지 않았다면 앱 종료
                    Toast.makeText(this@MainActivity,
                        "위치 서비스를 사용할 수 없습니다.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
        // 사용자에게 의사를 물어보는 AlertDialog 생성
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("위치 서비스 활성화")
        builder.setMessage("위치 서비스가 꺼져있습니다. 설정해야 앱을 사용할 수 있습니다.")
        // 다이얼로그 창 바깥 터치 시 닫힘
        builder.setCancelable(true)
        builder.setPositiveButton("설정", DialogInterface.OnClickListener {
            dialog, id ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            getGPSPermissionLauncher.launch(callGPSSettingIntent)
        })
        builder.setNegativeButton("취소", DialogInterface.OnClickListener{
            dialog, id ->
            dialog.cancel()
            Toast.makeText(this@MainActivity, "기기에서 위치서비스(GPS) 설정 후 사용해주세요",
                Toast.LENGTH_SHORT).show()
            finish()
        })
        builder.create().show()
    }

    private fun updateUI() {
        locationProvider = LocationProvider(this@MainActivity)

        // 위도와 경도의 정보를 가져옵니다.
        val latitude: Double = locationProvider.getLocationLatitude()
        val longitude: Double = locationProvider.getLocationLongitude()

        if (latitude != 0.0 || longitude != 0.0) {

            // 1. 현재 위치를 가져오고 UI 업데이트
            // 현재 위치를 가져오기
            val address = getCurrentAddress(latitude, longitude)
            // 주소가 null이 아닐 경우 UI 업데이트
            address?.let {
                binding.tvLocationTitle.text = "${it.thoroughfare}" // 예시 : 역삼 1동
                binding.tvLocationSubtitle.text = "${it.countryName}" +
                        "${it.adminArea}" // 에 : 대한민국 서울특별시
            }
            // 2. 현재 미세먼지 농도 가져오고 UI 업데이트

        } else {
            Toast.makeText(this@MainActivity,
                "위도, 경도 정보를 가져올 수 없었습니다. 새로고침을 눌러주세요.",
                Toast.LENGTH_LONG).show()
        }
    }

    fun getCurrentAddress(latitude: Double, longitude: Double) : Address? {
        val geocoder = Geocoder(this, Locale.getDefault())
        // Address 객체는 주소와 관련된 여러 정보를 가지고 있습니다.
        // android.location.Address 패키지 참고
        val addresses: List<Address>?

        addresses = try {
            // Geocoder 객체를 이용하여 위도와 경도로부터 리스트를 가져옵니다.
            geocoder.getFromLocation(latitude, longitude, 7)
        } catch (ioException: IOException) {
            Toast.makeText(this, "지오코더 서비스 사용불가합니다.", Toast.LENGTH_LONG).show()
            return null
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(this, "잘못된 위도, 경도 입니다.", Toast.LENGTH_LONG).show()
            return null
        }

        // 에러는 아니지만 주소가 발견되지 않은 경우
        if (addresses == null || addresses.size == 0) {
            Toast.makeText(this, "주소가 발견되지 않았습니다.", Toast.LENGTH_LONG).show()
            return null
        }

        val address: Address = addresses[0]
        return address
    }
}