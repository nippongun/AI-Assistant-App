package com.example.aiassistant

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.aiassistant.domain.model.Prompt
import com.example.aiassistant.domain.model.ScheduledTask
import com.example.aiassistant.domain.repository.JSONRepository
import com.example.aiassistant.domain.repository.ScheduledTaskRepository

class ScheduleDialogFragment : DialogFragment() {
    private lateinit var timePicker: TimePicker
    private lateinit var promptSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var scheduleManager: ScheduledTaskRepository
    private lateinit var prompts: List<Prompt>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_schedule_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scheduleManager = ScheduledTaskRepository(requireContext())

        timePicker = view.findViewById(R.id.timePicker)
        promptSpinner = view.findViewById(R.id.promptSpinner)
        saveButton = view.findViewById(R.id.saveButton)

        // Set TimePicker to 24h view
        timePicker.setIs24HourView(true)

        prompts = getPrompts()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, prompts.map { it.name })
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_item_dropdown_item)
        promptSpinner.adapter = adapter

        saveButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val selectedPrompt = prompts[promptSpinner.selectedItemPosition]

            val scheduledTask = ScheduledTask(
                hour = hour,
                minute = minute,
                promptName = selectedPrompt.name,
                promptLocation = selectedPrompt.location,
                promptStocks = selectedPrompt.stocks,
                promptNews = selectedPrompt.news,
                promptRhetoric = selectedPrompt.rhetoric,
                active = true
            )

            scheduleManager.saveScheduledTask(scheduledTask)

            Toast.makeText(context, "Scheduled for $hour:$minute", Toast.LENGTH_SHORT).show()
            setFragmentResult(REQUEST_KEY, bundleOf(TASK_KEY to true))
            dismiss()
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle("Schedule AI Prompt")
        return dialog
    }

    companion object {
        const val TAG = "ScheduleDialogFragment"
        const val REQUEST_KEY = "SCHEDULE_REQUEST"
        const val TASK_KEY = "TASK"

        fun newInstance() = ScheduleDialogFragment()
    }

    private fun getPrompts(): List<Prompt> {
        return JSONRepository.deserializePrompts(requireContext())
    }
}