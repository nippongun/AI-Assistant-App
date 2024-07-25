package com.example.aiassistant

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.aiassistant.domain.model.Prompt
import com.example.aiassistant.domain.repository.JSONRepository

class AddPromptActivity : AppCompatActivity() {
    private lateinit var newsContainer: LinearLayout
    private lateinit var stockContainer: LinearLayout
    private var lastAddedViewId: Int = View.NO_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_prompt)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        newsContainer = findViewById<LinearLayout>(R.id.newsContainer)
        stockContainer = findViewById<LinearLayout>(R.id.stockContainer)
        val addStockButton = findViewById<Button>(R.id.addStock)
        val addNewsButton = findViewById<Button>(R.id.addNewsButton)

        val logButton = findViewById<Button>(R.id.logButton)

        logButton.setOnClickListener {
            saveToJson()
        }
        addStockButton.setOnClickListener(){
            addNewStockRow()
        }
        addNewsButton.setOnClickListener(){
            addNewNewsRow()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the Up button click here
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addNewStockRow(){
        val newRow = EditText(this).apply {
            val heightInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64f, resources.displayMetrics).toInt()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                heightInPixels
            ).apply {
                val marginInPixels = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics
                ).toInt()
                setMargins(0,marginInPixels,0,0)
            }
            hint = "Enter Stock"
            setEms(10)
            background = ContextCompat.getDrawable(context,R.drawable.roundstyle)
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.element)
            setTextColor(ContextCompat.getColor(context, R.color.text_color))
            setHintTextColor(ContextCompat.getColor(context, R.color.gray_hint))
        }
        stockContainer.addView(newRow)
        Log.d("AddPromptActivity", "AddedRow: $newRow")
    }
    private fun getStocksStrings(): List<String> {
        val stockEditTexts = mutableListOf<String>()

        for (i in 0 until stockContainer.childCount) {
            val child = stockContainer.getChildAt(i)
            if (child is EditText) {
                stockEditTexts.add(child.text.toString())
            }
        }

        return stockEditTexts
    }

    private fun getNewsStrings(): List<String> {
        val newsEditTexts = mutableListOf<String>()

        for (i in 0 until newsContainer.childCount) {
            val child = newsContainer.getChildAt(i)
            if (child is EditText) {
                newsEditTexts.add(child.text.toString())
            }
        }
        return newsEditTexts
    }

    private fun addNewNewsRow(){
        val newRow = EditText(this).apply {
            val heightInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64f, resources.displayMetrics).toInt()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                heightInPixels
            ).apply {
                val marginInPixels = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics
                ).toInt()
                setMargins(0,marginInPixels,0,0)
            }
            hint = "Enter News"
            setEms(10)
            background = ContextCompat.getDrawable(context,R.drawable.roundstyle)
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.element)
            setTextColor(ContextCompat.getColor(context, R.color.text_color))
            setHintTextColor(ContextCompat.getColor(context, R.color.gray_hint))
        }
        newsContainer.addView(newRow)
        Log.d("AddPromptActivity", "AddedRow: $newRow")
    }

    private fun saveToJson(){

        val prompt: Prompt = Prompt(
            name = findViewById<EditText>(R.id.editName).text.toString(),
            location = findViewById<EditText>(R.id.editLocation).text.toString(),
            stocks = getStocksStrings(),
            news = getNewsStrings(),
            rhetoric = findViewById<EditText>(R.id.editRhetoric).text.toString()
        )
        try {
            JSONRepository.appendPromptToJson(this,prompt)
            Toast.makeText(this, "Prompt saved!", Toast.LENGTH_LONG).show()
        } catch (e: Exception){
            Toast.makeText(this,
                "Error saving prompt: ${e.message}",
                android.widget.Toast.LENGTH_LONG).show()
        }


    }
}