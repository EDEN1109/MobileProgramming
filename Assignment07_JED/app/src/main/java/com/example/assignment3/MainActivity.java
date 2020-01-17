package com.example.assignment3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1; // 권한 부여를 위한 정수
    private MusicService mService; // 서비스 받기 위한 MusicService형 변수
    boolean mBound = false; // 바운드 된 상태인지 확인하기 위한 변수

    private boolean isPause; // 일시정지 상태인지 아닌지 알기 위함
    private TextView musicName; // 음악의 이름을 출력하기 위함
    private ImageButton controller; // 일시정지 혹은 재생시 아이콘을 바꾸기 위함
    private ListView list; // 음악 리스트
    private ArrayAdapter<String> adapter; // 음악 리스트의 어댑터
    private ArrayList<String> music = new ArrayList<String>(); // 음악의 이름을 저장하는 리스트
    private long backKeyPressedTime = 0; // 뒤로 가기 버튼을 누른 시간을 측정하기 위한 변수

    // ServiceConnection 인터페이스를 구현한 ServiceConnection 객체 생성
    // onServiceConnected() 콜백 메소드와 onServiceDisconnected() 콜백 메소드를 구현
    private ServiceConnection mConnection = new ServiceConnection() {

        // Service에 연결(bound)되었을 때 호출되는 callback 메소드
        // Service의 onBind() 메소드에서 반환한 IBinder 객체로 service를 받음
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 두번째 인자로 넘어온 IBinder 객체를 MusicService 클래스에 정의된 MusicBinder 클래스 객체로 캐스팅
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;

            // MusicService 객체를 참조하기 위해 MusicBinder 객체의 getService() 메소드 호출
            mService = binder.getService();
            mBound = true; // 바운드 되었으므로 true로 변경

            mService.setTextView(musicName); // 음악 명 변경을 위해 변수를 넘겨줌

            // 서비스에서 음악 리스트의 사이즈를 받아와 그만큼 반복하며 listView의 음악명들을 넣어줌
            for (int i = 0; i < mService.getListSize(); i++) {
                music.add(mService.getMusicName(i)); // 노래제목에서 .mp3를 삭제함
            }
            adapter.notifyDataSetChanged(); // 음악명을 넣고 어댑터를 notify하여 반영해줌

            // 만일 현재 재생중인 음악의 이름이 빈칸이 아니라면, 즉 액티비티를 종료했다가 켰다면
            if(mService.getMusicName() != "") {
                musicName.setText(mService.getMusicName()); // 현재 재생중인 음악의 이름을 변경해준다.
                isPause = false; // 재생 상태로 변경
                controller.setImageResource(R.drawable.ic_pause_black_24dp); // 버튼의 이미지를 바꿈
            }
            mService.setNoti(getApplicationContext()); // 노티피케이션을 설정함
        }

        // Service 연결 해제되었을 때 호출되는 callback 메소드
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null; // 서비스에 null을 넣음
            mBound = false; // 바운드 해지된 상태이므로 false로 변경
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestRuntimePermission(); // 저장공간 읽기 권한을 얻기 위해 권한 요청 함수를 수행

        // 연결할 Service를 위한 Intent 객체 생성
        Intent intent = new Intent(this, MusicService.class);
        // startService 호출
        // 실행할 Service 클래스에 대한 Intent 객체를 생성하여 startService 호출 시 넘겨줌
        startService(intent);
        // Service에 연결하기 위해 bindService 호출, 생성한 intent 객체와 구현한 ServiceConnection의 객체를 전달
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        // 초기 선언, 어댑터를 설정해준다.
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, music);

        // 처음 음악을 선택하지 않은 화면에서는 정지 상태이므로 true로 설정
        isPause = true;

        // 각 변수에 알맞는 View를 할당
        controller = findViewById(R.id.controllBtn);
        musicName = findViewById(R.id.musicName);
        list = findViewById(R.id.listview);

        // listView의 Adapter와 ClickListener를 설정
        list.setAdapter(adapter);
        list.setOnItemClickListener(onClickListItem);
        // 음악의 이름이 선택되지 않아도 이름이 흐르며 보이게 함
        musicName.setSelected(true);
    }

    @Override
    public void onBackPressed() { // 뒤로 가기 버튼이 눌렸을 때 시행됨
        mService.setMusicName(musicName.getText().toString()); // 현재 재생중인 노래의 제목을 설정해 준다.
        super.onBackPressed();
    }

    // ImageButton을 눌렀을 때 onClick
    public void onClick(View view) {
        if(mBound) { // 바운드 상태일때 각 버튼들이 유효하게 동작함
            switch (view.getId()) { // 클릭한 버튼의 id를 가져옴
                case R.id.skipPrev: // 이전곡 듣기를 눌렀을 때
                    mService.prevMusic(); // 서비스의 이전곡 재생 함수를 호출
                    isPause = false; // 일지정지 상태가 아니므로 false
                    controller.setImageResource(R.drawable.ic_pause_black_24dp); // 버튼의 이미지를 바꿈
                    Toast.makeText(getApplicationContext(), "Skip to Prev", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.controllBtn: // 일시정지 혹은 재생을 눌렀을 때
                    mService.controllMusic(isPause);
                    if (isPause) { // 일시정지 상태였다면
                        isPause = false; // 재생 상태로 변경
                        controller.setImageResource(R.drawable.ic_pause_black_24dp); // 버튼의 이미지를 바꿈
                        Toast.makeText(getApplicationContext(), "Play", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        isPause = true; // 일시정지 상태로 변경
                        controller.setImageResource(R.drawable.ic_play_arrow_black_24dp); // 버튼의 이미지를 바꿈
                        Toast.makeText(getApplicationContext(), "Pause", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.skipNext: // 다음곡 듣기를 눌렀을 때
                    mService.nextMusic(); // 서비스의 다음곡 재생 함수를 호출
                    isPause = false; // 일지정지 상태가 아니므로 false
                    controller.setImageResource(R.drawable.ic_pause_black_24dp); // 버튼의 이미지를 바꿈
                    Toast.makeText(getApplicationContext(), "Skip to Next", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    // 리스트의 음악을 클릭했을 때 사용되는 리스너
    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // 이벤트 발생 시 해당 아이템 위치를 텍스트로 출력
            Toast.makeText(getApplicationContext(), "Start Music", Toast.LENGTH_SHORT).show();
            mService.selectMusic(position); // 선택한 음악의 position을 매개변수로 하여 서비스에서 함수를 호출
            isPause = false; // 일지정지 상태가 아니므로 false
            controller.setImageResource(R.drawable.ic_pause_black_24dp); // 버튼의 이미지를 바꿈
        }
    };

    // 액션바의 메뉴를 할당
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.exit_menu, menu); // exit_menu를 액션바의 메뉴로 설정
        return super.onCreateOptionsMenu(menu);
    }

    // 액션바의 메뉴를 눌렀을 때
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.exit: // 종료 버튼이라면
                Toast.makeText(getApplicationContext(), "Exit", Toast.LENGTH_SHORT).show();
                // 바운드를 끊기 위함
                if (mBound) { // 바운드 상태라면
                    unbindService(mConnection); // 바운드를 끊어줌
                    mBound = false; // 바운드 해지되었으므로 false를 넣어줌
                }
                // stopService 호출
                // 중지할 Service 클래스에 대한 Intent 객체를 생성하여 stopService 호출 시 넘겨준다
                stopService(new Intent(this, MusicService.class));
                finish(); // 애플리케이션 종료
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void requestRuntimePermission() {
        // 권한이 있는지 스스로 확인함
        // 권한이 없다면 실행
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                // 권한을 요청
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        } else {
            // READ_EXTERNAL_STORAGE 권한이 있는 것
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // READ_EXTERNAL_STORAGE 권한을 얻음
                } else {
                    // 권한을 얻지 못 하였으므로 location 요청 작업을 수행할 수 없다
                    // 적절히 대처한다
                }
                return;
            }
        }
    }

}
