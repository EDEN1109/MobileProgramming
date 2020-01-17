package com.example.ourproject.ui.mypage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.example.ourproject.R;
import com.example.ourproject.ShowSelectedProductActivity;
import com.example.ourproject.ui.ListViewAdapter;
import com.example.ourproject.ui.ListViewItem;
import com.example.ourproject.ui.home.HomeFragment;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MyPageFragment extends Fragment implements Runnable{

    private ListView listView;
    private ListViewAdapter m_Adapter;

    private static String IP_ADDRESS = "175.115.155.72";
    private String mJsonString;

    private boolean lastItemVisibleFlag = false;    // 리스트 스크롤이 마지막 셀(맨 바닥)로 이동했는지 체크할 변수
    private int page = 0;                           // 페이징변수. 초기 값은 0 이다.
    private ProgressBar progressBar;                // 데이터 로딩중을 표시할 프로그레스바
    private boolean mLockListView = false;          // 데이터 불러올때 중복안되게 하기위한 변수
    private final int PAGING_NUM = 5;

    private String id;
    private final String FILENAME = "session.txt";
    Bitmap bitmap;
    String imagePath;
    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    //타이머 만들기 1초에 한번씩 움직이는 거..
    public void startTimerTask() {
        Timer mTimer = new Timer();

        TimerTask m1000msCountTimerTask = new TimerTask() {
            Handler handler = new Handler();

            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        m_Adapter.notifyDataSetChanged();
                    }
                });
            }

        };

        mTimer.schedule(m1000msCountTimerTask, 0, 1000);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        try {
            FileInputStream fis = getActivity().openFileInput(FILENAME);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            id = new String(buffer);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        m_Adapter = new ListViewAdapter();
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        listView = root.findViewById(R.id.listview_home);
        listView.setAdapter(m_Adapter);
        listView.setOnItemClickListener(onClickListItem);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 1. OnScrollListener.SCROLL_STATE_IDLE : 스크롤이 이동하지 않을때의 이벤트(즉 스크롤이 멈추었을때).
                // 2. lastItemVisibleFlag : 리스트뷰의 마지막 셀의 끝에 스크롤이 이동했을때.
                // 3. mLockListView == false : 데이터 리스트에 다음 데이터를 불러오는 작업이 끝났을때.
                // 1, 2, 3 모두가 true일때 다음 데이터를 불러온다.
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag && mLockListView == false) {
                    // 화면이 바닦에 닿을때 처리
                    // 로딩중을 알리는 프로그레스바를 보인다.
                    progressBar.setVisibility(View.VISIBLE);

                    // 다음 데이터를 불러온다.
                    getItem();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // firstVisibleItem : 화면에 보이는 첫번째 리스트의 아이템 번호.
                // visibleItemCount : 화면에 보이는 리스트 아이템의 갯수
                // totalItemCount : 리스트 전체의 총 갯수
                // 리스트의 갯수가 0개 이상이고, 화면에 보이는 맨 하단까지의 아이템 갯수가 총 갯수보다 크거나 같을때.. 즉 리스트의 끝일때. true
                lastItemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
            }
        });
        startTimerTask();

        progressBar = (ProgressBar) root.findViewById(R.id.homeProgressbar);
        progressBar.setVisibility(View.GONE);

        // 예시
