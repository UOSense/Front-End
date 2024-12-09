package com.example.uosense.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uosense.R
import com.example.uosense.databinding.ItemReviewBinding
import com.example.uosense.models.ReviewResponse

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val reviewList = mutableListOf<ReviewResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviewList[position])
    }

    override fun getItemCount(): Int = reviewList.size

    fun submitList(reviews: List<ReviewResponse>) {
        reviewList.clear()
        reviewList.addAll(reviews)
        notifyDataSetChanged()
    }

    class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: ReviewResponse) {
            binding.reviewerName.text = review.reviewerName
            binding.reviewContent.text = review.body
            binding.reviewRatingBar.rating = review.rating.toFloat()
            binding.likeCount.text = review.likeCount.toString()

            // 리뷰 이벤트 참여 여부
            binding.eventParticipation.visibility =
                if (review.isReviewEventCheck) View.VISIBLE else View.GONE

            // 리뷰 이미지 로드
            review.imageUrls?.let { imageUrls ->
                if (imageUrls.isNotEmpty()) {
                    Glide.with(binding.root.context)
                        .load(imageUrls[0])
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(binding.reviewImage1)

                    if (imageUrls.size > 1) {
                        Glide.with(binding.root.context)
                            .load(imageUrls[1])
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(binding.reviewImage2)
                    }
                }
            }

            // 신고 버튼 클릭 동작
            binding.reportButton.setOnClickListener {
                Toast.makeText(binding.root.context, "리뷰 신고 기능 실행", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
