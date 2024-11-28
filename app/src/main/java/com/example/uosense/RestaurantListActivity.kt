package com.example.uosense

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.R
import com.example.uosense.adapters.RestaurantAdapter
import com.example.uosense.viewmodel.RestaurantViewModel

class RestaurantListActivity : AppCompatActivity() {

    private val viewModel: RestaurantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.restaurants.observe(this, Observer { restaurants ->
            recyclerView.adapter = RestaurantAdapter(restaurants)
        })

        // Mock 데이터 로드 (또는 API 호출)
        viewModel.fetchMockRestaurants()
    }
}


