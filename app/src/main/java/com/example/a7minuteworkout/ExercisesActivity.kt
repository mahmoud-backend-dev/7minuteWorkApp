package com.example.a7minuteworkout

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_exercises.*
import kotlinx.android.synthetic.main.custom_dialog_back_confirmation.*
import java.util.*
import kotlin.collections.ArrayList

class ExercisesActivity : AppCompatActivity(),TextToSpeech.OnInitListener {
   private var restTimer:CountDownTimer?=null
   private var restProgress=0
    private var exercisesTimer:CountDownTimer?=null
    private var exercisesProgress=0
    private var exercisesDuration:Long=30
    private var restDuration:Long=10
    private var exerciseList:ArrayList<ExerciseModel>?=null
    private var currentExercisePosition=-1
    private var tts:TextToSpeech?=null
    private var player:MediaPlayer?=null
    private var pauseRest:Long=0
    private var pauseExercise:Long=0
    private var pauseCountTime:Boolean=true
    private var exerciseStatusAdapter:ExerciseStatusAdapter?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)
        setSupportActionBar(toolbar_exercise_activity)
        val actionbar=supportActionBar
        if (actionbar!=null){
            actionbar.setDisplayHomeAsUpEnabled(true)
        }
        toolbar_exercise_activity.setNavigationOnClickListener {
            customDialogForBackButton()
        }
        tts= TextToSpeech(this,this)
        exerciseList=Constants.defaultExercisesList()
        setupResetView()
        setUpExerciseStatusRecyclerView()
    }

    override fun onDestroy() {
        if (restTimer!=null){
            restTimer!!.cancel()
            restProgress=0
        }
        if (exercisesTimer!=null){
            exercisesTimer!!.cancel()
            exercisesProgress=0
        }
        if (player!=null){
            player!!.stop()
        }
        if (tts!=null){
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
    private fun setResetProgressBar(){
        progressBar.progress=restProgress
        restTimer=object :CountDownTimer((restDuration-pauseRest)*1000,1000){
            override fun onTick(millisUntilFinished: Long) {
                restProgress++
                pauseRest=restDuration-millisUntilFinished
                progressBar.progress=10-restProgress
                tvTimer.text=(10-restProgress).toString()
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onFinish() {
                currentExercisePosition++
                exerciseList!![currentExercisePosition].setIsSelected(true)
                exerciseStatusAdapter!!.notifyDataSetChanged()
                setupResetExercisesView()
            }
        }.start()
    }
    private fun setResetExercisesProgressBar(){
        exercisesprogressBar.progress=exercisesProgress
        exercisesTimer=object :CountDownTimer(  (exercisesDuration-pauseExercise)*1000,1000){
            override fun onTick(millisUntilFinished: Long) {
                exercisesProgress++
                pauseExercise=exercisesDuration-millisUntilFinished
                exercisesprogressBar.progress=exercisesDuration.toInt()-exercisesProgress
                tvExerciseTimer.text=(exercisesDuration.toInt()-exercisesProgress).toString()
            }

            override fun onFinish() {
                exerciseList!![currentExercisePosition].setIsSelected(false)
                exerciseList!![currentExercisePosition].setIsCompleted(true)
                exerciseStatusAdapter!!.notifyDataSetChanged()
                if(currentExercisePosition<exerciseList!!.size-1){
                    setupResetView()
                }else{
                    finish()
                    val intent=Intent(this@ExercisesActivity,FinishActivity::class.java)
                    startActivity(intent)
                }
            }
        }.start()
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setupResetExercisesView(){
        llRestView.visibility= View.GONE
        llExerciseView.visibility=View.VISIBLE
        if (exercisesTimer!=null){
            exercisesTimer!!.cancel()
            pauseExercise=0
            exercisesProgress=0
        }
        speakOut(exerciseList!![currentExercisePosition].getName())
        ivImage.setImageResource(exerciseList!![currentExercisePosition].getImage())
        tvExerciseName.text=exerciseList!![currentExercisePosition].getName()
        setResetExercisesProgressBar()
    }
    private fun setupResetView(){
        try {
            player= MediaPlayer.create(applicationContext,R.raw.press_start)
            player!!.isLooping=false
            player!!.start()
        }catch (e:Exception){}
        llRestView.visibility=View.VISIBLE
        llExerciseView.visibility=View.GONE
        if (restTimer!=null){
            restTimer!!.cancel()
            pauseRest=0
            restProgress=0
        }
        tvUpComingExerciseName.text=exerciseList!![currentExercisePosition+1].getName()
        setResetProgressBar()
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun speakOut(text:String){
        tts!!.speak(text,TextToSpeech.QUEUE_FLUSH,null,"")
    }
    override fun onInit(status: Int) {
        if (status==TextToSpeech.SUCCESS){
            val result=tts!!.setLanguage(Locale.US)
            if(result==TextToSpeech.LANG_MISSING_DATA || result==TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS","The language specified is not supported!")
            }
        }else{
            Log.e("TTS","initialization failed!")
        }
    }
    private fun setUpExerciseStatusRecyclerView(){
        exerciseStatusAdapter= ExerciseStatusAdapter(exerciseList!!,this)
        rvExerciseStatus.layoutManager=LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,
            false)
        rvExerciseStatus.adapter=exerciseStatusAdapter
    }
    private fun customDialogForBackButton()
    {
        val customDialog=Dialog(this)
        customDialog.setContentView(R.layout.custom_dialog_back_confirmation)
        customDialog.tvYes.setOnClickListener {
            finish()
            customDialog.dismiss()
        }
        customDialog.tvNo.setOnClickListener {
            pauseCountTime=false
            customDialog.dismiss()
        }
        customDialog.setCanceledOnTouchOutside(false)

        customDialog.show()
    }
}