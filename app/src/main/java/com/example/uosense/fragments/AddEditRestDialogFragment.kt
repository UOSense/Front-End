package com.example.uosense.fragments

import com.example.uosense.network.RetrofitInstance
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.uosense.AdminActivity
import com.example.uosense.R
import com.example.uosense.models.Restaurant
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddEditRestDialogFragment : DialogFragment() {

    private var restaurant: Restaurant? = null
    private var call: Call<Void>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            restaurant = it.getParcelable(ARG_RESTAURANT)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.activity_add_edit_rest_dialog_fragment, null)
        builder.setView(view)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etLocation = view.findViewById<EditText>(R.id.etLocation)
        val etRating = view.findViewById<EditText>(R.id.etRating)

        // 수정인 경우 기존 데이터 채우기
        restaurant?.let {
            etName.setText(it.name)
            etLocation.setText(it.location)
            etRating.setText(it.rating.toString())
        }

        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val updatedRestaurant = Restaurant(
                id = restaurant?.id ?: 0,
                name = etName.text.toString(),
                location = etLocation.text.toString(),
                rating = etRating.text.toString().toFloatOrNull() ?: 0f
            )

            // 입력값 검증
            if (updatedRestaurant.name.isBlank() || updatedRestaurant.location.isBlank() || updatedRestaurant.rating <= 0) {
                Toast.makeText(requireContext(), "모든 필드를 올바르게 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (restaurant == null) addRestaurant(updatedRestaurant) // 새 항목 추가
            else updateRestaurant(updatedRestaurant) // 기존 항목 수정

            dismiss()
        }

        return builder.create()
    }

    private fun addRestaurant(restaurant: Restaurant) {
        RetrofitInstance.api.addRestaurant(restaurant).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Restaurant added successfully", Toast.LENGTH_SHORT).show()
                    (activity as? AdminActivity)?.fetchRestaurantList() // 목록 업데이트
                } else {
                    Toast.makeText(requireContext(), "Failed to add restaurant", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateRestaurant(restaurant: Restaurant) {
        RetrofitInstance.api.updateRestaurant(restaurant.id, restaurant).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // Fragment가 Context에 연결되어 있지 않으면 실행하지 않음
                if (!isAdded || view == null) return

                if (response.isSuccessful) {
                    // Context 대신 view?.context 사용
                    Toast.makeText(view?.context, "Restaurant added successfully!", Toast.LENGTH_SHORT).show()
                    (activity as? AdminActivity)?.fetchRestaurantList()
                } else {
                    Toast.makeText(view?.context, "Failed to add restaurant: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<Void>, t: Throwable) {
                if (!isAdded || view == null) return

                Toast.makeText(view?.context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        call?.cancel() // Fragment가 파괴될 때 Retrofit 요청 취소
    }

    companion object {
        private const val ARG_RESTAURANT = "restaurant"

        fun newInstance(restaurant: Restaurant?): AddEditRestDialogFragment {
            val fragment = AddEditRestDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_RESTAURANT, restaurant)
            fragment.arguments = args
            return fragment
        }
    }
}
