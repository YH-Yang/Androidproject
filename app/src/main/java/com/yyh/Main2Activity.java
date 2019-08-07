package com.yyh;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.yyh.db.Organization;


public class Main2Activity extends BaseActivity implements View.OnClickListener {
//    private Button set;
//    private Button record;
//    private Button proofread;
//    private Button search;
//    private Button paint;
//    private Button manage;
//    private Button system;
    private RadioGroup bottomView;
    private RadioButton set, record, proofread, search, print, manage, setting;
    private ImageButton account;
    private TextView account_infor;

   public static boolean isable=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //对上部栏目的组件进行初始化
        account=(ImageButton)findViewById(R.id.account);
        account_infor=(TextView)findViewById(R.id.account_infor);

        //得到传入的账号信息
        Intent intent=getIntent();
        String ID=intent.getStringExtra("trueID");
        account_infor.setText(ID);
        bottomView = findViewById(R.id.bottom_view);
        set = findViewById(R.id.navigation_set);
        record = findViewById(R.id.navigation_record);
        proofread = findViewById(R.id.navigation_proofread);
        search = findViewById(R.id.navigation_search);
        print = findViewById(R.id.navigation_print);
        manage = findViewById(R.id.navigation_manage);
        setting = findViewById(R.id.navigation_setting);
        bottomView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.navigation_set:
                        replaceFragment(new Setting_fragement());
                        break;

                    case R.id.navigation_record:
                        start_record();
                        break;

                    case R.id.navigation_proofread:
                        replaceFragment(new Setting_fragement());
                        break;

                    case R.id.navigation_search:
                        replaceFragment(new Setting_fragement());
                        break;

                    case R.id.navigation_print:
                        replaceFragment(new Setting_fragement());
                        break;

                    case R.id.navigation_manage:
                        replaceFragment(new templateFragment());
                        break;

                    case R.id.navigation_setting:
                        replaceFragment(new Setting_fragement());
                        break;

                    default:
                        break;
                }
                setTabState();
            }
        });
        //首先让fragement显示的界面
        replaceFragment(new Setting_fragement());
        set.setChecked(true);
    }

    public void onClick(View v) {
        switch (v.getId()){

            default:
                break;

        }
    }

    public void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.replace(R.id.right_layout,fragment);
        transaction.commit();
    }

    public void start_record(){
        //首先判断是否能进行录屏
        if(!isable){
            Toast.makeText(this,"请先进行笔录设置",Toast.LENGTH_LONG).show();

        }else{
            replaceFragment(new collecting_fragement());
        }
    }

    private void setState(RadioButton b){
        if(b.isChecked()){
            b.setTextColor(ContextCompat.getColor(this, R.color.colorRadioButtonD));
        }else{
            b.setTextColor(ContextCompat.getColor(this, R.color.colorRadioButtonU));
        }
    }

    private void setTabState(){
        setState(set);
        setState(record);
        setState(proofread);
        setState(search);
        setState(print);
        setState(manage);
        setState(setting);
    }
}
