package com.example.uosense.adapters

import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uosense.R
import com.example.uosense.models.ReviewItem
import android.view.ViewGroup
import android.view.LayoutInflater

class ReviewAdapter(private val reviews: List<ReviewItem>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val reviewerName: TextView = itemView.findViewById(R.id.reviewerName)
        val reviewRatingBar: RatingBar = itemView.findViewById(R.id.reviewRatingBar)
        val reviewContent: TextView = itemView.findViewById(R.id.reviewContent)
        val eventParticipation: TextView = itemView.findViewById(R.id.eventParticipation)
        val writeDate: TextView = itemView.findViewById(R.id.writeDate)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val reviewImage1: ImageView = itemView.findViewById(R.id.reviewImage1)
        val reviewImage2: ImageView = itemView.findViewById(R.id.reviewImage2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        // 1. 닉네임
        holder.reviewerName.text = review.nickname

        // 2. 별점
        holder.reviewRatingBar.rating = review.rating.toFloat()

        // 3. 리뷰 내용
        holder.reviewContent.text = review.body

        // 4. 이벤트 참여 여부
        holder.eventParticipation.text =
            if (review.reviewEventCheck) "리뷰 이벤트 참여" else "리뷰 이벤트 미참여"

        // 5. 좋아요 수
        holder.likeCount.text = review.likeCount.toString()

        // 6. 작성 날짜
        holder.writeDate.findViewById<TextView>(R.id.writeDate).text = review.getFormattedDate()

        // 7. 프로필 이미지 로드 (Glide 사용)
        Glide.with(holder.itemView.context)
            .load(review.userImage ?: R.drawable.ic_user)
             // 기본 이미지
            .into(holder.profileImage)

        // 8. 리뷰 이미지 로드
        val images = review.imageUrls ?: emptyList()
        holder.reviewImage1.visibility = if (images.isNotEmpty()) View.VISIBLE else View.GONE
        holder.reviewImage2.visibility = if (images.size > 1) View.VISIBLE else View.GONE

        if (images.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(images[0])
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.reviewImage1)
        }
        if (images.size > 1) {
            Glide.with(holder.itemView.context)
                .load(images[1])
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.reviewImage2)
        }

    }

    override fun getItemCount(): Int = reviews.size
}

