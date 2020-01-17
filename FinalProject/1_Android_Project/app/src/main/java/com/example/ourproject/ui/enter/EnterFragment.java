package com.example.ourproject.ui.enter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.ourproject.DataBaseController;
import com.example.ourproject.R;
import com.example.ourproject.SearchAddressActivity;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static android.util.Patterns.IP_ADDRESS;

public class EnterFragment extends Fragment {
   // private static String IP_ADDRESS = "localhost/insert.php";
    private static String IP_ADDRESS = "175.115.155.72";
    private static final int GET_ADDRESS = 3;
    String uploadFilePath = "";
    String upLoadServerUri = "";
    int serverResponseCode = 0;

    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.KOREA);
    SimpleDateFormat format_date = new SimpleDateFormat("yyyy/MM/dd", Locale.KOREA);
    SimpleDateFormat format_h = new SimpleDateFormat("HH", Locale.KOREA);
    SimpleDateFormat format_t = new SimpleDateFormat("mm", Locale.KOREA);

    private DataBaseController controller;
    private EditText title;
    private EditText people;
    private EditText orgPrice;
    private TextView gbPrice;
    private ImageView image;
    private RadioGroup route;
    private EditText description;
    private Button plus;
    private Button minus;
    private EditText address;
    private EditText maddress;
    private EditText weight;
    private EditText unit;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button enterImage;
    private Button enterProductInfo;
    private Button searchAddress;
    private int num = 2;
    private boolean isSet = false;

    private static final int PICK_FROM_ALBUM = 1;
    private File tempFile;

    public boolean isCurrnetDate(){
        if(
                (format_date.format(System.currentTimeMillis()).equals(
                        datePicker.getYear() + "/" + Integer.toString(new Integer(datePicker.getMonth())+1) + "/" + datePicker.getDayOfMonth())
                )
        )
        {
            return true;
        }
        else{
            return false;
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_enter, container, false);

        controller = new DataBaseController(this.getContext());

        title = (EditText) root.findViewById(R.id.title_enter);
        orgPrice = (EditText) root.findViewById(R.id.price_enter);
        gbPrice = (TextView) root.findViewById(R.id.cal_price_enter);
        image = (ImageView) root.findViewById(R.id.image_enter);
        route = (RadioGroup) root.findViewById(R.id.route_enter);
        people = (EditText) root.findViewById(R.id.people_enter);
        plus = (Button) root.findViewById(R.id.plus);
        minus = (Button) root.findViewById(R.id.minus);
        address = (EditText) root.findViewById(R.id.address_enter);
        weight = (EditText) root.findViewById(R.id.weight_enter);
        unit = (EditText) root.findViewById(R.id.unit_enter);
        enterImage = (Button) root.findViewById(R.id.image_btn);
        datePicker = (DatePicker) root.findViewById(R.id.datePicker);
        timePicker = (TimePicker) root.findViewById(R.id.timePicker);
        description = (EditText) root.findViewById(R.id.data_enter);
        enterProductInfo = (Button) root.findViewById(R.id.productInfo_enter);
        plus.setOnClickListener(onClick);
        minus.setOnClickListener(onClick);
        enterImage.setOnClickListener(onClick);
        enterProductInfo.setOnClickListener(onClick);
        enterImage.setOnClickListener(onClick);
        searchAddress = (Button) root.findViewById(R.id.address_search);
        maddress = (EditText) root.findViewById(R.id.maddress_enter);
        datePicker.setMinDate(System.currentTimeMillis());
        timePicker.setIs24HourView(Boolean.TRUE);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                String currnet_h = format_h.format(System.currentTimeMillis());
                String currnet_t = format_t.format(System.currentTimeMillis());

                //현재 날짜와 선택한 날짜와 같을 떄만 시간 제한을 함
                if(isCurrnetDate())
                {
                    //현재 시보다 지정한 시가 작으면 > 시를 막음
                    if(i < Integer.parseInt(currnet_h)){
                        timePicker.setHour(Integer.parseInt(format_h.format(System.currentTimeMillis())));

                    }
                    //현재 시와 설정한 시가 같은 때 > 분을 막음
                    else if(i == Integer.parseInt(currnet_h)){
                        if(i1 < Integer.parseInt(currnet_t)){
                            timePicker.setMinute(Integer.parseInt(format_t.format(System.currentTimeMillis())));
                        }
                    }
                }
            }
        });

        searchAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent aintent = new Intent(getActivity(), SearchAddressActivity.class);
                startActivityForResult(aintent, GET_ADDRESS);
            }
        });

        tedPermission();
        upLoadServerUri = "http://175.115.155.72/upload.php";
        orgPrice.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String price = orgPrice.getText().toString();
                if ("".equals(price)) {
                    price = "0";
                }
                gbPrice.setText(Integer.toString(Integer.parseInt(price) / num));
            }
        });

        return root;
    }


    private void tedPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {

            }
        };

        TedPermission.with(this.getContext())
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                //참여인원 +1
                case R.id.plus:
                    num++;
                    people.setText(Integer.toString(num));
                    if (orgPrice.getText() != null)
                        gbPrice.setText(Integer.toString(Integer.parseInt(orgPrice.getText().toString()) / num));

                    System.out.println(route.getCheckedRadioButtonId());
                    System.out.println(weight.getText());
                    System.out.println(unit.getText());
                    break;
                //참여인원 -1, 최소값은 2
                case R.id.minus:
                    if (num > 2) num--;
                    people.setText(Integer.toString(num));
                    if (orgPrice.getText().toString() != null)
                        gbPrice.setText(Integer.toString(Integer.parseInt(orgPrice.getText().toString()) / num));
                    break;
                //데이터베이스에 정보 전송, 메인화면으로 이동
                case R.id.productInfo_enter:
                    //임의로 넣음
                    String _title = title.getText().toString();
                    String _ddate =
                            datePicker.getYear() + "/" + Integer.toString(new Integer(datePicker.getMonth())+1) + "/" + datePicker.getDayOfMonth() + "/" +
                                    timePicker.getHour() + "/" + timePicker.getMinute() + "/" + "59" ;
                    String _edate = format.format(System.currentTimeMillis());
                    String _eGroup = "1"; //현재 참여 인원
                    int mGroup_int = Integer.parseInt(people.getText().toString());
                    String _mGroup = people.getText().toString(); //최대 인원
                    int _orgPrice_int = Integer.parseInt(orgPrice.getText().toString());
                    String _orgPrice = orgPrice.getText().toString();
                    String _gbPrice = Integer.toString(_orgPrice_int / mGroup_int);
                    String _address = address.getText().toString() + " ";
                    String _maddress = maddress.getText().toString() + " ";
                    String _weight = weight.getText().toString();
                    String _gbWeight = Double.toString(Double.parseDouble(weight.getText().toString()) / num);
                    String _unit = unit.getText().toString();
                    int route_int = route.getCheckedRadioButtonId();
                    String _route = "";
                    if (route_int == 1) {
                        _route = "직접 만나서 분배 ";
                    } else {
                        _route = "다른 장소를 이용 ";
                    }
                    String _description = description.getText().toString() + " ";
                    String _uid = "";

                    try {
                        FileInputStream fis = getActivity().openFileInput("session.txt");
                        byte[] buffer = new byte[fis.available()];
                        fis.read(buffer);

                        _uid = new String(buffer);

                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    InsertData task = new InsertData();
                    task.execute("http://" + IP_ADDRESS + "/insertBoardInfo.php", "ENTER",
                            _title, _ddate, _edate, _eGroup, _mGroup, _orgPrice, _gbPrice, _route,
                            _description, _uid, _address, _maddress, _weight, _gbWeight, _unit);

                    break;
                //갤러리로 이동
                case R.id.image_btn:
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    startActivityForResult(intent, PICK_FROM_ALBUM);
                    break;

            }
        }
    };

    class InsertData extends AsyncTask<String, Void, String> {
        //ProgressDialog progressDialog;
        String imgText;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result == null)
                Toast.makeText(getContext(), "빈 칸을 모두 채워주세요.", Toast.LENGTH_SHORT).show();
            else {
                OnCreateDialog("공구 등록이 완료되었습니다.");
                image.setImageResource(R.drawable.ic_photo_white_24dp2);
                InitAll();
            }
        }

        public void imageUpload() {

            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;

            String fileName = uploadFilePath;
            File sourceFile = new File(uploadFilePath);
            if (!sourceFile.isFile()){
                Log.e("uploadFile", "Source File not exist :"
                        + uploadFilePath);
            } else {
                try {
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);

                    URL url = new URL("http://" + IP_ADDRESS + "/upload.php");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setReadTimeout(5000);
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                    httpURLConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    httpURLConnection.setRequestProperty("uploaded_file", fileName);
                    httpURLConnection.connect();

                    dos = new DataOutputStream(httpURLConnection.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + fileName + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    serverResponseCode = httpURLConnection.getResponseCode();

                    String serverResponseMessage = httpURLConnection.getResponseMessage();


                    int responseStatusCode = httpURLConnection.getResponseCode();
                    Log.d(TAG, "POST response code - " + responseStatusCode);

                    dos.flush();
                    dos.close();

                    responseStatusCode = httpURLConnection.getResponseCode();
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
                    imgText = sb.toString();
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    Log.d("Upload file to server", "error: " + ex.getMessage(), ex);
                } catch (Exception e) {
                    Log.d(TAG, "InsertData: Error ", e);
                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!DB insert Error");
                }
            }
        }

        @Override
        protected String doInBackground(String... params) {
            imageUpload();
            String serverURL = (String) params[0];
            String postParameters = null;

            if((String)params[1]=="ENTER") {
                if(!isAllSelected()) {
                    return null;
                }
                String title = (String) params[2];
                String ddate = (String) params[3];
                String edate = (String) params[4];
                String egroup = (String) params[5];
                String mgroup = (String) params[6];
                String orgPrice = (String) params[7];
                String gbPrice = (String) params[8];
                String route = (String) params[9];
                String description = (String) params[10];
                String uid = (String) params[11];
                String addr = (String) params[12];
                String maddr = (String) params[13];
                String orgWei = (String) params[14];
                String gbWei = (String) params[15];
                String unit = (String) params[16];

                postParameters =
                        "title=" + title + "&ddate=" + ddate + "&edate=" + format.format(System.currentTimeMillis()) +
                                "&egroup=" + Integer.parseInt(egroup) + "&mgroup=" + Integer.parseInt(mgroup) +
                                "&orgPrice=" + Integer.parseInt(orgPrice) + "&gbPrice=" + Integer.parseInt(gbPrice) +
                                "&description=" + description + "&uid=" + uid + "&address=" + addr +
                                "&image=" + imgText + "&way=" + route + "&maddress=" + maddr + "&orgWeight=" + orgWei +
                                "&gbWeight=" + gbWei + "&danwi=" + unit;
            }
            else
                return null;

                try {
                    URL url = new URL(serverURL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setReadTimeout(5000);
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.connect();

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    outputStream.write(postParameters.getBytes("UTF-8"));
                    outputStream.flush();
                    outputStream.close();

                    int responseStatusCode = httpURLConnection.getResponseCode();
                    Log.d(TAG, "POST response code - " + responseStatusCode);

                    InputStream inputStream;
                    if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                        inputStream = httpURLConnection.getInputStream();
                    } else {
                        inputStream = httpURLConnection.getErrorStream();
                    }

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    bufferedReader.close();

                    return sb.toString();
                } catch (Exception e) {
                    Log.d(TAG, "InsertData: Error ", e);

                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!DB insert Error");
                    return new String("Error: " + e.getMessage());
                }


        }
    }


    public void OnCreateDialog(String text)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(text).setPositiveButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this.getContext(), "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if (tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e("갤러리", tempFile.getAbsolutePath() + "삭제 성공");
                        tempFile = null;
                    }
                }
            }
        }
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == PICK_FROM_ALBUM) {

                Uri photoUri = data.getData();

                Cursor cursor = null;

                try {
                    String[] proj = {MediaStore.Images.Media.DATA};

                    assert photoUri != null;
                    cursor = this.getContext().getContentResolver().query(photoUri, proj, null, null);

                    assert cursor != null;
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                    cursor.moveToFirst();

                    tempFile = new File(cursor.getString(column_index));
                    uploadFilePath = tempFile.toString();
                    setImage();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else if (requestCode == GET_ADDRESS) {
                String addressText = data.getStringExtra("ADDRESS");
                address.setText(addressText);
            }
        }
    }

    private void InitAll() {
        title.setText(null);
        people.setText("2");
        orgPrice.setText("0");
        gbPrice.setText("____");
        image.setImageResource(R.drawable.ic_photo_white_24dp2);
        route.clearCheck();
        description.setText(null);
        weight.setText(null);
        unit.setText(null);
        address.setText(null);
        maddress.setText(null);
        datePicker.setMinDate(System.currentTimeMillis());
        timePicker.setHour(Integer.parseInt(format_h.format(System.currentTimeMillis())));
        timePicker.setMinute(Integer.parseInt(format_t.format(System.currentTimeMillis())));
        num = 2;
    }

    private boolean isAllSelected() {
        if(title.getText().equals(""))
            return false;
        if(orgPrice.getText().equals("0"))
            return false;
        if(!isSet)
            return false;
        if(description.getText().equals(""))
            return false;
        if(route.getCheckedRadioButtonId() == -1)
            return false;
        if(weight.getText().equals(""))
            return false;
        if(unit.getText().equals(""))
            return false;
        if(address.getText().equals(""))
            return false;
        if(maddress.getText().equals(""))
            return false;
        return true;
    }

    private void setImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);

        image.setImageBitmap(originalBm);
        if(originalBm!=null) {
            isSet = true;
        }
        else
        {
            Toast.makeText(getContext(), "이미지 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
            image.setImageResource(R.drawable.ic_photo_white_24dp2);
        }
    }
}