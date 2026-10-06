package com.canzoo.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var input: EditText
    private lateinit var chat: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var tts: TextToSpeech
    private var language = "Français"

    override fun onCreate(state: Bundle?) {
        super.onCreate(state); setContentView(R.layout.activity_main)
        input=findViewById(R.id.messageInput); chat=findViewById(R.id.chatContainer)
        scroll=findViewById(R.id.chatScroll); tts=TextToSpeech(this,this)
        val langs=arrayOf("Français","Wolof","Pulaar Sénégal")
        findViewById<Spinner>(R.id.languageSpinner).apply {
            adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,langs)
            onItemSelectedListener=object: android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
                override fun onItemSelected(p: android.widget.AdapterView<*>?,v:android.view.View?,pos:Int,id:Long){language=langs[pos]}
            }
        }
        findViewById<Button>(R.id.sendButton).setOnClickListener{send()}
        findViewById<ImageButton>(R.id.micButton).setOnClickListener{listen()}
        add("Canzoo","Bonjour 👋 Je suis Canzoo. Choisis une langue et parle-moi.")
    }
    private fun send(){
        val s=input.text.toString().trim(); if(s.isEmpty())return
        add("Toi",s); input.setText("")
        val r=reply(s); add("Canzoo",r); speak(r)
    }
    private fun reply(s:String):String{
        val t=s.lowercase(Locale.ROOT)
        if(t.contains("bonjour")||t.contains("salut")) return when(language){
            "Wolof"->"Nanga def ! Maa ngi fi, di la ndimbal."
            "Pulaar Sénégal"->"Jam tan! Miɗo woni Canzoo, miɗo waawi wallude maa."
            else->"Bonjour ! Comment puis-je t'aider ?"
        }
        if(t.contains("wolof")) return "Je peux t'aider avec le vocabulaire, les dialogues et les exercices en wolof."
        if(t.contains("pulaar")||t.contains("peul")) return "Je peux t'enseigner le Pulaar du Sénégal avec des dialogues et des exercices."
        return "Demande reçue. Cette base est prête pour connecter le moteur IA complet de Canzoo."
    }
    private fun add(a:String,s:String){
        val v=TextView(this); v.text="$a\n$s"; v.setTextColor(ContextCompat.getColor(this,R.color.text))
        v.textSize=16f; v.setPadding(18,14,18,14)
        val lp=LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,6,0,6); v.layoutParams=lp
        chat.addView(v); scroll.post{scroll.fullScroll(ScrollView.FOCUS_DOWN)}
    }
    private fun listen(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.RECORD_AUDIO),10); return
        }
        if(!SpeechRecognizer.isRecognitionAvailable(this)){Toast.makeText(this,"Voix indisponible",Toast.LENGTH_SHORT).show();return}
        val i=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"fr-FR"); startActivityForResult(i,11)
    }
    override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d)
        if(r==11&&c==RESULT_OK){d?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let{input.setText(it);send()}}
    }
    override fun onInit(s:Int){if(s==TextToSpeech.SUCCESS)tts.language=Locale.FRENCH}
    private fun speak(s:String){tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"canzoo")}
    override fun onDestroy(){tts.stop();tts.shutdown();super.onDestroy()}
}
