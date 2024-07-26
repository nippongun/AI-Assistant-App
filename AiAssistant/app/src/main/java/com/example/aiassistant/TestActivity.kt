package com.example.aiassistant

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.aiassistant.domain.repository.APIRepository
import com.example.aiassistant.domain.viewmodels.AIBriefingTaskViewModel
import com.example.aiassistant.domain.repository.JSONRepository
import com.example.aiassistant.domain.usecase.ExecuteAITaskUseCase
import com.example.aiassistant.domain.viewmodels.AIBriefingTaskViewModelFactory
import com.example.aiassistant.domain.workers.AIBriefingWorker
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class TestActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val testData = List(20) { "Item $it" }
        adapter = TestAdapter(testData)
        recyclerView.adapter = adapter

        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                Log.d("MainActivity", "RecyclerView width: ${recyclerView.width}, height: ${recyclerView.height}")
            }
        })

        Log.d("TestActivity", "Setup complete. Items: ${testData.size}")
    }

    class TestAdapter(private val items: List<String>) : RecyclerView.Adapter<TestAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            Log.d("TestAdapter", "onCreateViewHolder called")
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = items[position]
            Log.d("TestAdapter", "onBindViewHolder called for position $position")
        }

        override fun getItemCount() = items.size.also {
            Log.d("TestAdapter", "getItemCount called, returning $it")
        }
    }
}