//        m_Adapter.addItem(1, getResources().getDrawable(R.drawable.onion),
//                "양파 공구합니다", 5, 0.3, 1303, "00:23:34", false);
        GetData task = new GetData();
        task.execute( "http://" + IP_ADDRESS + "/mypage.php", id);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        m_Adapter = new ListViewAdapter();
        listView.setAdapter(m_Adapter);
        GetData task = new GetData();
        task.execute( "http://" + IP_ADDRESS + "/mypage.php", id);
        page = 0;
    }

    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = null; // 초기 인텐트는 null로 설정한다.
            intent = new Intent( getContext() , ShowSelectedProductActivity.class );
            intent.putExtra("productID", (int)m_Adapter.getItemId(position));
            startActivity(intent);
        }
    };

    private void getItem(){

        // 리스트에 다음 데이터를 입력할 동안에 이 메소드가 또 호출되지 않도록 mLockListView 를 true로 설정한다.
        mLockListView = true;

        // 다음 20개의 데이터를 불러와서 리스트에 저장한다.
        showResult();


        // 1초 뒤 프로그레스바를 감추고 데이터를 갱신하고, 중복 로딩 체크하는 Lock을 했던 mLockListView변수를 풀어준다.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                m_Adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                mLockListView = false;
            }
        },1000);
    }
    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(getContext(),
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if (result == null){

            }
            else {
                //Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();

                mJsonString = result;
                showResult();
                m_Adapter.notifyDataSetChanged();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = "id=" + params[1];

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

                Log.d("AAAAA", sb.toString().trim());
                return sb.toString().trim();


            } catch (Exception e) {
                errorString = e.toString();
                return null;
            }

        }
    }


    private void showResult(){
        String TAG_JSON="webnautes";
        String TAG_NUM = "num";
        String TAG_TITLE = "title";
        String TAG_DDATE = "ddate";
        String TAG_EGROUP = "egroup";
        String TAG_MGROUP = "mgroup";
        String TAG_ORGPRICE = "orgPrice";
        String TAG_GBPRICE = "gbPrice";
        String TAG_IMAGE = "image";
        String TAG_UID = "uid";
        String TAG_GBWEIGHT = "gbWeight";
        String TAG_UNIT = "danwi";


        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
            FileInputStream fis = getActivity().openFileInput(FILENAME);
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            fis.close();
            String userID = new String(bytes);

            for(int i=PAGING_NUM*page;i<PAGING_NUM*(page+1);i++){
                if(m_Adapter.getCount() >= jsonArray.length())
                {
                    return ;
                }
                if(m_Adapter.getCount() - 1 < i) {
                    JSONObject item = jsonArray.getJSONObject(i);

                    String num = item.getString(TAG_NUM);
                    String title = item.getString(TAG_TITLE);
                    String ddate = item.getString(TAG_DDATE);
                    String egroup = item.getString(TAG_EGROUP);
                    String mgroup = item.getString(TAG_MGROUP);
                    String orgprice = item.getString(TAG_ORGPRICE);
                    String gbprice = item.getString(TAG_GBPRICE);
                    String image = item.getString(TAG_IMAGE);
                    String uid = item.getString(TAG_UID);
                    String weight = item.getString(TAG_GBWEIGHT);
                    String unit = item.getString(TAG_UNIT);

                    boolean isMine = false;

                    imagePath = item.getString(TAG_IMAGE).substring(1);
                    Log.d("AAAAA", imagePath);

                    Thread th =new Thread(MyPageFragment.this);
                    // 동작 수행
                    th.start();
                    th.join();

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                    long now = System.currentTimeMillis();
                    Date todaCal = new Date(now); //오늘날자 가져오기
                    Date ddayCal = simpleDateFormat.parse(ddate); //오늘날자를 가져와 변경시킴

                    long calculate = ddayCal.getTime() - todaCal.getTime(); // 오늘 날짜에서 dday 날짜를 빼주게 됩니다.

                    long day = (calculate / (60 * 60 * 1000));
                    long hour = ((calculate / (60 * 1000)) % 60);
                    long sec = ((calculate / 1000) % 60);

                    String time = null;

                    if(day <= 0 && hour <= 0 && sec <= 0)
                        time = "종료";
                    else
                        time = day+":"+hour+":"+sec;
                    if(uid.equals(userID))
                        isMine = true;

                    m_Adapter.addItem(Integer.parseInt(num), bitmap, title, Integer.parseInt(mgroup), weight+unit,
                            Integer.parseInt(gbprice), time, isMine);
                }
            }
            page++;

        } catch (JSONException e) {

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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