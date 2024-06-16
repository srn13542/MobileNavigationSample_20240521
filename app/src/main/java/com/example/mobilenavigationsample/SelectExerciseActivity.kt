package com.example.mobilenavigationsample

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SelectExerciseActivity : AppCompatActivity() {

    private var findCheckExerciseButton: Boolean = false  //버튼이 클릭되었는지를 확인
    private lateinit var selectedExerciseType: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_exercise)

        title = "운동 선택하기"

//        val locationName = intent.getStringExtra("location_name") ?: ""
//        val latitude = intent.getDoubleExtra("latitude", 0.0)
//        val longitude = intent.getDoubleExtra("longitude", 0.0)
        selectedExerciseType = intent.getStringExtra("Exercise") ?: ""

        // UI 요소 초기화
        val listView: ListView = findViewById(R.id.selectExerciseListView)

        // 운동 종목 데이터 설정
        val exerciseList = selectedExerciseType.split(", ") // 운동 종목이 쉼표와 공백으로 구분되어 있을 경우 분리
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, exerciseList)
        listView.adapter = adapter


    }


}