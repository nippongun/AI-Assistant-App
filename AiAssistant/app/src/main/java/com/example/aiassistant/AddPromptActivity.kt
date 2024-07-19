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
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

class AddPromptActivity : AppCompatActivity() {

    private lateinit var stockContainer: LinearLayout
    private var lastAddedViewId: Int = View.NO_ID
    private var stockTextList = mutableListOf<EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_prompt)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        stockContainer = findViewById<LinearLayout>(R.id.stock_container)
        val addStockButton = findViewById<Button>(R.id.addStock)

        val logButton = findViewById<Button>(R.id.logButton)

        logButton.setOnClickListener {
            saveToJson()
        }
        addNewStockRow()
        addStockButton.setOnClickListener(){
            addNewStockRow()
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
    private fun getStockEditTexts(): MutableList<EditText> {
        val stockEditTexts = mutableListOf<EditText>()

        for (i in 0 until stockContainer.childCount) {
            val child = stockContainer.getChildAt(i)
            if (child is EditText) {
                stockEditTexts.add(child)
            }
        }

        return stockEditTexts
    }

    private fun saveToJson(){
        val editName = findViewById<EditText>(R.id.editName)
        val editLocation = findViewById<EditText>(R.id.editLocation)
        val editNews = findViewById<EditText>(R.id.editNews)
        stockTextList = getStockEditTexts()


        val promptObject = JSONObject()
        promptObject.put("name", editName.text.toString())
        promptObject.put("location", editLocation.text.toString())

        val stocksArray = JSONArray()
        for(editText in stockTextList) {
            val stockTicker = editText.text.toString().trim()
            if(stockTicker.isNotEmpty()){
                stocksArray.put(stockTicker)
            }
        }
        promptObject.put("stocks", stocksArray)

        val newsCategoriesArray = JSONArray()
        editNews.text.toString().split(",").forEach { category ->
            val trimmedCategory = category.trim()
            if (trimmedCategory.isNotEmpty()) {
                newsCategoriesArray.put(trimmedCategory)
            }
        }
        promptObject.put("news_categories", newsCategoriesArray)

        val promptsArray = JSONArray()
        promptsArray.put(promptObject)

        val jsonObject = JSONObject()
        jsonObject.put("prompts", promptsArray)

        try {
            val file = File(getExternalFilesDir(null), "prompts.json")
            FileWriter(file).use {writer ->
                writer.write(jsonObject.toString(2))
            }
            Toast.makeText(this, "Prompt saved!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving prompt: ${e.message}", Toast.LENGTH_LONG).show()

        }
    }
}