package com.example.a191006_airplane;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // 버튼들과 상호작용을 위해 선언된 변수들
    TextView total;
    TextView numofpeople;
    ImageView picture;
    int seatclass;
    int meal;
    int seatposition;
    int result;
    int people;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        total = findViewById(R.id.price); // 가격을 나타내는 TextView를 id를 통해 변수에 매칭시킨다.
        numofpeople = findViewById(R.id.people); // 인원수를 나타내는 TextView를 id를 통해 변수에 매칭시킨다.
        picture = findViewById(R.id.airimage); // 좌석등급의 이미지를 나타내는 ImageView를 id를 통해 변수에 매칭시킨다.

        // 각종 가격들을 0으로 초기화한다
        seatclass = 0;
        meal = 0;
        seatposition = 0;
        result = 0;

        // 인원수를 1로 초기화한다
        people = 1;
    }

    public void onClick(View view){
        // 버튼과 상호작용을 위한 switch문
        switch (view.getId()){ // 클릭된 id를 가져온다
            case R.id.first : // first 버튼이 클릭되면
                seatclass = 3000000; // 좌석 등급의 가격을 3000000로 한다.
                picture.setImageResource(R.drawable.first); // 좌석 등급에 맞는 이미지로 이미지뷰를 변경한다.
                break;
            case R.id.business : // business 버튼이 클릭되면
                seatclass = 2000000; // 좌석 등급의 가격을 2000000로 한다.
                picture.setImageResource(R.drawable.business); // 좌석 등급에 맞는 이미지로 이미지뷰를 변경한다.
                break;
            case R.id.economy : // economy 버튼이 클릭되면
                seatclass = 1000000; // 좌석 등급의 가격을 1000000로 한다.
                picture.setImageResource(R.drawable.economy); // 좌석 등급에 맞는 이미지로 이미지뷰를 변경한다.
                break;
            case R.id.korean : // korean 버튼이 클릭되면
                if(((CheckBox)view).isChecked()) // 만일 체크되어 있지 않다가 체크된 것이면
                    meal += 15000; // 기내식 가격에 15000을 더해준다
                else // 그렇지 않다면(즉, 체크가 해제된 것이면)
                    meal -= 15000; // 기내식 가격에 15000원을 빼준다.
                break;
            case R.id.western : // western 버튼이 클릭되면
                if(((CheckBox)view).isChecked()) // 만일 체크되어 있지 않다가 체크된 것이면
                    meal += 15000; // 기내식 가격에 15000을 더해준다
                else // 그렇지 않다면(즉, 체크가 해제된 것이면)
                    meal -= 15000; // 기내식 가격에 15000원을 빼준다.
                break;
            case R.id.chinese : // chinese 버튼이 클릭되면
                if(((CheckBox)view).isChecked()) // 만일 체크되어 있지 않다가 체크된 것이면
                    meal += 15000; // 기내식 가격에 15000을 더해준다
                else // 그렇지 않다면(즉, 체크가 해제된 것이면)
                    meal -= 15000; // 기내식 가격에 15000원을 빼준다.
                break;
            case R.id.aisle : // aisle 버튼이 클릭되면
                seatposition = 20000; // 좌석 위치에 따른 가격을 20000으로 한다.
                break;
            case R.id.window : // window 버튼이 클릭되면
                seatposition = 0; // 좌석 위치에 따른 가격을 0으로 한다.
                break;
            case  R.id.plus : // plus 버튼이 클릭되면
                people++; // 인원수를 1 늘린다.
                break;
            case R.id.minus : // minus 버튼이 클릭되면
                if(people > 1) // 인원수가 1명보다 많을 때만
                    people--; // 인원수를 1 줄인다.
                break;
        }

        result = (seatclass + meal + seatposition) * people; // 최종 가격은 ( 좌석등급비용 + 기내식 + 좌석위치에 따른 비용 ) * 인원수 이다.
        total.setText(Integer.toString(result)); // 최종가격으로 TextBox의 내용을 변경한다.
        numofpeople.setText(Integer.toString(people)); // 최종 인원수로 TextBox의 내용을 변경한다.
    }
}
