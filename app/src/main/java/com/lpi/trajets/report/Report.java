/**
 *
 */
package com.lpi.trajets.report;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lpi.trajets.BuildConfig;
import com.lpi.trajets.database.DatabaseHelper;

import java.util.Calendar;
import java.util.Locale;

/**
 * @author lucien
 */
@SuppressWarnings("nls")
public class Report
{
	private static final boolean GENERER_REPORT = BuildConfig.REPORT;

	// Niveaux de trace
	static public final int DEBUG = 0;
	static public final int WARNING = 1;
	static public final int ERROR = 2;

	private static final int MAX_BACKTRACE = 10;
	@Nullable
	private static Report INSTANCE = null;

	HistoriqueDatabase _historiqueDatabase;
	TracesDatabase _tracesDatabase;


	private Report(Context context)
	{
		if (GENERER_REPORT)
		{
			_historiqueDatabase = HistoriqueDatabase.getInstance(context);
			_tracesDatabase = TracesDatabase.getInstance(context);
		}
	}

	/**
	 * Point d'accès pour l'instance unique du singleton
	 *
	 * @param context: le context habituel d'ANdroid, peut être null si l'objet a deja ete utilise
	 */
	@NonNull
	public static synchronized Report getInstance(@NonNull Context context)
	{
		if (INSTANCE == null)
		{
			INSTANCE = new Report(context);
		}
		return INSTANCE;
	}


	@SuppressWarnings("boxing")
	public static String getLocalizedDate(long date)
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date);

		return String.format(Locale.getDefault(), "%02d/%02d/%02d %02d:%02d:%02d",
				c.get(Calendar.DAY_OF_MONTH),
				(c.get(Calendar.MONTH) + 1), c.get(Calendar.YEAR), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
				c.get(Calendar.SECOND)); // + ":" + c.get(Calendar.MILLISECOND) ;
	}


	public void log(@NonNull int niv, @NonNull String message)
	{
		if (GENERER_REPORT)
			_tracesDatabase.Ajoute(DatabaseHelper.CalendarToSQLiteDate(null), niv, message);
	}

	public void log(@NonNull int niv, @NonNull Exception e)
	{
		if (GENERER_REPORT)
		{
			log(niv, e.getLocalizedMessage());
			for (int i = 0; i < e.getStackTrace().length && i < MAX_BACKTRACE; i++)
				log(niv, e.getStackTrace()[i].getClassName() + '/' + e.getStackTrace()[i].getMethodName() + ':' + e.getStackTrace()[i].getLineNumber());
		}
	}

	public void historique(@NonNull String message)
	{
		if (GENERER_REPORT)
			_historiqueDatabase.ajoute(DatabaseHelper.CalendarToSQLiteDate(null), message);
	}


}
