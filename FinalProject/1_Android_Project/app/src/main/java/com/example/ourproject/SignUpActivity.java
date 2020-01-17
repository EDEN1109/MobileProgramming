package com.example.ourproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignUpActivity extends AppCompatActivity {
    public static String IP_ADDRESS = "175.115.155.72";
    public static final int DIALOG_SIGN_UP_EMPTY = 1;
    public static final int DIALOG_SIGN_UP_OK = 2;
    public static final int DIALOG_SIGN_UP_ERROR = 3;
    EditText id;
    EditText nickname;
    EditText password;
    EditText phone;
    TextView checkIdText;
    TextView checkNicknameText;
    private boolean idCheck = false;
    private boolean nicknameCheck = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        id = findViewById(R.id.id);
        password = findViewById(R.id.password);
        nickname = findViewById(R.id.nickname);
        phone = findViewById(R.id.phone);
        checkIdText = findViewById(R.id.check_id_msg);
        checkNicknameText = findViewById(R.id.check_nickname_msg);

        id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    idCheck = false;
                    checkIdText.setText("아이디 중복검사를 해주세요.");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        nickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    nicknameCheck = false;
                    checkNicknameText.setText("닉네임 중복검사를 해주세요.");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void onClick(View view) {
        switch(view.getId()){
            case R.id.id_check:
                String idText = id.getText().toString();
                if(!idText.equals("")){
                ServerController task = new ServerController();
                task.execute("http://"+ IP_ADDRESS +"/validateId.php", idText, "ID");
                }
                else checkIdText.setText("아이디를 입력하세요.");
            break;
            case R.id.nickname_check:
                String nicknameText = nickname.getText().toString();
                if(!nicknameText.equals("")){
                    ServerController task = new ServerController();
                    task.execute("http://"+ IP_ADDRESS +"/validateNickname.php", nicknameText, "NICKNAME");
                }
                else checkNicknameText.setText("닉네임을 입력하세요.");
                break;
        }
        return;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id){
            case DIALOG_SIGN_UP_EMPTY :
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("회원가입 양식의 모든 칸을 채워주세요.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                return alert;
            case DIALOG_SIGN_UP_OK :
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("회원가입이 완료되었습니다.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), LogInAcitivity.class));
                            }
                        });
                AlertDialog alert1 = builder1.create();
                return alert1;
            case DIALOG_SIGN_UP_ERROR :
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setMessage("아이디 또는 닉네임을 다시 확인하세요.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert2 = builder2.create();
                return alert2;
        }
        return super.onCreateDialog(id);
    }

    public void signUp(View view){
            String idText = id.getText().toString();
            String pwText = password.getText().toString();
            String nickText = nickname.getText().toString();
            String phoneText = phone.getText().toString();

            if(idText.equals("") || pwText.equals("") || nickText.equals("") || phoneText.equals("")){
                showDialog(DIALOG_SIGN_UP_EMPTY);
            }
            else if(!idCheck || !nicknameCheck){
                showDialog(DIALOG_SIGN_UP_ERROR);
            }
            else{
                showDialog(DIALOG_SIGN_UP_OK);
                ServerController task = new ServerController();
                task.execute("http://"+IP_ADDRESS+"/insertUserInfo.php", idText, pwText, nickText, phoneText, "SIGNUP");
            }
    }

    class ServerController extends AsyncTask<String, Void, String> {
        //ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /*
            progressDialog = ProgressDialog.show(getActivity(),
                    "Please Wait", null, true, true);
             */
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals("itrue")) {
                checkIdText.setText("사용 가능한 아이디 입니다.");
                idCheck = true;
            }
            else if (result.equals("ifalse")){
                checkIdText.setText("중복된 아이디 입니다. 다시 입력하세요.");
                idCheck = false;
            }
            else if(result.equals("ntrue")){
                checkNicknameText.setText("사용 가능한 닉네임 입니다.");
                nicknameCheck = true;
            }
            else if(result.equals("nfalse")){
                checkNicknameText.setText("중복된 닉네임입니다. 다시 입력하세요.");
                nicknameCheck = false;
            }
            else{
                Log.d("ERROR", "서버 오류");
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];
            String postParameters ="";
            if(params[2].equals("ID")){
                postParameters ="id=" + params[1];
            }
            else if(params[2].equals("NICKNAME")){
                postParameters = "nickname=" + params[1];
            }
            else if(params[params.length -1].equals("SIGNUP")){
                postParameters = "id=" + params[1] + "&password=" + params[2]
                        + "&nickname=" + params[3] + "&phone=" + params[4];
            }



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
                Log.d("AAAAA", "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();


                Log.d("sb.toString()", sb.toString());
                return sb.toString();

            } catch (Exception e) {

                Log.d("AAAAA", "GetData : Error ", e);
                return null;
            }

        }
    }

}

