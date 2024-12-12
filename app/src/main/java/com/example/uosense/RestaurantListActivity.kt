package com.example.uosense

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.AppUtils.showToast
import com.example.uosense.adapters.RestaurantListAdapter
import com.example.uosense.databinding.ActivityRestaurantListBinding
import com.example.uosense.models.RestaurantListResponse
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RestaurantListActivity : AppCompatActivity() {

    private lateinit var restaurantList: MutableList<RestaurantListResponse>
    private lateinit var adapter: RestaurantListAdapter
    private lateinit var binding: ActivityRestaurantListBinding
    private var selectedFilter = "DEFAULT"
    //검색을 위해서 새로운 변수
    private var selectedDoorType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 바인딩 초기화
        binding = ActivityRestaurantListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        restaurantList = intent.getParcelableArrayListExtra("restaurantList") ?: mutableListOf()

        setupRecyclerView()
        setupFilterButtons()
        setupSortButton()
        checkIfListIsEmpty()
        customizeSearchView()

        binding.ivMap.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    // RecyclerView 설정 함수
    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RestaurantListAdapter(restaurantList) { navigateToDetailActivity(it) }
        binding.recyclerView.adapter = adapter
    }
    // 리스트가 비어있는지 체크하고, 빈 경우에는 "결과 없음" 텍스트를 보여줌
    private fun checkIfListIsEmpty() {
        if (restaurantList.isEmpty()) {
            binding.recyclerView.visibility = RecyclerView.GONE
            binding.tvNoResults.visibility = TextView.VISIBLE
        } else {
            binding.recyclerView.visibility = RecyclerView.VISIBLE
            binding.tvNoResults.visibility = TextView.GONE
        }
    }
    // 필터 버튼에 대한 클릭 리스너 설정
    private fun setupFilterButtons() {
        binding.apply {
            doorTypeButton1.setOnClickListener { fetchFilteredRestaurants("FRONT") }
            doorTypeButton2.setOnClickListener { fetchFilteredRestaurants("SIDE") }
            doorTypeButton3.setOnClickListener { fetchFilteredRestaurants("BACK") }
            doorTypeButton4.setOnClickListener { fetchFilteredRestaurants("SOUTH") }
        }
    }
    // 필터된 레스토랑 데이터를 서버에서 받아오는 함수
    private fun fetchFilteredRestaurants(doorType: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantList(doorType, selectedFilter)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        adapter.updateList(response.body()!!)
                    } else {
                        adapter.updateList(emptyList())
                    }
                    checkIfListIsEmpty()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RestaurantListActivity, "서버 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // 정렬 버튼 클릭 시 옵션을 보여주는 함수
    private fun setupSortButton() {
        binding.btnFilter.setOnClickListener {
            showSortOptions()
        }
    }
    // 정렬 기준을 선택하는 다이얼로그를 띄우는 함수
    private fun showSortOptions() {
        val sortOptions = arrayOf("리뷰 많은 순", "즐겨찾기 많은 순", "평점 순", "가격 낮은 순", "거리 가까운 순")
        val apiValues = arrayOf("REVIEW", "BOOKMARK", "RATING", "PRICE", "DISTANCE")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("정렬 기준 선택")
            .setItems(sortOptions) { _, which ->
                selectedFilter = apiValues[which]
                binding.tvSelectedSort.text = sortOptions[which]
                fetchSortedRestaurants()
            }
            .setNegativeButton("취소", null)
            .show()
    }
    // 정렬된 레스토랑 데이터를 서버에서 받아오는 함수
    private fun fetchSortedRestaurants() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.sortRestaurants("", selectedFilter)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        adapter.updateList(response.body()!!)
                    } else {
                        adapter.updateList(emptyList())
                    }
                    checkIfListIsEmpty()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RestaurantListActivity, "정렬 중 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // 레스토랑 상세보기 화면으로 이동하는 함수
    private fun navigateToDetailActivity(restaurant: RestaurantListResponse) {
        val intent = Intent(this, RestaurantDetailActivity::class.java).apply {
            putExtra("restaurantId", restaurant.id)
        }
        startActivity(intent)
    }
    // RestaurantListAdapter에서 리스트를 갱신하는 함수
    fun RestaurantListAdapter.updateList(newList: List<RestaurantListResponse>) {
        restaurantList.clear()
        restaurantList.addAll(newList)
        notifyDataSetChanged()
    }
    // 검색 기능을 위한 설정 함수
    private fun setupSearch() {
        binding.svSearch.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val trimmedQuery = query?.trim()
                if (!trimmedQuery.isNullOrBlank()) {
                    searchRestaurants(trimmedQuery)
                } else {
                    showToast(this@RestaurantListActivity, "검색어를 입력해주세요.")
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }
    // 검색어를 기반으로 레스토랑을 검색하는 함수
    private fun searchRestaurants(keyword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.searchRestaurants(
                    keyword = keyword,
                    doorType = selectedDoorType
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                        navigateToRestaurantList(response.body()!!)
                    } else {

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {

                }
            }
        }
    }

    // 전체 목록 보기로 이동 (정문 기본 선택)
    private fun navigateToRestaurantList(restaurantList: List<RestaurantListResponse>) {
        val intent = Intent(this, RestaurantListActivity::class.java).apply {
            putParcelableArrayListExtra("restaurantList", ArrayList(restaurantList))
            putExtra("defaultDoorType", "정문") // 정문 기본 선택
        }
        startActivity(intent)
    }

    /**
     * 검색창 글씨체는 따로 함수정의해서 색상 변경
     * xml 파일 내에서 수정 X
     */
    private fun customizeSearchView() {
        try {
            // SearchView의 AutoCompleteTextView 가져오기
            val searchAutoComplete = binding.svSearch.javaClass
                .getDeclaredField("mSearchSrcTextView")
                .apply { isAccessible = true }
                .get(binding.svSearch) as? android.widget.AutoCompleteTextView

            searchAutoComplete?.apply {
                setHintTextColor(resources.getColor(R.color.black, null))  // 힌트 텍스트 색상 설정
                setTextColor(resources.getColor(R.color.black, null))     // 입력 텍스트 색상 설정
                textSize = 16f                                            // 텍스트 크기 설정
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
