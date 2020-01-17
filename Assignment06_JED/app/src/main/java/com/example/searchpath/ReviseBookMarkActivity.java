package com.example.searchpath;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReviseBookMarkActivity extends AppCompatActivity {

    private String FILENAME = new MainActivity().getFILENAME(); // 파일이름을 모두 같게하기 위해 변수에서 받아온다.
    private String startPoint; // 변경하기 전의 출발지를 저장한다.
    private String endPoint; // 변경하기 전의 도착지를 저장한다.
    private EditText editStart; // 출발지를 수정하는 EditText
    private EditText editEnd; // 도착지를 수정하는 EditText

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revisebookmark);

        Intent intent = getIntent(); // putExtra를 받기위한 인텐트 변수
        editStart = (EditText) findViewById(R.id.editStart); // 아이디를 통해 해당하는 EditText를 찾아 할당한다.
        editEnd = (EditText) findViewById(R.id.editEnd); // 아이디를 통해 해당하는 EditText를 찾아 할당한다.
        // key를 start/end로 하여 전달받은 정보를 startPoint/endPoint에 저장한다.
        // 해당 키로 전달받은 내용이 없다면 !@!NULL!@!를 넣는다.
        startPoint = intent.getExtras().getString("start", "!@!NULL!@!");
        endPoint = intent.getExtras().getString("end", "!@!NULL!@!");

        if (startPoint == "!@!NULL!@!" || endPoint == "!@!NULL!@!") { // 하나의 정보라도 전달받지 못하면
            finish(); // 액티비티를 끝낸다.
        }

        // EditText의 초기의 문장을 각각 변경 전의 출발지와 도착지로 한다.
        editStart.setText(startPoint);
        editEnd.setText(endPoint);
    }

    public void onClick(View view) {
        try {
            if (R.id.editBtn == view.getId()) { // 수정 버튼을 눌렀다면
                // 출발지와 도착지의 EditText에 내용을 입력했다면 을 뜻하는 if문
                // 모든 공백을 제거해 공백을 제외하고 내용을 입력했는지 확인한다.
                if (!editStart.getText().toString().replaceAll(" ", "").equals("")
                        && !editEnd.getText().toString().replaceAll(" ", "").equals("")) {
                    FileInputStream fis; // 파일을 읽기 위한 변수
                    FileOutputStream fos; // 파일을 쓰기 위한 변수
                    String changedValue; // 수정된 내용을 반영하여 파일에 쓰기 위한 String변수

                    fis = openFileInput(FILENAME); // 파일을 읽기위해 연다
                    byte[] buffer = new byte[fis.available()]; // 파일에서 내용을 읽기위한 byte형 변수
                    fis.read(buffer); // 내용을 읽어 buffer변수에 넣는다.
                    changedValue = new String(buffer); // buffer변수를 string형으로 변환하여 string에 삽입한다.
                    fis.close(); // 파일을 닫는다.

                    // 변경전의 출발지->변경전의 도착지의 내용을 찾아 변경후의 출발지->변경후의 도착지로 수정한다.
                    // 변경후의 값은 EditText에 있으므로 getText로 받아온 후 String으로 변경해준다.
                    changedValue = changedValue.replace(startPoint+"->"+endPoint,
                            editStart.getText().toString()+"->"+editEnd.getText().toString());

                    fos = openFileOutput(FILENAME, Context.MODE_PRIVATE); // 내용을 덮어쓰기 위해 MODE_PRIVATE로 연다.

                    // 변경 전 경로와 변경 후 경로를 토스트를 이용해 알려준다.
                    Toast.makeText(getApplicationContext(),
                            startPoint + "에서 " + endPoint + "로 가는 경로를 " + editStart.getText() + "에서 " + editEnd.getText() + "로 가는 경로로 수정하였습니다.",
                            Toast.LENGTH_LONG).show();

                    fos.write(changedValue.getBytes()); // 변경된 내용을 byte로 변환하여 넣는다.
                    fos.close(); // 파일을 닫는다.

                    finish(); // 액티비티를 끝낸다.
                } else { // 출발지와 목적지가 입력되어 있지 않으면 입력하라는 안내문을 출력한다.
                    Toast.makeText(getApplicationContext(), "출발지와 목적지를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) { // 파일 예외처리
            e.printStackTrace();
        }
    }
}
