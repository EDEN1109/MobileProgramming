package com.example.ourproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ourproject.ui.home.HomeFragment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LogInAcitivity extends AppCompatActivity {
    static final int DIALOG_LOGIN_NOT_EXIST = 1;
    static final int DIALOG_LOGIN_FAIL = 2;
    private final String FILENAME = "session.txt";
    TextView signUpText;
    EditText id;
    EditText password;
    public static String IP_ADDRESS = "175.115.155.72";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signUpText = findViewById(R.id.signUp);
        signUpText.setPaintFlags(signUpText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        signUpText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
            }
        });
        id = findViewById(R.id.id);
        password = findViewById(R.id.password);

        try {
            FileInputStream fis = openFileInput(FILENAME);
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            fis.close();

            if(new String(bytes).length() > 0)
            {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void signUp(View view){
        startActivity(new Intent(this, SignUpActivity.class));
    }

    public void logIn(View view){
        String idText = id.getText().toString();
        String pwText = password.getText().toString();
        ServerController task = new ServerController();
        task.execute("http://"+ IP_ADDRESS +"/logIn.php", idText, pwText);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_LOGIN_NOT_EXIST:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("회원 정보가 존재하지 않습니다.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                return alert;
            case DIALOG_LOGIN_FAIL:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("아이디 또는 비밀번호가 일치하지 않습니다.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert1 = builder1.create();
                return alert1;
        }
        return super.onCreateDialog(id);
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
            if(result.equals("nexist")){
                showDialog(DIALOG_LOGIN_NOT_EXIST);
            }
            if(result.equals("existfalse")){
               showDialog(DIALOG_LOGIN_FAIL);
            }
            else if(result.equals("existtrue")){

                try {
                    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    fos.write(id.getText().toString().getBytes());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //로그인 성공, homefragment로 이동
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];
            String postParameters = "id=" + params[1] + "&password=" + params[2];
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
