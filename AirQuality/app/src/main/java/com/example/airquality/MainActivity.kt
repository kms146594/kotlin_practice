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
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.airquality.databinding.ActivityMainBinding
import com.example.airquality.retrofit.AirQualityResponse
import com.example.airquality.retrofit.AirQualityService
import com.example.airquality.retrofit.RetrofitConnection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.*


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

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // 권한 확인
        checkAllPermission()
        updateUI()
        setRefreshButton()

        setFAB()
    }
    // 업데이트 버튼을 통해 UI 업데이트
    private fun setRefreshButton() {
        binding.btnRefresh.setOnClickListener{
            updateUI()
        }
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
    /**
     *  @desc 런타임 권한을 요청하고 권한 요청에 따른 결과 반환
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // 요청 코드가 PERMISSION_CODE이고
        // 요청한 퍼미션 개수 만큼 수신된 경우
        if (requestCode == PERMISSION_REQUEST_CODE &&
            grantResults.size == REQUIRED_PERMISSIONS.size)
        {
            var checkResult = true
            // 모든 퍼미션을 허용했는지 체크
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }
            // 위칫값을 가져올 수 있음
            if (checkResult) {
                // 런타임 권한이 허용되었을 때도 updateUI 함수가 실행되어야 함
                updateUI()
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
    /**
     *  @desc LocationManager를 사용하기 위해서 권한 요청
     */
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

        // MapActivity로 위치를 보내고, 지정된 위도,경도를 받아와 함수를 실행해야 함.
        // 위도 경도를 클래스 객체 변수로 격상
        if (latitude != 0.0 || longitude != 0.0) {
            // 위도와 경도의 정보를 가져옵니다.
            latitude = locationProvider.getLocationLatitude()
            longitude = locationProvider.getLocationLongitude()

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
            getAirQualityData(latitude, longitude) //

        } else {
            Toast.makeText(this@MainActivity,
                "위도, 경도 정보를 가져올 수 없었습니다. 새로고침을 눌러주세요.",
                Toast.LENGTH_LONG).show()
        }
    }

    /**
     * @desc 위도와 경도를 기준으로 실제 주소 가져오기
     */
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

    /**
     * @desc 레트로핏 클래스를 이용하여 미세먼지 오염 정보를 가져옴
     */
    private fun getAirQualityData(latitude: Double, longitude: Double) {
        // 레트로핏 객체를 이용해 AirQualityService 인터페이스 구현체를 가져올 수 있음
        val retrofitAPI = RetrofitConnection.getInstance().create(
            AirQualityService::class.java)

        /** 구현된 AirQualityService 인터페이스 객체(retrofitAPI)를 이용하여 Call 객체를 만든 후
         *  enqueue() 함수를 실행하여 서버에 API 요청을 보냄
         *  retrofitAPI.getAirQualityData()는 유일한 함수 반환 값은 Call<AirQualityResponse>
         */
        // 레트로핏에서 요청을 처리하는 Call 객체는 HTTP 요청을 보내는 두가지 방식을 제공
        // execute(): 동기적으로 요청과 응답. 함수가 실행되는 스레드에서 실행 -> 메인 스레드에서
        // 함수 실행 시 응답 전까지 UI가 블로킹 됨 = 추천 X
        // enqueue(retrofit2): 비동기적 백그라운드 스레드에서 요청 / 응답 시 등록된 콜백 함수 실행
        // 메인스레드에서 실행해도 백그라운드 스레드에서 요청 처리되기 때문에 UI 블로킹 X
        retrofitAPI.getAirQualityData(
            latitude.toString(),
            longitude.toString(),
            "6a0f7de6-747a-4dc3-999a-536939020c63"
        ).enqueue(object : Callback<AirQualityResponse> {
            override fun onResponse(
                call: Call<AirQualityResponse>,
                response: Response<AirQualityResponse>
            ){
                // 정상적인 Response가 왔다면 UI 업데이트
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "최신 정보 업데이트 완료!",
                        Toast.LENGTH_SHORT).show()
                    // response.body()가 null이 아니면 updateAirUI()
                    response.body()?.let { updateAirUI(it) }
                } else {
                    Toast.makeText(this@MainActivity, "업데이트에 실패했습니다",
                        Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@MainActivity, "업데이트에 실패했습니다",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     *  @desc 가져온 데이터 정보를 바탕으로 화면 업데이트
     */
    private fun updateAirUI(airQualityData: AirQualityResponse) {
        val pollutionData = airQualityData.data.current.pollution

        // 수치 지정 (가운데 숫자)
        binding.tvCount.text = pollutionData.aqius.toString()

        // 측정된 날짜 지정
        // utc 시간대 -> 한국 시간보다 느림, ZonedDateTime 클래스를 이용하여 서울 시간대 적용
        // DateTimeFormatter.ofPattern 함수를 이용하여 형식 수정
        // "2021-09-04T14:00:00.000z" 형식을 "2021-09-04 23:00"로 수정
        val dateTime = ZonedDateTime.parse(pollutionData.ts).withZoneSameInstant(
            ZoneId.of("Asia/Seoul"))
            .toLocalDateTime()
        val dateFormatError: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm")
        binding.tvCheckTime.text = dateTime.format(dateFormatError).toString()

        // aqius 값은 미국 기준 AQI(대기지수) 의미, 위 기준으로 범위별 농도에 따른 텍스트와 배경 설정
        when (pollutionData.aqius) {
            in 0..50 -> {
                binding.tvTitle.text = "좋음"
                binding.imgBg.setImageResource(R.drawable.bg_good)
            }
            in 51..150 -> {
                binding.tvTitle.text = "보통"
                binding.imgBg.setImageResource(R.drawable.bg_soso)
            }
            in 151..200 -> {
                binding.tvTitle.text = "나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_bad)
            }
            else -> {
                binding.tvTitle.text = "매우 나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_worst)
            }
        }
    }

    /**
     *  액티비티와 액티비티 사이에서 데이터는 양방향으로 이동할 수 있다. -> registerForActivityResult
     */
    // 해당 변수의 타입은 ActivityResultLauncher이다 : 결과를 받아와야 하는 액티비티를 실행할 때 사용
    // registerForActivityResult() 함수에 두번째 인수로 콜백을 등록 -> 해당 액티비티가 결과를 반환시 실행
    var startActivityForResult = registerForActivityResult(ActivityResultContracts
        .StartActivityForResult(), object : ActivityResultCallback<ActivityResult> {
        override fun onActivityResult(result: ActivityResult) {
            if (result?.resultCode ?: 0 == Activity.RESULT_OK) {
                // 지도에서 위도와 경도를 반환, 객체 변수에 각각 저장
                latitude = result?.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                longitude = result?.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
                updateUI()
                // 해당 위/경도를 이용해 미세먼지 농도를 구함
            }
        }
    })
    // 현재 위도와 경도 정보를 담아 지도 페이지로 보냄
    // startMapActivityResult 변수를 launch하면 지도 페이지로 이동하고,
    // 등록해둔 onActivityResult 콜백에 보낸값이 전달됨
    private fun setFAB() {
        binding.fab.setOnClickListener{
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("currentLat", latitude)
            intent.putExtra("currentLong", longitude)
            startActivityForResult.launch(intent)
        }
    }
}