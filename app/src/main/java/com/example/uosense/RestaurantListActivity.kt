package com.example.uosense

import android.content.Intent
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

        val adapter = RestaurantAdapter { selectedRestaurant ->
            val intent = Intent(this, RestaurantDetailActivity::class.java).apply {
                putExtra("restaurantId", selectedRestaurant.id)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        viewModel.restaurantList.observe(this) { restaurants ->
            println("Observed Restaurants: $restaurants")
            adapter.submitList(restaurants)
            adapter.notifyDataSetChanged()
        }

        // Fetch mock data
        viewModel.fetchMockRestaurants()
    }
}




