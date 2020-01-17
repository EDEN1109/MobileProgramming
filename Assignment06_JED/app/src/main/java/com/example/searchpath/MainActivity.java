package com.example.searchpath;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ListView m_ListView; // 리스트뷰를 할당할 변수
    private ArrayAdapter<String> m_Adapter; // String으로 이루어진 어댑터
    private String FILENAME = "direction.txt"; // 파일 이름을 설정한다.
    private ArrayList<String> values = new ArrayList<String>(); // 값의 삭제와 추가를 원활하게 하기 위해 ArrayList로 String배열을 생성

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setValues(); // values의 값을 설정한다.
        m_ListView = findViewById(R.id.list); // 리스트뷰의 아이디를 통해 리스트 뷰를 할당한다.
        m_ListView.setOnItemClickListener(onClickListItem); // 리스트뷰에 클릭리스너를 설정한다.
        m_Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values); // 리스트 형식으로 values의 값을 어댑터에 적용한다.
        m_ListView.setAdapter(m_Adapter); // m_Adapter를 리스트뷰의 어댑터로 설정한다.
        registerForContextMenu(m_ListView); // 플로팅 컨텍스트 메뉴를 위해 m_ListView를 등록해준다.
    }

    @Override
    protected void onResume() { // 다시 이 액티비티가 활성화 될 때
        super.onResume();

        setValues(); // values의 값을 설정해준다.
        m_Adapter.notifyDataSetChanged(); // m_Adapter에 변경사항을 업데이트 해준다.
    }

    // 리스트뷰의 아이템을 클릭하면 실행되는 함수
    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = null; // 초기 인텐트는 null로 설정한다.
            // 선택한 아이템을 ->를 기준으로 문장을 나누어 after에 저장한다.
            // after[0]는 출발지, after[1]은 도착지가 된다.
            String[] after = m_Adapter.getItem(position).split("->");

            for(int i=0;i<after.length;i++)
            {
                after[i].replaceAll(" ", "+"); // Uri의 양식에 맞추기 위해 출발지와 도착지의 띄어쓰기를 +로 바꾸어준다.
            }

            if(after.length==2) // after에 출발지와 도착지가 알맞게 들어왔다면
            {
                // 인텐트에 Uri 주소를 해당 사이트의 양식에 맞게 적용하여 할당한다.
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/"+after[0]+"/"+after[1]));
            }

            if(intent!=null) // 인텐트가 정상적으로 할당되었다면
            {
                startActivity(intent); // 액티비티를 활성화시킨다.
            }
        }
    };

    // 상단 액션바에 메뉴를 생성하는 함수
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu); // main_menu.xml을 적용하여 메뉴를 생성한다.
        return super.onCreateOptionsMenu(menu);
    }

    // 상단 액션바를 이용해 북마크를 추가하는 뷰를 띄우는 함수
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuAddBtn) { // 눌린 버튼이 메뉴의 Add버튼이라면
            Intent intent = new Intent(this, AddBookMarkActivity.class); // 목적지를 추가하는 액티비티를 인텐트에 할당한다.
            startActivity(intent); // 액티비티를 실행한다.
        }
        return super.onOptionsItemSelected(item);
    }

    // 플로팅 메뉴 생성 코드
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("메뉴"); // 타이틀을 메뉴로 설정한다.
        getMenuInflater().inflate(R.menu.floating_menu, menu); // floating_menu.xml을 플로팅 컨텍스트 메뉴로 추가한다.
    }

    // 플로팅 메뉴 선택시 코드
    @Override
    public  boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo(); // 컨텍스트 메뉴에서 선택된 아이템을 할당한다.
        String selectedItem = m_Adapter.getItem(info.position); // 선택한 아이템의 내용을 저장한다.

        if(item.getItemId() == R.id.edit) // 수정을 선택한다면
        {
            Intent intent = new Intent(MainActivity.this, ReviseBookMarkActivity.class); // 인텐트에 수정하는 액티비티를 할당한다.
            // 선택한 아이템을 ->를 기준으로 문장을 나누어 after에 저장한다.
            // after[0]는 출발지, after[1]은 도착지가 된다.
            String[] after = selectedItem.split("->");

            if(intent!=null&&after.length==2) // 인텐트에 액티비티가 정상적으로 할당되었고 after에 출발지와 목적지가 정상적으로 저장되었다면
            {
                intent.putExtra("start", after[0]); // 호출하는 액티비티(수정 액티비티)에 key를 start로 하여 출발지를 전달한다.
                intent.putExtra("end", after[1]); // 호출하는 액티비티(수정 액티비티)에 key를 end로 하여 도착지를 전달한다.
                startActivity(intent); // 액티비티를 활성화한다.
            }
            return true; // 해당 함수 내에서 하고자 하는 일을 모두 수행했고 추가적인 함수를 필요로 하지 않으므로 true를 반환한다.
        }
        if(item.getItemId() == R.id.del) // 삭제를 선택한다면
        {
            String changedValue; // 삭제된 내용을 반영하여 파일에 쓰기 위한 String변수

            try {
                FileInputStream fis; // 파일을 읽기 위한 변수
                fis = openFileInput(FILENAME); // 파일을 읽기위해 연다
                byte[] buffer = new byte[fis.available()]; // 파일에서 내용을 읽기위한 byte형 변수

                fis.read(buffer); // 내용을 읽어 buffer변수에 넣는다.

                changedValue = new String(buffer); // buffer변수를 string형으로 변환하여 string에 삽입한다.
                // 삭제하고자 하는 내용을 삭제하여 내용을 변경한다.
                // 이때 줄바꿈 문자인 \n도 함께 삭제한다.
                changedValue = changedValue.replace(selectedItem+"\n", "");

                fis.close(); // 파일을 닫는다.

                FileOutputStream fos; // 파일을 쓰기 위한 변수
                fos = openFileOutput(FILENAME, Context.MODE_PRIVATE); // 내용을 덮어쓰기 위해 MODE_PRIVATE로 연다.
                fos.write(changedValue.getBytes()); // 변경된 내용을 byte로 변환하여 넣는다.

                fos.close(); // 파일을 닫는다.
            } catch (IOException e) { // 파일 예외처리
                e.printStackTrace();
            }

            setValues(); // 변경된 내용을 values에 적용한다.
            m_Adapter.notifyDataSetChanged(); // 변경 사항을 적용하기 위해 m_Adapter를 업데이트 해준다.

            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void setValues() {
        FileInputStream fis; // 파일을 읽기 위한 변수

        try {
            fis = openFileInput(FILENAME); // 파일을 읽기로 연다.
            byte[] buffer = new byte[fis.available()]; // 파일에서 내용을 읽기위한 byte형 변수

            fis.read(buffer); // 내용을 읽어 buffer변수에 넣는다.

            // \n을 기준으로 문장을 나누어 저장한다.
            // 출발지->도착지 형태로 저장되게 된다.
            String[] value = new String(buffer).split("\n");

            values.clear(); // 기존에 values에 있던 값을 모두 비워준다.

            for(int i = 0; i<value.length; i++){ // 출발지->도착지의 개수만큼 반복한다.
                values.add(value[i]); // values에 내용을 추가한다.
            }

            fis.close(); // 파일을 닫는다.
        } catch (IOException e) { // 파일 예외처리
            e.printStackTrace();
        }
    }

    public String getFILENAME() { // 파일 이름을 반환하는 함수로 수정, 추가에서 파일명을 통일하기 위함이다.
        return FILENAME; // 파일명을 반환한다.
    }
}
