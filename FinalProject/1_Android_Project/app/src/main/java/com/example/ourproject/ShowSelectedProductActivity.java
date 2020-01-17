package com.example.ourproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ShowSelectedProductActivity extends FragmentActivity implements OnMapReadyCallback, Runnable {
    public final static int DIALOG_ENTER_ERROR = 1;
    public final static int DIALOG_END_ERROR = 2;

    private int productID;
    private TextView title; // 공구 제목
    private TextView time; // 남은 시간
    private TextView people; // 참여인원
    private TextView originWeight; // 원래 무게
    private TextView groupWeight; // 공구 무게
    private TextView originPrice; // 원래 가격
    private TextView groupPrice; // 공구 가격
    private TextView explane; // 공구 설명
    private TextView date; // 공구 분배 날짜
    private TextView place; // 공구 장소
    private ImageView imageView; // 공구 사진

    private Button join_button;

    private static String IP_ADDRESS = "175.115.155.72";
    private String mJsonString;

    private String id;
    private boolean isLoad = true;
    private boolean isEnd = false;
    private boolean isInit = false;

    static  public Geocoder geocoder;
    private GoogleMap mMap;
    static public String origin_adress;

    static public List<Address> addressList;
    static public double latitude;
    static public double longitude;
    static  public String adress;

    Bitmap bitmap;
    String imagePath;
    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            imageView.setImageBitmap(bitmap);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_product_info);

        try {
            FileInputStream fis = openFileInput("session.txt");
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            id = new String(buffer);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        productID = intent.getIntExtra("productID", -1);

        if (productID == -1) {
            Toast.makeText(ShowSelectedProductActivity.this, "상품을 불러오지 못했습니다.", Toast.LENGTH_LONG).show();
            finish();
        }

        // 초기 부여함수
        title = findViewById(R.id.title_info);
        time = findViewById(R.id.restTime_info);
        people = findViewById(R.id.nomGroup_info);
        originWeight = findViewById(R.id.OriginKg_info);
        groupWeight = findViewById(R.id.GroupKg_info);
        originPrice = findViewById(R.id.OriginPrice_info);
        groupPrice = findViewById(R.id.GroupPrice_info);
        explane = findViewById(R.id.Content_info);
        date = findViewById(R.id.DistributionContent_date);
        place = findViewById(R.id.DistributionContent_place);
        imageView = findViewById(R.id.Image_info);
        join_button = findViewById(R.id.JoinButton);

        join_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetDataInShow join = new GetDataInShow();
                join.execute("http://" + IP_ADDRESS + "/validateEnter.php", "ENTER", Integer.toString(productID), id);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        isLoad = true;
        isInit = false;
        GetDataInShow task = new GetDataInShow();
        task.execute("http://" + IP_ADDRESS + "/select.php", "LOAD");
        GetDataInShow Init = new GetDataInShow();
        Init.execute("http://" + IP_ADDRESS + "/validateEnter.php", "ENTER", Integer.toString(productID), id);
    }

    protected void OnCreateDialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(text)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                builder.show();
    }

    private class GetDataInShow extends AsyncTask<String, Void, String> {

        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (isLoad) {
                if (result == null) {

                } else {
                    mJsonString = result;
                    setProductInfo();
                    isLoad = false;
                }
            }
            else if(!isInit)
            {
                if (result.equals("false") && !isEnd) {
                    join_button.setText("취소하기");
                } else if(isEnd) {
                    join_button.setText("공구종료");
                } else {
                    join_button.setText("참여하기");
                }

                isInit = true;
            }
            else {
                //참여하기 누른 경우
                if (result.equals("true") && !isEnd) {
                    Intent intentCall = new Intent(ShowSelectedProductActivity.this, PayActivity.class);
                    intentCall.putExtra("productID", productID);
                    startActivity(intentCall);
                } else if(result.equals("truemax")){
                    //참여인원 다 찼을 때
                    OnCreateDialog("이미 인원이 꽉 찼습니다.");
                } else if (result.equals("false") && !isEnd) {
                    GetDataInShow task = new GetDataInShow();
                    task.execute("http://" + IP_ADDRESS + "/cancel.php", "CANCEL", Integer.toString(productID), id);
                    OnCreateDialog("공구를 취소하였습니다.");
                    finish();
                } else if(isEnd) {
                    OnCreateDialog("이미 종료된 공구입니다.");
                }
            }
        }

        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters;
            if(params[1].equals("ENTER")){
                postParameters = "num=" + params[2] + "&id=" + params[3];
            }
            else if(params[1].equals("CANCEL")) {
                postParameters = "num=" + params[2] + "&id=" + params[3];
            }
            else if(params[1].equals("LOAD"))
            {
                postParameters = "";
            }
            else
                return null;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                bufferedReader.close();
                Log.d("AAAA",sb.toString());
                return sb.toString().trim();
            } catch (Exception e) {
                errorString = e.toString();
                return null;
            }

        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        geocoder = new Geocoder(this);

        origin_adress = adress;
        //origin_adress = "Koreatech";
        List<Address> addressList = null;

        System.out.println("@@@@@@@@@@@@@@@@@@@@@adress = " + adress);

        try {
            if (geocoder.isPresent()) {
                // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
                addressList = geocoder.getFromLocationName(
                        origin_adress, // 주소
                        1); // 최대 검색 결과 개수
                latitude = addressList.get(0).getLatitude();
                longitude = addressList.get(0).getLongitude();

                System.out.println("******위도 = " + latitude + ", 경도 = " + longitude + "********");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!주소 변환 실패..");
        }


        //LatLng point = new LatLng(36.764064, 127.281102);
        LatLng point = new LatLng(latitude, longitude);
        // 마커 생성
        MarkerOptions mOptions2 = new MarkerOptions();
        mOptions2.title("분배 장소");
        mOptions2.position(point);
        // 마커 추가
        mMap.addMarker(mOptions2);
        // 해당 좌표로 화면 줌
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 16));
    }

    //타이머 만들기 1초에 한번씩 움직이는 거..
    public void startTimerTask(String d) {
        Timer mTimer = new Timer();

        final String ddate = d;

        TimerTask m1000msCountTimerTask = new TimerTask() {
            Handler handler = new Handler();

            public void init() throws ParseException {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                Date todaCal = new Date(System.currentTimeMillis()); //오늘날자 가져오기
                Date ddayCal = simpleDateFormat.parse(ddate); //오늘날자를 가져와 변경시킴

                long calculate = ddayCal.getTime() - todaCal.getTime(); // 오늘 날짜에서 dday 날짜를 빼주게 됩니다.

                long day = (calculate / (60 * 60 * 1000));
                long hour = ((calculate / (60 * 1000)) % 60);
                long sec = ((calculate / 1000) % 60);

                if(day <= 0 && hour <= 0 && sec <= 0) {
                    time.setText("남은 시간 : 종료");
                    join_button.setText("공구종료");
                    isEnd = true;
                }
                else
                    time.setText("남은 시간 " + day + ":" + hour + ":" + sec);
            }

            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            init();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        };
        mTimer.schedule(m1000msCountTimerTask, 0, 1000);
    }

    // 파일에서 내용을 읽어와 공구 제목 등을 설정함
    private void setProductInfo() {
        String TAG_JSON = "webnautes";
        String TAG_NUM = "num";
        String TAG_TITLE = "title";
        String TAG_DDATE = "ddate";
        String TAG_EGROUP = "egroup";
        String TAG_MGROUP = "mgroup";
        String TAG_ORGPRICE = "orgPrice";
        String TAG_GBPRICE = "gbPrice";
        String TAG_INFO = "description";
        String TAG_ADDRESS = "address";
        String TAG_MADDRESS = "maddress";
        String TAG_IMAGE = "image";
        String TAG_ORGWEIGHT = "orgWeight";
        String TAG_GBWEIGHT = "gbWeight";
        String TAG_UNIT = "danwi";
        String TAG_WAY = "way";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                String num = item.getString(TAG_NUM);

                if (Integer.parseInt(num) == productID) {
                    title.setText(item.getString(TAG_TITLE));
                    people.setText("참여인원" + item.getString(TAG_EGROUP) + "/" + item.getString(TAG_MGROUP));


                    originWeight.setText(item.getString(TAG_ORGWEIGHT) + item.getString(TAG_UNIT));
                    groupWeight.setText(item.getString(TAG_GBWEIGHT) + item.getString(TAG_UNIT));

                    place.setText(item.getString(TAG_ADDRESS)+ " "+item.getString(TAG_MADDRESS));

                    originPrice.setText(item.getString(TAG_ORGPRICE) + "원");
                    groupPrice.setText(item.getString(TAG_GBPRICE) + "원");
                    explane.setText(item.getString(TAG_INFO)+"\n");

                    adress = item.getString(TAG_ADDRESS)+ " "+item.getString(TAG_MADDRESS);
                    String ddate = item.getString(TAG_DDATE);
                    imagePath = item.getString(TAG_IMAGE).substring(1);
                    String after_parsing = ddate.split(" ")[0];
                    String[] ymd = after_parsing.split("-");
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                    long now = System.currentTimeMillis();
                    Date todaCal = new Date(now); //오늘날자 가져오기
                    Date ddayCal = simpleDateFormat.parse(ddate); //오늘날자를 가져와 변경시킴

                    long calculate = ddayCal.getTime() - todaCal.getTime(); // 오늘 날짜에서 dday 날짜를 빼주게 됩니다.

                    long day = (calculate / (60 * 60 * 1000));
                    long hour = ((calculate / (60 * 1000)) % 60);
                    long sec = ((calculate / 1000) % 60);

                    if(day <= 0 && hour <= 0 && sec <= 0) {
                        time.setText("남은 시간 : 종료");
                        join_button.setText("공구종료");
                        isEnd = true;
                    }
                    else
                        time.setText("남은 시간 : " + day + ":" + hour + ":" + sec);

                    startTimerTask(ddate);

                    date.setText(item.getString(TAG_WAY)+"\n"+ymd[0] + "년 " + ymd[1] + "월 " + ymd[2] + "일");

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);

                    Thread th = new Thread(ShowSelectedProductActivity.this);
                    th.start();
                }
            }


        } catch (JSONException e) {

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        URL url =null;
        try{
            // 스트링 주소를 url 형식으로 변환
            url =new URL("http://" + IP_ADDRESS + imagePath);
            // url에 접속 시도
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.connect();
            // 스트림 생성
            InputStream is = conn.getInputStream();
            // 스트림에서 받은 데이터를 비트맵 변환
            // 인터넷에서 이미지 가져올 때는 Bitmap을 사용해야함
            bitmap = BitmapFactory.decodeStream(is);

            // 핸들러에게 화면 갱신을 요청한다.
            handler.sendEmptyMessage(0);
            // 연결 종료
            is.close();
            conn.disconnect();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
