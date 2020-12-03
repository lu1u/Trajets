package com.lpi.trajets.GPS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.lpi.trajets.report.Report;

public class AlarmReceiver extends BroadcastReceiver
{
	/***
	 * Reception des evenements
	 *
	 * @param context
	 * @param intent
	 */
	public void onReceive(@NonNull Context context, @NonNull Intent intent)
	{
		Report.getInstance(context).log(Report.DEBUG, "Alarmreceiver");
		GPSService.refresh(context);
	}
}