/*
 * @file	SensorController.java
 * @brief	センサー処理を管理する
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
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

//----------------------------------------------------------------------
// Global Class
//----------------------------------------------------------------------
public class SensorController extends Thread implements SensorEventListener
{
	//----------------------------------------------------------------------
	// 変数
	//----------------------------------------------------------------------
	public static boolean	gProximityState = false;	// 近接センサーの状態([近い/遠い]=[true/false])
	private SensorManager	mSensorManager = null;		// センサーマネージャ

	
	//----------------------------------------------------------------------
	// グローバル関数
	//----------------------------------------------------------------------
	/**
	 * @category	コンストラクタ関数
	 * @brief		コンストラクタ
	 * @param aContext	コンテキスト
	 * @note
	 */
	public SensorController(Context aContext)
	{
		// センサーマネージャ初期化
		mSensorManager	= (SensorManager)aContext.getSystemService(Context.SENSOR_SERVICE);

		// センサー登録
		List<Sensor> sensors	= mSensorManager.getSensorList(Sensor.TYPE_PROXIMITY);
		if (sensors.size() > 0) {
			Sensor sensor	= sensors.get(0);
			mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
		}
	}
	
	/**
	 * @category	グローバル関数
	 * @brief		レジューム
	 * @note
	 */
	public void onResume()
	{
		// センサーの再設定
	}
	
	/**
	 * @category	グローバル関数
	 * @brief		ポーズ
	 * @note
	 */
	public void onPause()
	{
		// センサーの解放
	}
	
	//----------------------------------------------------------------------
	// オーバーライド関数
	//----------------------------------------------------------------------
	/**
	 * @category	オーバーライド関数
	 * @brief		センサーの値が変化すると呼ばれる
	 * @note
	 */
	@Override
	public void onSensorChanged(SensorEvent aEvent)
	{
		// 近接センサー
		if (aEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			gProximityState	= (aEvent.values[0] == 0.0f);
		}
	}

	/**
	 * @category	オーバーライド関数
	 * @brief		センサーの精度が変更されると呼ばれる
	 * @note
	 */
	@Override
	public void onAccuracyChanged(Sensor aSensor, int aAccuracy) {
	}
}
/****************************** End Of File ******************************/
