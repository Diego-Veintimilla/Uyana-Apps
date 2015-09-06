package com.javaorigin.audio;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity {
	AudioManager	am	= null;
	AudioRecord record =null;
	AudioTrack track =null;
	OnSeekBarChangeListener barChange;
	
	private float masterVolume = 1.0f;
	private float leftVolume = 1.0f;
	private float rightVolume = 1.0f;
    private float balance = 0.5f;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
		
		// Create a seek bar handler
        barChange = new OnSeekBarChangeListener() 
        {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {	}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {  }
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				switch (seekBar.getId())
				{
				 case R.id.VolBar1:
					 setVolume((float)progress/100.0f);
					 break;
					 
				 case R.id.BalBar:
					 setBalance((float)progress/100.0f);
					 break;
				
				}
			}
		};
		
		// Set our handler as the ChangeListener for the seekbar controls  
        SeekBar sb;
        
        sb = (SeekBar)findViewById(R.id.BalBar);
        sb.setOnSeekBarChangeListener(barChange);  
        
        sb = (SeekBar)findViewById(R.id.VolBar1);
        sb.setOnSeekBarChangeListener(barChange);
		init();
		(new Thread() {
			@Override
			public void run() {
				recordAndPlay();
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void init() {
		int min = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		record = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 8000, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, min);

		int maxJitter = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		track = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, 8000, AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);
	}

	private void recordAndPlay() {
		short[] lin = new short[1024];
		int num = 0;
		am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		am.setMode(AudioManager.MODE_IN_COMMUNICATION);
		record.startRecording();
		
		track.play();
		while (true) {
			num = record.read(lin, 0, 1024);
			track.write(lin, 0, num);
		}
	}
	
	boolean isSpeaker = false;

	public void modeChange(View view) {
		Button modeBtn=(Button) findViewById(R.id.modeBtn);
		if (isSpeaker == true) {			
			am.setSpeakerphoneOn(false);
			isSpeaker = false;
			modeBtn.setText("Salida/Parlantes");
		} else {			
			am.setSpeakerphoneOn(true);
			isSpeaker = true;
			modeBtn.setText("Salida/Manos Libres");
		}
	}
    
	boolean isPlaying=true;
	public void play(View view){
		Button playBtn=(Button) findViewById(R.id.playBtn);
		if(isPlaying){
			record.stop();
			track.pause();
			
			isPlaying=false;
			playBtn.setText("Escuchar");
		}else{
			record.startRecording();
			track.setStereoVolume(leftVolume, rightVolume);
			track.play();
			isPlaying=true;
			playBtn.setText("Aplicar Cambios de Configuración");
		}
	}
	
		public void setVolume(float newValue)
		{
			masterVolume = newValue;
			
			if(balance < 1.0f)
			{
				leftVolume = masterVolume;
				rightVolume = masterVolume * balance;
			}
			else
			{
				rightVolume = masterVolume;
				leftVolume = masterVolume * ( 2.0f - balance );
			}

		}
		
		public void setBalance(float newValue)
		{
			balance = newValue;
			setVolume(masterVolume);
		}
}
