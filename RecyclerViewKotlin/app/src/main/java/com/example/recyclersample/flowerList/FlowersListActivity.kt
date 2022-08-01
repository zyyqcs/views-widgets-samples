/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.recyclersample.flowerList

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recyclersample.addFlower.AddFlowerActivity
import com.example.recyclersample.flowerDetail.FlowerDetailActivity
import com.example.recyclersample.R
import com.example.recyclersample.addFlower.FLOWER_DESCRIPTION
import com.example.recyclersample.addFlower.FLOWER_NAME
import com.example.recyclersample.data.Flower

const val FLOWER_ID = "flower id"

class FlowersListActivity : AppCompatActivity() {
    private val newFlowerActivityRequestCode = 1
    private val flowersListViewModel by viewModels<FlowersListViewModel> {
        FlowersListViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* Instantiates headerAdapter and flowersAdapter. Both adapters are added to concatAdapter.
        which displays the contents sequentially */
        val headerAdapter = HeaderAdapter()
        val flowersAdapter = FlowersAdapter { flower -> adapterOnClick(flower) }
        val concatAdapter = ConcatAdapter(headerAdapter, flowersAdapter)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        //为recyclerView设置布局管理器，这里使用网格布局管理器。设置最大列数为3
        //使用spanSizeLookup设置每个item占的列数
        recyclerView.layoutManager = GridLayoutManager(this,3).also{it.spanSizeLookup=object : GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int = if(position==0)3 else 1
        }}

        //使用itemDecoration为每个item添加不同的装饰效果，这里是不同的偏移量
        val itemDecoration: RecyclerView.ItemDecoration = object :RecyclerView.ItemDecoration(){
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
               // 获取每个item的position
                var itemPosition = (view.layoutParams as RecyclerView.LayoutParams) .viewLayoutPosition
                //从dimens文件中获取dp对应的像素值
                val parentWidth = resources.getDimension(R.dimen.recycler_width)-resources.getDimension(R.dimen.recycler_margin)*2
                val viewWidth = resources.getDimension(R.dimen.item_width)
                Log.d("zht", "getItemOffsets: pw$parentWidth,vw$viewWidth")
                //计算每列的偏移量
                val edgeOffset = resources.getDimension(R.dimen.page_margin)-resources.getDimension(R.dimen.recycler_margin)
                val leftPadding = (parentWidth/3-viewWidth)-edgeOffset
                var rightPadding = leftPadding
                var midPadding = (parentWidth/3-viewWidth)/2

                when (itemPosition) {
                    0 -> return //不修改第一个item
                    else -> when(itemPosition%3){
                        0 -> view.setPadding(leftPadding.toInt(),0,edgeOffset.toInt(),0) //第三列
                        1 -> view.setPadding(edgeOffset.toInt(),0,rightPadding.toInt(),0) //第一列
                        else -> view.setPadding(midPadding.toInt(),0,midPadding.toInt(),0) //第二列
                    }
                }
            }
        }
        recyclerView.addItemDecoration(itemDecoration)
        //为recyclerView设置adapter
        recyclerView.adapter = concatAdapter

        flowersListViewModel.flowersLiveData.observe(this) {
            it?.let {
                flowersAdapter.submitList(it as MutableList<Flower>)
                headerAdapter.updateFlowerCount(it.size)
            }
        }

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener {
            fabOnClick()
        }
    }

    /* Opens FlowerDetailActivity when RecyclerView item is clicked. */
    private fun adapterOnClick(flower: Flower) {
        val intent = Intent(this, FlowerDetailActivity()::class.java)
        intent.putExtra(FLOWER_ID, flower.id)
        startActivity(intent)
    }

    /* Adds flower to flowerList when FAB is clicked. */
    private fun fabOnClick() {
        val intent = Intent(this, AddFlowerActivity::class.java)
        startActivityForResult(intent, newFlowerActivityRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        /* Inserts flower into viewModel. */
        if (requestCode == newFlowerActivityRequestCode && resultCode == Activity.RESULT_OK) {
            intentData?.let { data ->
                val flowerName = data.getStringExtra(FLOWER_NAME)
                val flowerDescription = data.getStringExtra(FLOWER_DESCRIPTION)

                flowersListViewModel.insertFlower(flowerName, flowerDescription)
            }
        }
    }
}