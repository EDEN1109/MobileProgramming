package com.example.searchpath;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddBookMarkActivity extends AppCompatActivity {

    private final String FILENAME = new MainActivity().getFILENAME(); // 파일이름을 모두 같게하기 위해 변수에서 받아온다.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addbookmark);
    }

    public void onClick(View view) throws IOException
    {
        EditText start = (EditText)findViewById(R.id.start); // 아이디를 통해 해당하는 EditText를 찾아 할당한다.
        EditText end = (EditText)findViewById(R.id.end); // 아이디를 통해 해당하는 EditText를 찾아 할당한다.

        if(R.id.addBtn == view.getId()) // 추가 버튼을 눌렀다면
        {
            // 출발지와 도착지의 EditText에 내용을 입력했다면 을 뜻하는 if문
            // 모든 공백을 제거해 공백을 제외하고 내용을 입력했는지 확인한다.
            if(!start.getText().toString().replaceAll(" ","").equals("")
                    && !end.getText().toString().replaceAll(" ","").equals("") )
            {
                FileOutputStream fos; // 파일을 쓰기 위한 변수
                // 내용을 저장파일 끝에 추가하기 위해 MODE_APPEND 혹은 MODE_PRIVATE로 연다.
                fos = openFileOutput(FILENAME, Context.MODE_APPEND|Context.MODE_PRIVATE);
                // 추가한 경로를 토스트를 통해 알려준다.
                Toast.makeText(getApplicationContext(), start.getText()+"에서 "+end.getText()+"로 가는 경로를 추가하였습니다.",
                        Toast.LENGTH_SHORT).show();
                fos.write(start.getText().toString().getBytes()); // 입력한 출발지를 byte로 변환하여 문장에 추가한다.
                fos.write("->".getBytes()); // 출발지와 도착지 사이에 ->를 byte로 변환하여 문장에 추가한다.
                fos.write(end.getText().toString().getBytes()); // 입력한 도착지를 byte로 변환하여 문장에 추가한다.
                fos.write("\n".getBytes()); // 문장의 끝에 \n을 byte로 변환하여 추가한다.
                fos.close(); // 파일을 닫는다.

                finish(); // 액티비티를 끝낸다.
            }
            else
            {  // 출발지와 목적지가 입력되어 있지 않으면 입력하라는 안내문을 출력한다.
                Toast.makeText(getApplicationContext(), "출발지와 목적지를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}