package com.example.aiassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.aiassistant.databinding.ActivityMainBinding
import com.example.aiassistant.domain.model.ScheduledTask
import com.example.aiassistant.domain.repository.APIRepository
import com.example.aiassistant.domain.repository.ScheduledTaskRepository
import com.example.aiassistant.domain.usecase.ExecuteAITaskUseCase
import com.example.aiassistant.domain.usecase.ScheduleManager
import com.example.aiassistant.domain.viewmodels.AIBriefingTaskViewModelFactory
import com.example.aiassistant.domain.workers.AIBriefingWorkerFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var scheduleManager: ScheduledTaskRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduledTaskAdapter
    private lateinit var executeAITaskUseCase: ExecuteAITaskUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scheduleManager = ScheduledTaskRepository(this)
        val repository = APIRepository()
        executeAITaskUseCase = ExecuteAITaskUseCase(repository)
        val myWorkerFactory = AIBriefingWorkerFactory(executeAITaskUseCase)
        val config = Configuration.Builder()
            .setWorkerFactory(myWorkerFactory)
            .build()
        WorkManager.initialize(this,config)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        updateTaskList()
        binding.fab.setOnClickListener { view ->
            val intent = Intent(this, AddPromptActivity::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.testButton).setOnClickListener {
            val scheduleDialog = ScheduleDialogFragment.newInstance()
            scheduleDialog.show(supportFragmentManager, ScheduleDialogFragment.TAG)
        }

        supportFragmentManager.setFragmentResultListener(ScheduleDialogFragment.REQUEST_KEY, this) { _, bundle ->
            val taskAdded = bundle.getBoolean(ScheduleDialogFragment.TASK_KEY, false)
            if (taskAdded) {
                updateTaskList()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateTaskList() {
        try {
            val tasks = scheduleManager.getAllScheduledTasks()
            Log.d("MainActivity", "Fetched tasks: ${tasks.size}")

            adapter = ScheduledTaskAdapter(tasks) { task, isActive ->
                scheduleManager.toggleTaskActive(task.id, isActive)
            }
            Log.d("MainActivity", "Created new adapter")

            recyclerView.adapter = adapter
            Log.d("MainActivity", "Set adapter to RecyclerView")

            recyclerView.post {
                Log.d("MainActivity", "In RecyclerView post")
                adapter.notifyDataSetChanged()
                Log.d("MainActivity", "Called notifyDataSetChanged")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error updating task list: ${e.message}")
        }
    }

    inner class ScheduledTaskAdapter(
        private val tasks: List<ScheduledTask>,
        private val onToggleTask: (ScheduledTask, Boolean) -> Unit
    ) : RecyclerView.Adapter<ScheduledTaskAdapter.ViewHolder>() {

        init {
            Log.d("Adapter", "Adapter initialized with ${tasks.size} tasks")
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val taskName: TextView = view.findViewById(R.id.taskName)
            val taskTime: TextView = view.findViewById(R.id.taskTime)
            val activeSwitch: SwitchCompat = view.findViewById(R.id.activeSwitch)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_scheduled_task, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val task = tasks[position]
            Log.d("Adapter", "Binding view for task: $task")
            holder.taskName.text = task.promptName ?: "No Name"
            holder.taskTime.text = String.format("%02d:%02d", task.hour, task.minute)
            holder.activeSwitch.isChecked = task.active
            holder.activeSwitch.setOnCheckedChangeListener { _, isChecked ->
                onToggleTask(task, isChecked)
            }

        }

        override fun getItemCount(): Int {
            Log.d("Adapter", "getItemCount called, returning ${tasks.size}")
            return tasks.size
        }
    }
}