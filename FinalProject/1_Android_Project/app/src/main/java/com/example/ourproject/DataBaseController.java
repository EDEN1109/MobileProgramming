package com.example.ourproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.ArrayList;

public class DataBaseController {
    private SQLiteDatabase database;
    private File file;
    private Context context;
    private int num = 1;

    public DataBaseController() {
    }
    public DataBaseController(Context _context) {
        context = _context;
        if(database == null) {
            SQLiteDatabase db = null;
            file = new File(context.getFilesDir(), "system.db");
            try {
                db = SQLiteDatabase.openOrCreateDatabase(file, null);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
            database = db;
            initTable();
            getLastProductID();
        }

    }

    private SQLiteDatabase initDB(Context _context){
        context = _context;
        SQLiteDatabase db = null;
        file = new File(context.getFilesDir(), "system.db");
        try {
            db = SQLiteDatabase.openOrCreateDatabase(file, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        return db;
    }

    // 테이블을 생성하는 코드
    public void initTable() {
        if (database != null) {
            String sqlCreateUserTable = "CREATE TABLE IF NOT EXISTS USER(ID VARCHAR(20) NOT NULL, PW VARCHAR(20), NICKNAME VARCHAR(10), PHONE VARCHAR(20), PRIMARY KEY(ID));";
            String sqlCreateBoardTable = "CREATE TABLE IF NOT EXISTS BOARD(NUM INT NOT NULL, CATEGORY VARCHAR(20), TITLE VARCHAR(50), DDATE DATE, EDATE DATE, EGROUP INT, MGROUP INT, ORGPRICE INT, GBPRICE INT, IMAGE VARCHAR(20), ROUTE INT, DESCRIPTION TEXT, UID VARCHAR(20), FOREIGN KEY(UID) REFERENCES USER(ID), PRIMARY KEY(NUM))";
            String sqlCreateEnterTable = "CREATE TABLE IF NOT EXISTS PARTICIPATE(NUM INT NOT NULL, ID VARCHAR(20), DATE DATE,FOREIGN KEY(NUM) REFERENCES BOARD(NUM), FOREIGN KEY(ID) REFERENCES USER(ID), PRIMARY KEY(NUM, ID))";

            System.out.println(sqlCreateUserTable);
            database.execSQL(sqlCreateUserTable);
            database.execSQL(sqlCreateBoardTable);
            database.execSQL(sqlCreateEnterTable);
        }
    }

    // 새로 가입한 User를 추가한다.
    public int addUserInfo(String id, String pw, String name, String phone) {
        return 0; // 잘 추가되었다면 0을, 중간에 오류가 발생하면 -1을 반환
    }

    // 공구에 참여하면 추가한다.
    // 참여를 추가한 후 상품의 참여 인원과 현재 참여 인원을 비교하여, 참여인원이 꽉차면 알람을 준다.
    public int addJoinInfo(String userID, String productID) {
        database.execSQL("INSERT INTO BOARD VALUES(" + userID + "," + productID + ")");
        return 0; // 잘 추가되었다면 0을, 중간에 오류가 발생하면 -1을 반환
    }

    // 상품을 추가하는 코드
    // 상품을 등록한 User의 ID도 함께 등록된다. 다(상품) 대 1(User)의 관계이므로 따로 분리하지 않았다.
    public int addProductInfo(String title, String ddate, String edate, int egroup, int mgroup, int orgPrice, int gbPrice, String image, int route, String description, String uid) {
        num++;
        ContentValues contentValues = new ContentValues();
        contentValues.put("NUM", num);
        //카테고리는 일단 not 문자열 삽입
        contentValues.put("CATEGORY", "not");
        contentValues.put("TITLE", title);
        contentValues.put("DDATE", ddate);
        contentValues.put("EDATE", edate);
        contentValues.put("EGROUP", egroup);
        contentValues.put("MGROUP", mgroup);
        contentValues.put("ORGPRICE", orgPrice);
        contentValues.put("GBPRICE", gbPrice);
        contentValues.put("IMAGE", image);
        contentValues.put("ROUTE", route);
        contentValues.put("DESCRIPTION", description);
        contentValues.put("UID", uid);
        long id = database.insert("BOARD", null, contentValues);
        if(id < 0) return -1;
        return 0; // 잘 추가되었다면 0을, 중간에 오류가 발생하면 -1을 반환
    }

    // User의 정보를 지운다.
    public int delUserInfo(String id, String pw) {
        return 0; // 잘 삭제되었다면 0을, 중간에 오류가 발생하면 -1을 반환
    }

    // 공구에 참여한 정보를 지운다.
    // 해당 상품을 올린 공구 개시자에게 알림을 준다.
    public int delJoinInfo(String userID, String ProductID) {
        return 0; // 잘 삭제되었다면 0을, 중간에 오류가 발생하면 -1을 반환
    }

    // 상품을 삭제하는 코드
    // 생성되어 있던 참여 정보도 함께 지워져야 하며, 참여한 인원에게 알림을 준다.
    public int delProductInfo(int productID) {
        return 0; // 잘 삭제되었다면 0을, 중간에 오류가 발생하면 -1을 반환
    }

    // 해당 id의 User에게 알람을 보낸다.
    public int sendAlarmToUser(String id) {
        return 0; // 잘 삭제되었다면 0을, 중간에 오류가 발생하면 -1을 반환
    }

    // 해당 상품 공구에 참여한 User의 목록을 반환
    // 이를 통해 현재 해당 상품의 공구에 참여한 인원 수를 알 수 있음
    public ArrayList<String> joinPeopleList(int productID) {
        ArrayList<String> peopleArray = new ArrayList<String>();

        return peopleArray;
    }

    // 내가 참여한 공구 목록을 반환
    public ArrayList<Integer> myJoinList(String UserID) {
        ArrayList<Integer> joinArray = new ArrayList<Integer>();

        return joinArray;
    }

    // 내가 올린 상품목록을 반환
    public ArrayList<Integer> myProductList(String userID) {
        ArrayList<Integer> enterArray = new ArrayList<Integer>();

        return enterArray;
    }

    // 마지막 상품 ID를 가져오는 함수, 데이터베이스 생성 시 한번만 부름
    private int getLastProductID()
    {
        String countQuery = "SELECT * FROM BOARD";
        Cursor cursor = database.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        num = cnt;
        return num;
    }

    // 상품의 공구 제목을 가져옴
    public String getSQLTitle(int id) {
        return null;
    }

    // 상품의 마감시간을 가져옴
    public String getSQLTime(int id) {
        return null;
    }

    // 상품의 참여인원수를 가져옴
    public int getSQLPeople(int id) {
        return 0;
    }

    // 상품의 공구 가격을 가져옴
    public int getSQLPrice(int id) {
        return 0;
    }

    // 상품의 분배 무게를 가져옴
    public float getSQLWeight(int id) {
        return 0.0f;
    }

    // 상품의 상세 설명을 가져옴
    public String getSQLExplane(int id) {
        return null;
    }

    // 상품의 직접 분배 여부를 가져옴
    public int getSQLisDirect(int id) {
        return 0;
    }

    // 상품의 분배 날짜를 가져옴
    public String getSQLDate(int id) {
        return null;
    }

    // 상품의 분배 위치를 가져옴
    public String getSQLPlace(int id) {
        return null;
    }

    // 상품의 사진을 가져옴
    public Drawable getSQLImage(int id) {
        return null;
    }

    // 상품을 업로드한 유저의 ID를 반환한다.
    public String getSQLUserID(int id) {
        return null;
    }

    // 서버에 데이터베이스를 업데이트함
    public void updateToServer() {

    }
}
