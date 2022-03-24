package com.example.lab_02

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {
    private var currentOperation: String = " "
    private var result: Float= 0f
    private var format: String= "Float"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spinner:Spinner = findViewById(R.id.operatingSpinner)

        val categories= mutableListOf<String>("+ and -", "* and /")
        val dataAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter= dataAdapter
        spinner.onItemSelectedListener= object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                //find the operation button
                Toast.makeText(applicationContext, categories[pos], Toast.LENGTH_SHORT).show()
                val op1Button: Button = findViewById(R.id.firstOperationButton)
                val op2Button: Button = findViewById(R.id.secondOperationButton)


                //based on selected position change the text of buttons
                when(pos){
                    0->{
                        op1Button.text= "+"
                        op2Button.text= "-"
                    }
                    1->{
                        op1Button.text= "*"
                        op2Button.text= "/"
                    }
                    else ->{
                        op1Button.text= "+"
                        op2Button.text= "-"
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        val calcButton: Button= findViewById(R.id.calcButton)
        calcButton.setOnClickListener(this)
        calcButton.setOnLongClickListener(this)
        val fab= findViewById<FloatingActionButton>(R.id.clearButton)
        fab.setOnClickListener{
            clearCalculator()
        }
    }

    private fun clearCalculator() {
        currentOperation=" "
        result = 0f
        updateOperation()
        updateResult(getString(R.string.result))
        findViewById<EditText>(R.id.firstNumber).text.clear()
        findViewById<EditText>(R.id.secondNumber).text.clear()

        Snackbar.make(
            findViewById(R.id.mainContainer),
            getString(R.string.clear_msg),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    fun selectOperation(view: View) {
        currentOperation= (view as Button).text.toString()

        updateOperation()
    }

    private fun updateOperation() {
        val operationTxt= findViewById<TextView>(R.id.operatorSymbol)
        operationTxt.text= currentOperation
    }

    override fun onClick(p0: View?) {
        val firstNum=  (findViewById<EditText>(R.id.firstNumber).text).toString().toFloat()
        val secondNum= (findViewById<EditText>(R.id.secondNumber).text).toString().toFloat()
        val resultStr: String= getResult(firstNum,secondNum)
        updateResult(resultStr)
    }

    override fun onLongClick(p0: View?): Boolean {
        val firstNum=
            (findViewById<EditText>(R.id.firstNumber).text).toString().toFloatOrNull() ?: 0f
        val secondNum=
            (findViewById<EditText>(R.id.secondNumber).text).toString().toFloatOrNull() ?: 0f

        val resultStr: String= getResult(firstNum,secondNum, updateResult=true)

        updateResult(resultStr)
        return true
    }

    private fun getResult(firstNum: Float, secondNum: Float,
                          updateResult: Boolean=false): String {

        val prevResult= if(updateResult){
            result
        }else{
            if(currentOperation == "/" || currentOperation == "*") 1f else 0f
        }

        result= when (currentOperation){
            "+" -> prevResult + firstNum + secondNum
            "-" -> {
                if(prevResult != 0f)
                    prevResult - (firstNum - secondNum)
                else{
                    firstNum-secondNum
                }
            }
            "*" -> prevResult * firstNum * secondNum
            "/" -> prevResult / firstNum / secondNum
            else -> 0f
        }
        //to ma sie zrobic, zeby wyswietallo int i float


        return if(format=="Integer"){
            "${getString(R.string.result)} ${result.roundToInt()}"
        } else {
            return "${getString(R.string.result)} ${result}"
        }


    }

    private fun updateResult(resultStr: String) {
        val resultTextView= findViewById<TextView>(R.id.resultView)
        resultTextView.text=resultStr
    }
    //crashing app
    override fun onCreateOptionsMenu(menu: Menu?): Boolean{
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.settingsItem -> startSettingsActivity()
            R.id.shareItem -> shareResult()
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shareResult() {
        val shareIntent: Intent= Intent(Intent.ACTION_SEND)

        shareIntent.setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, getExpressionResult())

        if(shareIntent.resolveActivity(packageManager) != null){
            startActivity(shareIntent)
        }
    }

    private fun getExpressionResult(): String? {
        val firstNum=(findViewById<EditText>(R.id.firstNumber).text).toString().toFloatOrNull() ?: 0f
        val secondNum=(findViewById<EditText>(R.id.secondNumber).text).toString().toFloatOrNull() ?: 0f
        val equation= "${firstNum} $currentOperation ${secondNum} = "
        return when (currentOperation){
            "+" -> "$equation ${firstNum + secondNum}"
            "-" -> "$equation ${firstNum - secondNum}"
            "*" -> "$equation ${firstNum * secondNum}"
            "/" -> "$equation ${firstNum / secondNum}"
            else -> "error"
        }

    }

    private fun startSettingsActivity() {
        val intent: Intent = Intent(this,SettingsActivity::class.java)

        intent.putExtra(getString(R.string.numberFormatKey),format)

        launchSettingsActivity.launch(intent)
    }

    private val launchSettingsActivity=
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if(result.resultCode == RESULT_OK){
            format = result.data?.getStringExtra(getString(R.string.numberFormatKey)) ?: "Float"
            Snackbar.make(
                findViewById(R.id.mainContainer),
                format,
                Snackbar.LENGTH_SHORT
            ).show()
        }


    }



}
