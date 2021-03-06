package com.jon.snow;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.IOError;
import java.io.IOException;
import java.util.List;

import Adapters.MyGlideEngine;
import Entity.User;
import Utils.HttpUtil;
import  Utils.MD5Utils;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CHOOSE = 23;
    private static final int PERMISSIONS_REQUEST_CODE = 25;
    private String iv_userface_url;
    private ImageView iv_userface;
    private TextView tv_main_title;//标题
    private TextView tv_back;//返回按钮
    private Button btn_register;//注册按钮
    //用户名，密码，再次输入的密码的控件
    private EditText et_user_name,et_psw,et_psw_again;
    //用户名，密码，再次输入的密码的控件的获取值
    private String userName,psw,pswAgain;
    //标题布局
    private RelativeLayout rl_title_bar;

    private HttpUtil httpUtil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置页面布局 ,注册界面
        setContentView(R.layout.activity_register);
        //设置此界面为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        hasPermission();
        init();
    }

    private void init() {
        //从main_title_bar.xml 页面布局中获取对应的UI控件
        iv_userface = findViewById(R.id.reg_userface);
        tv_main_title=findViewById(R.id.tv_main_title);
        tv_main_title.setText("注册");
        tv_back=findViewById(R.id.tv_back);
        //布局根元素
        rl_title_bar=findViewById(R.id.title_bar);
        rl_title_bar.setBackgroundColor(Color.TRANSPARENT);
        //从activity_register.xml 页面中获取对应的UI控件
        btn_register=findViewById(R.id.btn_register);
        et_user_name=findViewById(R.id.et_user_name);
        et_psw=findViewById(R.id.et_psw);
        et_psw_again=findViewById(R.id.et_psw_again);
        iv_userface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Matisse.from(RegisterActivity.this)
                        .choose(MimeType.ofImage())//图片类型
                        .countable(true)//true:选中后显示数字;false:选中后显示对号
                        .maxSelectable(9)//可选的最大数
                        .capture(true)//选择照片时，是否显示拍照
                        .captureStrategy(new CaptureStrategy(true, "com.jon.snow.fileprovider"))//参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                        .imageEngine(new MyGlideEngine())//图片加载引擎
                        .forResult(REQUEST_CODE_CHOOSE);//

//                Matisse
//                        .from(RegisterActivity.this)
//                        //选择图片
//                        .choose(MimeType.ofImage())
//                        //是否只显示选择的类型的缩略图，就不会把所有图片视频都放在一起，而是需要什么展示什么
//                        .showSingleMediaType(true)
//                        //这两行要连用 是否在选择图片中展示照相 和适配安卓7.0 FileProvider
//                        .capture(true)
//                        .captureStrategy(new CaptureStrategy(true,"com.jon.snow.fileprovider"))
//                        //有序选择图片 123456...
//                        .countable(true)
//                        //最大选择数量为1
//                        .maxSelectable(1)
//                        //选择方向
//                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
//                        //界面中缩略图的质量
//                        .thumbnailScale(0.8f)
//                        //蓝色主题
//                        .theme(R.style.Matisse_Zhihu)
//                        //Glide加载方式
//                        .imageEngine(new GlideEngine())
//                        //请求码
//                        .forResult(REQUEST_CODE_CHOOSE);
            }
        });
        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //返回键
                RegisterActivity.this.finish();
            }
        });
        //注册按钮
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取输入在相应控件中的字符串
                getEditString();
                //判断输入框内容
                if(TextUtils.isEmpty(userName)){
                    Toast.makeText(RegisterActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(psw)){
                    Toast.makeText(RegisterActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(pswAgain)){
                    Toast.makeText(RegisterActivity.this, "请再次输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }else if(!psw.equals(pswAgain)){
                    Toast.makeText(RegisterActivity.this, "输入两次的密码不一样", Toast.LENGTH_SHORT).show();
                    return;
                    /**
                     *从SharedPreferences中读取输入的用户名，判断SharedPreferences中是否有此用户名
                     */
                }else if(isExistUserName(userName)){
                    Toast.makeText(RegisterActivity.this, "此账户名已经存在", Toast.LENGTH_SHORT).show();
                    return;
                }else if(iv_userface.getDrawable().getCurrent().getConstantState()==getResources().getDrawable(R.drawable.ic_default_icon).getConstantState()) {
                    Toast.makeText(RegisterActivity.this, "请设置头像", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    //Post到服务器，传入数据库
                    RequestBody requestBody = new FormBody.Builder()
                            .add("name",userName)
                            .add("password",psw)
                            .build();
                    User user = new User();
                    user.setDisplayName(userName);
                    user.setPassword(psw);
                    user.setAvatar(iv_userface_url);
                    Gson gson = new Gson();
                    String jsonObject = gson.toJson(user);
                    Log.d("UserJson","注册"+jsonObject);
                    new Thread()
                    {
                        @Override
                        public void run()
                        {
                            super.run();
                            try
                            {
                                System.setProperty("https.protocols", "TLSv1.2,TLSv1.1,SSLv3");
                                httpUtil.OkHttpRequestPost("http://"+R.string.ip+":"+R.string.port+"/users",jsonObject);
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                               Log.d("UserJSON出错","UserJson错");
                            }
                        }
                    }.start();

                    //把账号、密码和账号标识保存到sp里面
                    /**
                     * 保存账号和密码到SharedPreferences中
                     */
                    saveRegisterInfo(userName, psw);
                    //注册成功后把账号传递到LoginActivity.java中
                    // 返回值到loginActivity显示
                    Intent data = new Intent();
                    data.putExtra("userName", userName);
                    setResult(RESULT_OK, data);
                    //RESULT_OK为Activity系统常量，状态码为-1，
                    // 表示此页面下的内容操作成功将data返回到上一页面，如果是用back返回过去的则不存在用setResult传递data值
                    RegisterActivity.this.finish();
                }
            }
        });
    }
    /**
     * 获取控件中的字符串
     */
    private void getEditString(){
        userName=et_user_name.getText().toString().trim();
        psw=et_psw.getText().toString().trim();
        pswAgain=et_psw_again.getText().toString().trim();
    }
    /**
     * 从SharedPreferences中读取输入的用户名，判断SharedPreferences中是否有此用户名
     */
    private boolean isExistUserName(String userName){
        boolean has_userName=false;
        //mode_private SharedPreferences sp = getSharedPreferences( );
        // "loginInfo", MODE_PRIVATE
        SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
        //获取密码
        String spPsw=sp.getString(userName, "");//传入用户名获取密码
        //如果密码不为空则确实保存过这个用户名
        if(!TextUtils.isEmpty(spPsw)) {
            has_userName=true;
        }
        return has_userName;
    }
    /**
     * 保存账号和密码到SharedPreferences中SharedPreferences
     */
    private void saveRegisterInfo(String userName,String psw){
        String md5Psw = MD5Utils.md5(psw);//把密码用MD5加密
        //loginInfo表示文件名, mode_private SharedPreferences sp = getSharedPreferences( );
        SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
        //获取编辑器， SharedPreferences.Editor  editor -> sp.edit();
        SharedPreferences.Editor editor=sp.edit();
        //以用户名为key，密码为value保存在SharedPreferences中
        //key,value,如键值对，editor.putString(用户名，密码）;
        editor.putString(userName, md5Psw);
        //提交修改 editor.commit();
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<Uri> result = Matisse.obtainResult(data);
            iv_userface.setImageURI(result.get(0));
            iv_userface_url = result.get(0).toString();
        }
    }

    /**
     * 获取权限
     * @return
     */
    private boolean hasPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "开启拍照权限成功", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
