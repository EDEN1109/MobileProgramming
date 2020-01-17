package com.example.ourproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

public class PayActivity extends AppCompatActivity {
    private static String IP_ADDRESS = "175.115.155.72";
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    ArrayList<String> paymentsList = new ArrayList<String>();
    ArrayAdapter<String> paymentsAdapter;
    private int productID;
    private String productName;
    private String productPrice;
    private boolean isInit = false;
    private String id;

    // 구글 결제
    private PaymentsClient mPaymentsClient;
    private View mGooglePayButton;
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        ServerController task = new ServerController();
        task.execute("http://" + IP_ADDRESS + "/select.php", "CALL", Integer.toString(productID));
        try {
            FileInputStream fis = openFileInput("session.txt");
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            id = new String(buffer);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        productID = intent.getIntExtra("productID", -1);

        if (productID == -1) {
            Toast.makeText(this, "상품 아이디를 불러오지 못했습니다.", Toast.LENGTH_LONG).show();
            finish();
        }

        // 구글 결제 API
        // Set up the mock information for our item in the UI.
        mGooglePayButton = findViewById(R.id.Pay_Button);

        // Initialize a Google Pay API client for an environment suitable for testing.
        // It's recommended to create the PaymentsClient object inside of the onCreate method.
        mPaymentsClient = PaymentsUtil.createPaymentsClient(this);
        possiblyShowGooglePayButton();

        mGooglePayButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestPayment(view);
                    }
                });
    }

    class ServerController extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(!isInit)
            {
                initItemUI(result);
                isInit = true;
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];
            String postParameters;
            if(params[1].equals("JOIN"))
                postParameters = "id=" + params[2] + "&num=" + params[3] + "&edate=" +params[4];
            else if(params[1].equals("CALL"))
                postParameters = "id=" + params[2];
            else
                return null;
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

    // 구글 결제 API
    private void possiblyShowGooglePayButton() {
        final Optional<JSONObject> isReadyToPayJson = PaymentsUtil.getIsReadyToPayRequest();
        if (!isReadyToPayJson.isPresent()) {
            return;
        }
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
        if (request == null) {
            return;
        }

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        Task<Boolean> task = mPaymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(this,
                new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            setGooglePayAvailable(task.getResult());
                        } else {
                            Log.w("isReadyToPay failed", task.getException());
                        }
                    }
                });
    }

    private void setGooglePayAvailable(boolean available) {
        if (available) {
            mGooglePayButton.setVisibility(View.VISIBLE);
        } else {
            // 구글 페이 사용 불가일때
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // value passed in AutoResolveHelper
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        handlePaymentSuccess(paymentData);
                        break;
                    case Activity.RESULT_CANCELED:
                        // Nothing to here normally - the user simply cancelled without selecting a
                        // payment method.
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        handleError(status.getStatusCode());
                        break;
                    default:
                        // Do nothing.
                }

                // Re-enables the Google Pay payment button.
                mGooglePayButton.setClickable(true);
                break;
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        String paymentInformation = paymentData.toJson();

        // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
        if (paymentInformation == null) {
            return;
        }
        JSONObject paymentMethodData;

        try {
            paymentMethodData = new JSONObject(paymentInformation).getJSONObject("paymentMethodData");
            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".
            if (paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("type")
                    .equals("PAYMENT_GATEWAY")
                    && paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token")
                    .equals("examplePaymentMethodToken")) {
                String edate = format.format(System.currentTimeMillis());
                ServerController join = new ServerController();
                join.execute("http://" + IP_ADDRESS + "/updateEnterInfo.php", "JOIN", id, Integer.toString(productID), edate);

                AlertDialog alertDialog =
                        new AlertDialog.Builder(this)
                                .setMessage(
                                        "공구 결제가 완료되었습니다.")
                                .setPositiveButton("OK",  new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        dialog.dismiss();
                                        finish();
                                    }
                                })
                                .create();
                alertDialog.show();
            }

            String billingName =
                    paymentMethodData.getJSONObject("info").getJSONObject("billingAddress").getString("name");
            Log.d("BillingName", billingName);

            // Logging token string.
            Log.d("GooglePaymentToken", paymentMethodData.getJSONObject("tokenizationData").getString("token"));
        } catch (JSONException e) {
            Log.e("handlePaymentSuccess", "Error: " + e.toString());
            return;
        }
    }

    private void handleError(int statusCode) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode));
    }

    // This method is called when the Pay with Google button is clicked.
    public void requestPayment(View view) {
        // Disables the button to prevent multiple clicks.
        mGooglePayButton.setClickable(false);

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.

        // TransactionInfo transaction = PaymentsUtil.createTransaction(price);
        Optional<JSONObject> paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(productPrice);
        if (!paymentDataRequestJson.isPresent()) {
            return;
        }
        PaymentDataRequest request =
                PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        if (request != null) {
            AutoResolveHelper.resolveTask(
                    mPaymentsClient.loadPaymentData(request), this, LOAD_PAYMENT_DATA_REQUEST_CODE);
        }
    }

    private void initItemUI(String mJsonString) {
        TextView itemName = findViewById(R.id.title_pay);
        TextView itemTime = findViewById(R.id.time_pay);
        TextView itemWeight = findViewById(R.id.weight_pay);
        TextView itemPrice = findViewById(R.id.price_pay);

        String TAG_JSON="webnautes";
        String TAG_NUM = "num";
        String TAG_TITLE = "title";
        String TAG_DDATE = "ddate";
        String TAG_GBPRICE = "gbPrice";
        String TAG_GBWEIGHT = "gbWeight";
        String TAG_UNIT = "danwi";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                if (Integer.parseInt(item.getString(TAG_NUM)) == productID) {
                    String title = item.getString(TAG_TITLE);
                    String ddate = item.getString(TAG_DDATE);
                    String gbprice = item.getString(TAG_GBPRICE);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                    long now = System.currentTimeMillis();
                    Date todaCal = new Date(now); //오늘날자 가져오기
                    Date ddayCal = simpleDateFormat.parse(ddate); //오늘날자를 가져와 변경시킴
                    long calculate = ddayCal.getTime() - todaCal.getTime(); // 오늘 날짜에서 dday 날짜를 빼주게 됩니다.
                    long day = (calculate / (60 * 60 * 1000));
                    long hour = ((calculate / (60 * 1000)) % 60);
                    long sec = ((calculate / 1000) % 60);
                    String time = null;
                    if (day <= 0 && hour <= 0 && sec <= 0)
                        time = "종료";
                    else
                        time = day + ":" + hour + ":" + sec;

                    productName = title;
                    productPrice = gbprice;
                    itemName.setText(title);
                    itemTime.setText(ddate);
                    itemPrice.setText(gbprice+"원");
                    itemWeight.setText(item.getString(TAG_GBWEIGHT) + item.getString(TAG_UNIT));
                    break;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
        }
    }
}
