package com.yyh;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.ldoublem.loadingviewlib.view.LVGearsTwo;
import com.yyh.Entity.Statics;
import com.yyh.Utils.NormalUtils;
import com.yyh.Utils.WordIO;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class templateFragment extends Fragment implements View.OnClickListener{

    private RelativeLayout replaceLayout;
    private Button addTemplate;
    private Button saveTemplate;
    private Spinner templateSpinner;
    private NiceSpinner titleSpinner;
    private NiceSpinner answerSpinner;
    private Map<String, String> titleMap;
    private ArrayList<String> title = new ArrayList<>();
    private ArrayList<String> titlePosition = new ArrayList<>();
    private Map<String, String> replaceMap = new HashMap<>();
    private String selectedTitle;
    private String templateUri;

    public templateFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化Android下POI正常使用所需的参数
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addTemplate = view.findViewById(R.id.addTemplate);
        saveTemplate = view.findViewById(R.id.saveTemplate);
        titleSpinner = view.findViewById(R.id.titleSpinner);
        answerSpinner = view.findViewById(R.id.answerSpinner);
        templateSpinner = view.findViewById(R.id.templateSpinner);
        replaceLayout = view.findViewById(R.id.replaceLayout);
        addTemplate.setOnClickListener(this);
        saveTemplate.setOnClickListener(this);

        setTemplateList();  //获取目录下模板的个数
        titleSpinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                //当选项被选中时，设定为本地数值
                selectedTitle = (String)parent.getItemAtPosition(position);
            }
        });
        answerSpinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                //当对应项选中时，获取指定选项，往map中增加（替换）一项
                String answer = (String)parent.getItemAtPosition(position);
                replaceMap.put(selectedTitle, answer);
            }
        });
        templateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                replaceLayout.setVisibility(View.VISIBLE);
                String templateName = (String)parent.getItemAtPosition(position);
                templateUri = Statics.PATH_TEMPLATES + templateName;
                setTitleList(templateUri);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_template, container, false);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 666){
            if(resultCode == RESULT_OK){
                //获取被选中文件的uri
                Uri uri = data.getData();
                //将模板复制到模板文件夹里
                //默认添加完模板后，开始编辑该模板（可能不行）
                Log.d("this way", uri.toString());
                Log.d("this way", NormalUtils.getPath(getContext(), uri));
                templateUri = copyTemplate(uri);
                setTitleList(templateUri);
                setTemplateList();
                SpinnerAdapter adapter = templateSpinner.getAdapter();
                for(int i = 0; i < adapter.getCount(); i++){
                    if(NormalUtils.getFileNameWithSuffix(templateUri).equals(adapter.getItem(i))){
                        templateSpinner.setSelection(i, true);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addTemplate:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 666);
                break;
            case R.id.saveTemplate:
                handleSaveTemplate();
            default:
                break;
        }
    }

    /**
     * 将模板复制到模板文件夹里
     * @param uri 文件的路径
     * @return outputUri 返回复制后的路径
     */
    private String copyTemplate(Uri uri){
        if(uri != null){
            File file = new File(Statics.PATH_TEMPLATES);
            if(!file.exists()){
                file.mkdirs();
            }
            String name = NormalUtils.getFileNameWithSuffix(NormalUtils.getPath(getContext(), uri));
            String outputUri = file.getAbsolutePath() + "/" +name;
            //将模板复制到模板文件夹里
            Log.d("this way", String.valueOf(NormalUtils.copyFile(NormalUtils.getPath(getContext(), uri),
                    outputUri)));
            return outputUri;
        }else{
            return null;
        }
    }

    /**
     * 保存替换后的Word文档（将废弃）
     * TODO：现在replaceMap还没做好
     * 逻辑是这样：dataMap存【“文档字段”——“数据库字段”】，replaceMap存【“文档字段”——“替换数据”】
     * 这边只进行dataMap的设置，设置完即存储好这个Map，不对文件进行操作
     * 当需要保存文件时，拿到dataMap，通过对应关系，找到数据库字段再找到需要替换上去的数据，然后建立replaceMap，调用changeWord()方法
     */
    private void handleSaveTemplate(){
        //保存时，保存为一个新的文件，然后覆盖原文件
        WordIO.changeWord(templateUri, "/storage/emulated/0/msc/templates/demo.docx", replaceMap);
    }

    /**
     * 获取到模板文件夹下的文件，并设置好列表
     */
    private void setTemplateList(){
        File file = new File(Statics.PATH_TEMPLATES);
        File[] files=file.listFiles();
        if (files == null){Log.e("error","空目录");return;}
        List<String> s = new ArrayList<>();
        for (File file1 : files) {
            s.add(NormalUtils.getFileNameWithSuffix(file1.getAbsolutePath()));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, s);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        templateSpinner.setAdapter(adapter);
    }

    private void setTitleList(String uri){
        titleMap = WordIO.getTitle(uri);
        for (String key : titleMap.keySet()) {
            title.add(key);
            titlePosition.add(titleMap.get(key));
        }
        titleSpinner.attachDataSource(title);
        //TODO:得到数据库字段后改成数据库字段
        answerSpinner.attachDataSource(title);
    }
}
