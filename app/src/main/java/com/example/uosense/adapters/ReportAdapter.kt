package com.example.uosense.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uosense.R
import com.example.uosense.models.Report

class ReportAdapter(
    private val reportList: MutableList<Report>
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.userProfileImage)
        val userName: TextView = view.findViewById(R.id.userName)
        val userRating: TextView = view.findViewById(R.id.userRating)
        val title: TextView = view.findViewById(R.id.reportTitle)
        val content: TextView = view.findViewById(R.id.reportContent)
        val deleteButton: Button = view.findViewById(R.id.deleteReportBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]

        // 사용자 정보 설정
        holder.userName.text = report.userName
        holder.userRating.text = "별점: ${report.rating}"
        holder.title.text = report.title
        holder.content.text = report.content

        // Glide로 프로필 이미지 로딩
        Glide.with(holder.itemView.context)
            .load(report.profileImageUri?.let { Uri.parse(it) })
            .placeholder(R.drawable.ic_user)
            .error(R.drawable.ic_user)
            .into(holder.profileImage)

        // 삭제 버튼 클릭 이벤트
        holder.deleteButton.setOnClickListener {
            removeReport(position, holder.itemView)
        }
    }

    override fun getItemCount() = reportList.size

    // 리뷰 삭제 로직
    private fun removeReport(position: Int, view: View) {
        val deletedReport = reportList[position]
        reportList.removeAt(position)
        notifyItemRemoved(position)

        // 메시지 알림
        Toast.makeText(
            view.context,
            "리뷰 '${deletedReport.title}'가 삭제되었습니다.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
