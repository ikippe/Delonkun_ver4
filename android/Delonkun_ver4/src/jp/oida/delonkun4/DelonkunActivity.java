/*
 * @file	DelonkunActivity.java
 * @brief	メインアクティビティ
 * @data	2014-07-19
 * @version	v1.0.4
 * @auther	Taiki Furui
 * @note
 */
//----------------------------------------------------------------------
// Package
//----------------------------------------------------------------------
package jp.oida.delonkun4;

//----------------------------------------------------------------------
// Import
//----------------------------------------------------------------------
import java.util.ArrayList;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

//----------------------------------------------------------------------
// Global Class
//----------------------------------------------------------------------
public class DelonkunActivity extends Activity
{
	//----------------------------------------------------------------------
	// 定数
	//----------------------------------------------------------------------
	// リクエストコード
	private final static int REQUEST_CODE			= 0;

	// ワード関連
	private static final int WORD_KIND_STOP			= 0;
	private static final int WORD_KIND_FORWARD		= 1;
	private static final int WORD_KIND_BACK			= 2;
	private static final int WORD_KIND_RIGHT		= 3;
	private static final int WORD_KIND_LEFT			= 4;
	private static final int WORD_KIND_GO			= 5;
	private static final int WORD_KIND_CALIBRATION	= 6;
	private static final int WORD_KIND_ESCAPE		= 7;
	private static final String WORD_LIST[]	= {
		"top", 			// stop
		"前", 			// 前
		"しろ", 			// 後ろ
		"右", 			// 右
		"左", 			// 左
		"け", 			// いけ
		"レーション", 		// キャリブレーション
		"ケープ", 		// エスケープ
	};

	// トーン
	public static final int TONE_CMD_STOP			= ToneGenerator.TONE_DTMF_3;
	private static final int TONE_CMD_RIGHT			= ToneGenerator.TONE_DTMF_1;
	private static final int TONE_CMD_BACK			= ToneGenerator.TONE_DTMF_2;
	private static final int TONE_CMD_FORWARD		= ToneGenerator.TONE_DTMF_4;
	private static final int TONE_CMD_LEFT			= ToneGenerator.TONE_DTMF_5;
	private static final int TONE_CMD_CALIBRATION	= ToneGenerator.TONE_DTMF_6;
	private static final int TONE_CMD_ESCAPE		= ToneGenerator.TONE_DTMF_7;

	private static final int SEQ_CMD_NONE[] = {
		-1, -1
	};

	private static final int SEQ_CMD_RIGHT[][] = {
	//	cmd,						wait(ms)
		{TONE_CMD_RIGHT,			200},
		{TONE_CMD_STOP,				10},
		SEQ_CMD_NONE,
	};
	private static final int SEQ_CMD_BACK[][] = {
	//	cmd,						wait(ms)
		{TONE_CMD_BACK,				800},
		{TONE_CMD_STOP,				10},
		SEQ_CMD_NONE,
	};
	private static final int SEQ_CMD_STOP[][] = {
	//	cmd,						wait(ms)
		{TONE_CMD_STOP,				10},
		SEQ_CMD_NONE,
	};
	private static final int SEQ_CMD_FORWARD[][] = {
	//	cmd,						wait(ms)
		{TONE_CMD_FORWARD,			2000},
		{TONE_CMD_STOP,				10},
		SEQ_CMD_NONE,
	};
	private static final int SEQ_CMD_LEFT[][] = {
	//	cmd,						wait(ms)
		{TONE_CMD_LEFT,				200},
		{TONE_CMD_STOP,				10},
		SEQ_CMD_NONE,
	};
	private static final int SEQ_CMD_GO[][] = {
	//	cmd,						wait(ms)
		{TONE_CMD_FORWARD,			10000},
		{TONE_CMD_STOP,				10},
		SEQ_CMD_NONE,
	};
	private static final int SEQ_CMD_CALIBRATION[][] = {
	//	cmd,						wait(ms)
		{TONE_CMD_CALIBRATION,		5000},
		{TONE_CMD_STOP,				10},
		SEQ_CMD_NONE,
	};
	private static final int SEQ_CMD_ESCAPE[][] = {
	//	cmd,						wait(ms)
		{TONE_CMD_ESCAPE,			-2},
		SEQ_CMD_NONE,
	};
	private static final int SEQ_CMD_TBL[][][] = {
		SEQ_CMD_STOP,
		SEQ_CMD_FORWARD,
		SEQ_CMD_BACK,
		SEQ_CMD_RIGHT,
		SEQ_CMD_LEFT,
		SEQ_CMD_GO,
		SEQ_CMD_CALIBRATION,
		SEQ_CMD_ESCAPE,
	};

