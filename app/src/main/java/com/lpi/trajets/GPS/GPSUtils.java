package com.lpi.trajets.GPS;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lpi.trajets.report.Report;

public class GPSUtils
{
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final float MAX_IMPRECISION = 20;              // Imprecision max du GPS admise

	/**
	 * Determines whether one Location reading is better than the current Location fix
	 *
	 * @param location            The new Location that you want to evaluate
	 * @param currentBestLocation The current Location fix, to which you want to compare the new one
	 */
	public static boolean isBetterLocation(@NonNull final Context context, @NonNull final Location location, @Nullable final Location currentBestLocation)
	{
		Report report = Report.getInstance(context);
		if ( location.getAccuracy() > MAX_IMPRECISION)
		{
			report.log( Report.WARNING, "Position regetée: trop imprécis, précision " + location.getAccuracy() + "m");
			// Trop imprecis
			return false;
		}

		if (currentBestLocation == null)
			// A new location is always better than no location
			return true;

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer)
		{
			return true;
			// If the new location is more than two minutes older, it must be worse
		}
		else if (isSignificantlyOlder)
		{
			report.log( Report.WARNING, "Position regetée: isSignificantlyOlder");
			return false;
		}

		boolean isNewer = timeDelta > 0;
		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate)
		{
			return true;
		}
		else if (isNewer && !isLessAccurate)
		{
			return true;
		}
		else
		{
			if ( !(isNewer && !isSignificantlyLessAccurate && isFromSameProvider))
				report.log( Report.WARNING, "Position regetée: !(isNewer && !isSignificantlyLessAccurate && isFromSameProvider)");
			return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
		}
	}


	/**
	 * Checks whether two providers are the same
	 */
	private static boolean isSameProvider(@Nullable String provider1, @Nullable String provider2)
	{
		if (provider1 == null)
		{
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
