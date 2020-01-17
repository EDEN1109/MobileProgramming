package com.example.assignment3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MusicService extends Service {
    private File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC); // MUSIC파일 경로를 가지고 있는 변수
    private File[] musicFile; // MUSIC파일에서 읽어온 파일을 담을 변수
    private boolean isPlayed = false; // 재생된 적 있는지 확인
    private MediaPlayer player = new MediaPlayer(); // 음악을 재생시키는 미디어 플레이어
    private int pos; // 현재 재생되고 있는 음악의 index
    private TextView musicName; // 재생되는 음악을 보여주는 TextView
    private int listSize = 0; // 음악 리스트의 크기
    private String nowMusicName = ""; // 메인액티비티가 종료될 경우, 재생중이던 음악의 이름을 받는 변수

    // notification 관련 클래스
    private NotificationManager notificationManager;
    private NotificationChannel mChannel;
    private Notification noti;
    private Notification.Builder notiBuilder;

    // Binder 클래스를 상속 받는 클래스를 정의
    // getService() 메소드에서 현재 서비스 객체를 반환
    public class MusicBinder extends Binder {
        // 클라이언트가 호출할 수 있는 공개 메소드. 현재 Service 객체 반환
        MusicService getService() {
            return MusicService.this;
        }
    }

    // 위에서 정의한 Binder 클래스의 객체 생성
    // Binder 클래스는 Interface인 IBinder를 구현한 클래스
    private final IBinder mBinder = new MusicBinder();

    // Service 연결이 되었을 때 호출되는 메소드
    @Override
    public IBinder onBind(Intent intent) {
        // 위에서 생성한 MusicBinder 객체를 반환
        return mBinder;
    }

    // 바인드가 끊겼을 때 호출
    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        musicFile = folder.listFiles(); // MUSIC폴더의 모든 파일을 musicFile변수에 넣음
        // 음악의 이름을 읽어옴
        if(musicFile != null) { // 음악이 있다면
            listSize = musicFile.length; // 리스트 사이즈를 초기화
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    // 서비스가 종료될 때 호출
    @Override
    public void onDestroy() {
        if(isPlayed) { // 음악이 플레이 된 적 있다면
            // MediaPlayer 해지
            if (player != null) { // MediaPlayer가 설정된 적 있다면
                player.release(); // 메모리를 release해준다.
                player = null; // null을 넣어준다.
            }
        }
    }

    // 이전 음악을 재생하는 함수
    public void prevMusic() {
        if (--pos >= 0) { // 현재 음악의 index - 1이 0보다 크다면 이전곡을 재생함
            playSong(musicFile[pos].getName()); // 해당 음악을 재생
        } else { // 그렇지 않다면 처음 곡에서 이전곡 듣기를 시도한 것임
            pos = listSize - 1; // 현재 음악의 index를 음악 리스트 맨 끝의 음악으로 변경
            playSong(musicFile[pos].getName()); // 해당 음악을 재생
        }
    }

    // 다음 음악을 재생하는 함수
    public void nextMusic() {
        if (++pos < listSize) { // 현재 음악의 index + 1이 음악 리스트의 사이즈보다 작다면 다음곡을 재생함
            playSong(musicFile[pos].getName()); // 해당 음악을 재생
        } else { // 그렇지 않다면 마지막 곡에서 다음곡 듣기를 시도한 것임
            pos = 0; // 현재 음악의 index를 음악 리스트의 맨 처음 음악으로 변경
            playSong(musicFile[pos].getName()); // 해당 음악을 재생
        }
    }

    // 선택한 음악을 재생
    public void selectMusic(int i) {
        pos = i; // 현재 재생되는 음악의 index를 매개변수로 받은 i로 변경
        playSong(musicFile[i].getName()); // 해당 음악을 재생
    }

    // 음악의 재생과 일시정지를 관리
    public void controllMusic(boolean isPause) {
        if (isPause) // 일시정지 상태였다면
        {
            player.start(); // 음악을 시작
        }
        else { // 일시정지 상태가 아니었다면
            player.pause(); // 음악을 정지
        }
        updateNoti(); // 노티피케이션의 문구를 업데이트 해줌
    }

    // 매개변수 i번째 음악의 이름을 가져오는 함수
    public String getMusicName(int i) {
        return musicFile[i].getName().replace(".mp3","");
    }

    // 음악 리스트 사이즈를 반환하는 함수
    public int getListSize() {
        return listSize;
    }

    // 현재 재생중인 음악의 이름을 설정한다.
    public void setMusicName(String name) {
        nowMusicName = name;
    }

    // 현재 재생중인 음악의 이름을 반환한다.
    public String getMusicName() { return nowMusicName; }

    // TextView를 지정하기 위한 함수
    public void setTextView(TextView textView) {
        musicName = textView;
    }

    // 노티피케이션을 초기 설정 하는 함수
    public void setNoti(Context context) { // Context를 변수로 받는다
        // Android version이 8.0 Oreo 이상이면
        if(Build.VERSION.SDK_INT >= 26){
            // 노티피케이션 채널을 설정
            mChannel = new NotificationChannel("music_service_channel_id",
                    "music_service_channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            // 노티피케이션 매니저 초기화
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            // 노티피케이션 채널 생성
            notificationManager.createNotificationChannel(mChannel);
            // 노티피케이션 빌더 객체 생성
            notiBuilder = new Notification.Builder(context, mChannel.getId());

        }
        else{
            // 노티피케이션 빌더 객체 생성
            notiBuilder = new Notification.Builder(context);
        }

        // 매개변수로 받은 context로 intent를 선언
        Intent intent = new Intent(context, MainActivity.class);
        // Intent 객체를 이용하여 PendingIntent 객체를 생성 - Activity를 실행하기 위한 PendingIntent
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification.Builder 객체를 이용하여 Notification 객체 생성
        // 초기 설정 당시에는 재생되는 음악이 없으므로 ContentText를 "Non Playing Music"으로 설정
        noti = notiBuilder.setContentTitle("Music App")
                .setContentText("Non Playing Music")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .build();

        // foregound service 설정 - startForeground() 메소드 호출, 위에서 생성한 nofication 객체 넘겨줌
        startForeground(123, noti);


        // 만일 현재 음악 이름이 빈칸이 아니라면, 즉 액티비티를 완전히 종료했다가 다시 실행했었다면
        if(nowMusicName != "")
            updateNoti(); // 노티피케이션의 내용을 업데이트한다.
    }

    // 노티피케이션의 내용을 업데이트하는 함수
    private void updateNoti() {
        String content;
        // 변경할 ContentText를 설정함
        if(player.isPlaying()) { // 음악이 재생 중이라면
            // 재생중인 음악을 content에 넣어줌
            content = "Playing Music : " + musicFile[pos].getName().replace(".mp3", "");
        }
        else // 재생중이지 않다면
        {
            content = "Non Playing Music"; // 재생중이지 않다고 표시
        }

        noti = notiBuilder.setContentText(content).build(); // 변경한 ContentView내용을 노티피케이션 빌더에 적용
        notificationManager.notify(123, noti); // 변경 내용을 반영
    }

    // 매개변수로 받은 음악명과 같은 음악을 재생하는 함수
    private void playSong(String songName) {
        player.reset(); // 음악을 변경할 수 있도록 reset해준다.
        try {
            isPlayed = true; // 음악이 재생된 적 있으므로 isPlayed를 true로 해준다.
            player.setDataSource(folder.getPath()+"/"+songName); // 재생할 음악을 경로를 통해 가져온다.
            player.prepare(); // 음악을 준비한다.
            player.start(); // 재생한다.

            // 재생중인 음악을 나타내는 TextView인 musicName의 내용을 변경한다.
            musicName.setText(musicFile[pos].getName().replace(".mp3", "")); // 파일확장자명인 .mp3를 제거한다.
            updateNoti(); // 노티피케이션을 업데이트하여 재생중인 음악이 정상적으로 나오게 한다.
            // 한 곡의 재생이 끝나면 다음 곡을 재생하도록 설정한다.
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer arg0) {
                    nextSong();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 한 곡의 재생이 끝나면 다음곡 재생을 위한 함수
    private void nextSong() {
        if (++pos < listSize) { // 현재 음악의 index + 1이 음악 리스트의 사이즈보다 작다면 다음곡을 재생함
            // 다음 곡을 재생합니다.
            Toast.makeText(this, "Next Music", Toast.LENGTH_SHORT).show();
            playSong(musicFile[pos].getName()); // 해당 음악을 재생
        } else { // 그렇지 않다면 음악 리스트의 마지막 곡이 끝난 것이므로, 재생할 곡을 초기화
            pos = 0; // 현재 음악의 index를 음악 리스트의 맨 처음 음악으로 변경
            Toast.makeText(this, "First of List", Toast.LENGTH_SHORT).show();
            playSong(musicFile[pos].getName()); // 해당 음악을 재생
        }
    }
}
