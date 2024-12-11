package com.example.uosense

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uosense.adapters.ReportAdapter
import com.example.uosense.data.ReportRepository
import com.example.uosense.databinding.ActivityReportBinding

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView 설정
        val reportList = ReportRepository.getReports().toMutableList() // 타입 변환
        binding.reportRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reportRecyclerView.adapter = ReportAdapter(reportList)
    }
}