	//----------------------------------------------------------------------
	// 変数
	//----------------------------------------------------------------------
	private static ToneGenerator	m_toneGenerator = null;			// トーンジェネレータ
	private AudioManager		m_audioManager = null;			// オーディオマネージャ
	private SensorController	m_sensorController = null;		// センサーコントローラ
	private Context				m_context;						// コンテキスト
	private Handler				m_handler;						// ハンドラ
	private Intent				m_tmepResultData;				// リザルトデータ一時保管領域


	//----------------------------------------------------------------------
	// ローカル関数
	//----------------------------------------------------------------------
	/**
	 * @category	ローカル関数
	 * @brief		音声認識用インテントの発行(音声認識開始)
	 * @note
	 */
	private void ActivateRecognize()
	{
		try
		{
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Delon-kun Voice Recognition");
			startActivityForResult(intent, REQUEST_CODE);
		}
		catch( ActivityNotFoundException e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @category	ローカル関数	
	 * @brief		DTMFデータを端子に出力
	 * @param aTone	トーン情報
	 * @param aTime	処理時間
	 * @return		正常終了したか
	 * @note
	 */
	public static boolean PushTone(int aTone, int aTime)
	{
		int i;
		
		// トーンを鳴らす
		m_toneGenerator.startTone(aTone);
		try {Thread.sleep(32);} catch (InterruptedException e){e.printStackTrace();}
		m_toneGenerator.stopTone();
		try {
			if (aTime == -2) {					// 追跡モード専用処理
				while (true) {
					Log.d("tone", "[追跡]");
					if (SensorController.gProximityState) {
						return	false;			// 強制停止
					}
					Thread.sleep(1);
				}
			}
			else {
				for (i = 0; i < aTime; ++i) {
					Log.d("tone", "["+i+"]:"+SensorController.gProximityState);
					if (SensorController.gProximityState) {
						return	false;			// 強制停止
					}
					Thread.sleep(1);
				}
			}
		} catch(InterruptedException e) {e.printStackTrace();}
		return	true;
	}
	
	/**
	 * @category	ローカル関数
	 * @brief		トーストで端末認識内容を確認
	 * @param aWord		ワード
	 * @param aResult	結果
	 * @note
	 */
	private void TorstMessage(int aWord, String aResult)
	{
		String _msg = "";
		if ( aWord == 0xFF )
		{
			_msg = "【誤認識】";
		}
		else
		{
			_msg = "【"+WORD_LIST[aWord]+"】";
		}
		_msg += aResult;
		
		Message msg = Message.obtain();				// メッセージハンドラでToastに出力
		msg.obj = new String(_msg);
		m_handler.sendMessage(msg);
		Log.d("toast", _msg);
	}
	
	/**
	 * @category	ローカル関数
	 * @brief		マシンの動作用シーケンス処理
	 * @param aSeq	シーケンスリスト
	 * @note
	 */
	private void MachineMoveSequence(int aSeq[][])
	{
		int index = 0;
		
		while (aSeq[index][0] != -1)
		{
		  	if (!PushTone(aSeq[index][0], aSeq[index][1])) {
		  		PushTone(TONE_CMD_STOP, 10);					// 停止トーン発行
		  		break;
		  	}
		  	index++;
		}
	}

	
	//----------------------------------------------------------------------
	// オーバーライド関数
	//----------------------------------------------------------------------
	/**
	 * @category	オーバーライド関数
	 * @brief		初期化
	 * @note		最初に一度呼ばれる
	 */
	@Override
	public void onCreate(Bundle aSavedInstanceState) 
	{
		super.onCreate(aSavedInstanceState);
		m_context	= this;
		
		// トーンジェネレータを生成
		m_toneGenerator	= new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);

		// スリープしないように設定
		WindowSleep.invalid(this);

		// 画面レイアウトの設定
		LinearLayout _layout	= new LinearLayout(this);
		_layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(_layout);

		// ボタンを生成(音声識別用)
		Button _btnVoiceRec	= new Button(this);
		_btnVoiceRec.setText("Voice Recognition");
		_btnVoiceRec.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v)
			{
				ActivateRecognize();
			}
		});
		_layout.addView(_btnVoiceRec);

		// ボタンを生成(強制停止用)
		Button _btnStop	= new Button(this);
		_btnStop.setText("STOP!!!!!");
		_btnStop.setHeight(300);
		_btnStop.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v)
			{
				MachineMoveSequence(SEQ_CMD_TBL[WORD_KIND_STOP]);
			}
		});
		_layout.addView(_btnStop);

		// ストップ命令発行
		MachineMoveSequence(SEQ_CMD_TBL[WORD_KIND_STOP]);
		
		// センサーコントローラ生成
		m_sensorController	= new SensorController(m_context);
		
		m_handler	= new Handler()
					{
						public void handleMessage(Message message) {
							Toast.makeText(m_context, (String)message.obj, Toast.LENGTH_LONG).show();
						}
					};
	}
	
	/**
	 * @category	グローバル関数
	 * @brief		音声を判別し動作を決める
	 * @note
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent aData)
	{
		if ( requestCode == REQUEST_CODE && resultCode == RESULT_OK ) 
		{
			m_tmepResultData = aData;
		//	new Handler().post(new Runnable(){
			new Thread(new Runnable(){
				@Override
				public void run() {
					int _wordKind = 0xFF;
					
					// 音声識別開始
					ArrayList<String> results	= m_tmepResultData.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					String _resultsString	= "";
					for ( int i = 0; i < results.size(); ++i ) 
					{
						_resultsString += results.get(i);
					}
		
					// 識別内容により鳴らすトーンを決める
					if (_resultsString.contains(WORD_LIST[WORD_KIND_RIGHT]) )		// 右
					{
						_wordKind = WORD_KIND_RIGHT;
					}
					else 
					if ( _resultsString.contains(WORD_LIST[WORD_KIND_LEFT]) ) 		// 左
					{
						_wordKind = WORD_KIND_LEFT;
					}
					else
					if ( _resultsString.contains(WORD_LIST[WORD_KIND_FORWARD]) )	// 前
					{
						_wordKind = WORD_KIND_FORWARD;
					}
					else 
					if ( _resultsString.contains(WORD_LIST[WORD_KIND_BACK]) )		// 後
					{
						_wordKind = WORD_KIND_BACK;
					}
					else 
					if ( _resultsString.contains(WORD_LIST[WORD_KIND_GO]) )			// いけ
					{
						_wordKind = WORD_KIND_GO;
					}
					else 
					if ( _resultsString.contains(WORD_LIST[WORD_KIND_CALIBRATION]) )		// キャリブレーション
					{
						_wordKind = WORD_KIND_CALIBRATION;
					}
					else 
					if ( _resultsString.contains(WORD_LIST[WORD_KIND_ESCAPE]) )		// エスケープ
					{
						_wordKind = WORD_KIND_ESCAPE;
					}
		
					// シーケンスの実行
					Log.d("tone", "word:["+WORD_LIST[_wordKind]+"] "+
								  "toneNum:["+(SEQ_CMD_TBL[_wordKind][0][0]-ToneGenerator.TONE_DTMF_1+1)+"]");
					if (_wordKind != 0xFF)
					{
						MachineMoveSequence(SEQ_CMD_TBL[_wordKind]);
					}
		
					// 認識内容を表示
					TorstMessage(_wordKind, _resultsString);
		
					// 再度音声識別スタート
					ActivateRecognize();
				}
			}).start();
		//	});
		}
		super.onActivityResult(requestCode, resultCode, m_tmepResultData);
	}
	
	/**
	 * @category	グローバル関数
	 * @brief		ハードキー操作
	 * @note
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		int volume;
		
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:				// バックキー
			// アプリを終了する
			this.finish();
			Process.killProcess(Process.myPid());
			break;

		case KeyEvent.KEYCODE_VOLUME_UP:		// ボリュームアップ
			if (m_audioManager == null) {
				m_audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			}
			volume = m_audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
			m_audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volume+1, 0);
			Log.d("volume", "volume:"+(volume+1));
			break;

		case KeyEvent.KEYCODE_VOLUME_DOWN:		// ボリュームアップ
			if (m_audioManager == null) {
				m_audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			}
			volume = m_audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
			m_audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volume-1, 0);
			Log.d("volume", "volume:"+(volume-1));
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
/****************************** End Of File ******************************/
