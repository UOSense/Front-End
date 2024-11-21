package com.example.uosense

import com.example.uosense.adapters.RestaurantAdapter
import com.example.uosense.network.RetrofitInstance
import com.example.uosense.fragments.AddEditRestDialogFragment

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.models.Restaurant
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody



class AdminActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerView)
        adapter = RestaurantAdapter(
            onEditClick = { restaurant -> showAddEditDialog(restaurant) },
            onDeleteClick = { restaurant -> deleteRestaurant(restaurant) }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // FloatingActionButton 클릭: 식당 추가
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddEditDialog(null) // null로 전달하면 새로 추가
        }

        // 서버에서 식당 목록 가져오기
        fetchRestaurantList()
    }

    fun fetchRestaurantList() {
        RetrofitInstance.api.getRestaurants().enqueue(object : Callback<List<Restaurant>> {
            override fun onResponse(
                call: Call<List<Restaurant>>,
                response: Response<List<Restaurant>>
            ) {
                if (response.isSuccessful) {
                    val restaurants = response.body() ?: emptyList()
                    adapter.submitList(restaurants) // 데이터를 RecyclerView에 갱신
                } else {
                    Toast.makeText(
                        this@AdminActivity,
                        "Failed to load data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


            override fun onFailure(call: Call<List<Restaurant>>, t: Throwable) {
                Toast.makeText(this@AdminActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }


    private fun showAddEditDialog(restaurant: Restaurant?) {
        val dialog = AddEditRestDialogFragment.newInstance(restaurant)
        dialog.show(supportFragmentManager, "AddEditDialog")
    }

    private fun deleteRestaurant(restaurant: Restaurant) {
        RetrofitInstance.api.deleteRestaurant(restaurant.id)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@AdminActivity,
                            "Deleted Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchRestaurantList()
                    } else {
                        Toast.makeText(
                            this@AdminActivity,
                            "Failed to delete",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        this@AdminActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

}


