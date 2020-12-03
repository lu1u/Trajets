////////////////////////////////////////////////////////////////////////////////////////////////////
// Gestion du GPS dans un service pour continuer a recevoir les positions meme si l'activity
// principale n'est plus active
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.lpi.trajets.GPS;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.lpi.trajets.database.DatabaseHelper;
import com.lpi.trajets.database.ItinerairesDatabase;
import com.lpi.trajets.itineraire.Position;
import com.lpi.trajets.preferences.Preferences;
import com.lpi.trajets.report.Report;

public class GPSService extends Service implements LocationListener
{
	private static final String DEMARRAGE = GPSService.class.getCanonicalName() + ".action.DEMARRAGE";
	private static final String STOP = GPSService.class.getCanonicalName() + ".action.STOP";
	private static final String REFRESH = GPSService.class.getCanonicalName() + ".action.REFRESH";
	private static PowerManager.WakeLock wl;

	private Report _report;
	@Nullable
	private LocationManager _locationManager;
	private Location dernierePosition;
	private PendingIntent _pendingIntentAlarme;
	private AlarmManager _alarmManager;
	private int _minDistance;
	private Intent _alarmReceiverIntent;

	public GPSService()
	{
	}

	/***
	 * Mise a jour du service: demarrage ou arret en fonction du nombre
	 * de randonnees qui enregistrent a ce moment
	 * @param context
	 */
	public static void update(Context context)
	{
		// Obtenir le nombre de randonnees en cours d'enregistrement
		ItinerairesDatabase database = ItinerairesDatabase.getInstance(context);
		final int nbRandonnees = database.getNbItinerairesEnregistrant();

		// Si 0 randonnees: arreter le GPS
		if (nbRandonnees == 0)
		{
			if (wl != null)
				if (wl.isHeld())
					wl.release();
			GPSService.arrete(context);
		}
		else
		{
			if (wl == null)
			{
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Itineraires:");
			}

			wl.acquire();
			GPSService.demarre(context);
		}
	}

	/***
	 * Previent le service qu'il doit s'arreter
	 * @param context
	 */
	public static void arrete(@NonNull Context context)
	{
		Intent intent = new Intent(context, GPSService.class);
		intent.setAction(STOP);
		context.startService(intent);
	}

	/***
	 * Demarre le service
	 * @param context
	 */
	public static void demarre(@NonNull Context context)
	{
		Intent intent = new Intent(context, GPSService.class);
		intent.setAction(DEMARRAGE);
		context.startService(intent);
	}

	public static void refresh(@NonNull Context context)
	{
		Intent intent = new Intent(context, GPSService.class);
		intent.setAction(REFRESH);
		context.startService(intent);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}


	/***
	 * reception de la commande de demarrage ou d'arret
	 * @param intent
	 * @param flags
	 * @param startId
	 * @return
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);
		_report = Report.getInstance(getApplicationContext());

		String action = intent.getAction();
		_report.log(Report.DEBUG, "GPSService:onStartCommand " + action);
		if (DEMARRAGE.equals(action))
		{
			// Demarre le service
			handleActionDemarre();
			return START_STICKY;
		}
		else
			if (STOP.equals(action))
			{
				// Arrete ce service
				handleActionStop();
			}
			else
				if (REFRESH.equals(action))
				{
					// Demarre le service
					handleActionRefresh();
					return START_STICKY;
				}

		return START_NOT_STICKY;
	}

	/**
	 * Reception d'un message a destination de ce service
	 *
	 * @param context : Context
	 * @param intent  : Intent recu
	 */
	protected void onServiceReceive(Context context, @Nullable Intent intent)
	{
		if (intent != null)
		{
			final String action = intent.getAction();
			if (DEMARRAGE.equals(action))
			{
				handleActionDemarre();
			}
			else
				if (STOP.equals(action))
				{
					handleActionStop();
				}
		}
	}

	/**
	 * Demarrer le GPS
	 */
	private void handleActionDemarre()
	{
		// demarrer le GPS
		try
		{
			Context context = getApplicationContext();
			Preferences prefs = Preferences.getInstance(context);
			final int GPSDelai = prefs.getGPSMinTime();
			_minDistance = prefs.getGPSMinDistance();

			if (_locationManager == null)
			{
				// GPS
				// Les permissions ont ete verifiee au demarrage de l'application
				if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
						|| ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
				{
					return;
				}

				_report.log(Report.DEBUG, "Création du GPS manager");
				_locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			}


			if (prefs.getLocalisationGPS())
			{
				_report.log(Report.DEBUG, "Demarrage localisation GPS");
				_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPSDelai, _minDistance, this);
			}
			if (prefs.getLocalisationReseau())
			{
				_report.log(Report.DEBUG, "Demarrage localisation Reseau");
				_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPSDelai, _minDistance, this);
			}

			_alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			timeOutGPS(context);
		} catch (Exception e)
		{
			_report.log(Report.ERROR, e);
		}
	}

	/***
	 * Creer une alarme pour lire les coordonnees GPS si on n'en a pas recu depuis un certain
	 * temps
	 * @param context
	 */
	private void timeOutGPS(@NonNull final Context context)
	{
		final int delai = Preferences.getInstance(context).getGPSMinTime() * 4; // 4 fois le delai pour laisser une chance au GPS de nous prevenir de lui meme

		if (_alarmReceiverIntent == null)
			_alarmReceiverIntent = new Intent(context, AlarmReceiver.class);

		// Supprimer l'ancienne alarme
		_pendingIntentAlarme = PendingIntent.getBroadcast(context, 0, _alarmReceiverIntent, 0);
		_alarmManager.cancel(_pendingIntentAlarme);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, _alarmReceiverIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		// Nouvelle position dans n secondes au plus tard
		_alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delai, pendingIntent);
	}



	/**
	 * Arreter de surveiller le GPS
	 */
	private void handleActionStop()
	{
		try
		{
			// Arrete le timeout GPS
			if (_alarmManager != null && _pendingIntentAlarme != null)
				_alarmManager.cancel(_pendingIntentAlarme);

			if (_locationManager != null)
			{
				_report.log(Report.DEBUG, "Arret du GPS");
				_locationManager.removeUpdates(this);
				_locationManager = null;
			}

			// Arreter ce service
			this.stopSelf();
		} catch (Exception e)
		{
			_report.log(Report.ERROR, e);
		}
	}

	/**
	 * Forcer la lecture de la derniere position connue
	 * Mecanisme pour demander la derniere position du GPS si on n'en a pas recu depuis un
	 * certain temps
	 */
	private void handleActionRefresh()
	{
		Context context = getApplicationContext();
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				|| ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			return;

		if (_locationManager != null)
		{
			Preferences prefs = Preferences.getInstance(context);

			if (prefs.getLocalisationGPS())
			{
				ajoutePosition(_locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
				return;
			}
			if (prefs.getLocalisationReseau())
			{
				ajoutePosition(_locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
				return;
			}

			// Redemander un timeout GPS
			timeOutGPS(context);
		}
	}

	/***
	 * Reception d'une nouvelle position GPS
	 * @param ici : position GPS
	 */
	@Override
	public void onLocationChanged(@NonNull Location ici)
	{
		_report.log(Report.DEBUG, "Nouvelle position GPS");

		ajoutePosition(ici);
	}

	/***
	 * Ajoute une position pour tous les itineraires en cours d'enregistrement
	 * @param ici
	 */
	private void ajoutePosition(Location ici)
	{
		Context context = getApplicationContext();
		_report.log(Report.DEBUG, String.format("Lat:%.03f, Lg:%.03f, V:%.02f, T:%s", (float) ici.getLatitude(), (float) ici.getLongitude(), ici.getSpeed(), ici.getTime()));
		if (!GPSUtils.isBetterLocation(context, ici, dernierePosition))
			return;

		if (dernierePosition != null)
			if (ici.distanceTo(dernierePosition) < _minDistance)
			{
				// Distance trop petite depuis la derniere fois
				_report.log(Report.WARNING, "Position regetée: Distance trop courte");
				return;
			}

		// Ajouter cette position a toutes les randonnees qui enregistrent
		Position pos = new Position(ici);
		getVitesse(pos, dernierePosition);// Truc pour le cas ou la vitesse n'est pas supportee par le GPS
		ItinerairesDatabase database = ItinerairesDatabase.getInstance(context);
		try
		{
			// Ajouter cette position pour toutes les randonnees enregistrant
			Cursor c = database.getItinerairesIdEnregistrant();
			if (c != null)
			{
				final int colonne = c.getColumnIndex(DatabaseHelper.COLONNE_ITI_ID);
				while (c.moveToNext())
				{
					pos.IdRandonnee = c.getInt(colonne);
					_report.log(Report.DEBUG, "ajout d'une position pour itineraire id=" + pos.IdRandonnee);
					database.ajoute(pos);
				}
				c.close();
			}
		} catch (Exception e)
		{
			_report.log(Report.ERROR, "Erreur lors de l'ajout d'une nouvelle position");
			_report.log(Report.ERROR, e);
		}

		dernierePosition = ici;
		timeOutGPS(context);
	}

	/***
	 * Retrouve la vitesse sur la position, directement si supporté par le GPS, indirectement sinon
	 * @param loc
	 * @return
	 */
	private void getVitesse(@NonNull Location loc, @Nullable Location derniere)
	{
		if (loc.hasSpeed())
			return;

		if (derniere == null)
			return;

		double distance = loc.distanceTo(dernierePosition);
		double temps = loc.getTime() - dernierePosition.getTime();
		_report.log(Report.DEBUG, "Location ne supporte pas vitesse: " + distance + "m / " + temps + "s =" + (float) (distance / temps) + "m/s");

		loc.setSpeed((float)distance / (float)temps);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		String Status = status == LocationProvider.AVAILABLE ? "available" : status == LocationProvider.OUT_OF_SERVICE ? "out of service" : status == LocationProvider.TEMPORARILY_UNAVAILABLE ? "temporary unavailable" : ("inconnu " + status);
		_report.log(Report.DEBUG, "GPS onStatusChanged " + Status);
	}

	@Override
	public void onProviderEnabled(String provider)
	{
		_report.log(Report.DEBUG, "GPS onProviderEnabled: " + provider);
	}

	@Override
	public void onProviderDisabled(String provider)
	{
		_report.log(Report.DEBUG, "GPS onStatusDisabled: " + provider);
	}

	/**
	 * Rapporte une erreur detectee par le programme
	 *
	 * @param context
	 * @param e
	 */
	@SuppressWarnings("nls")
	public static void Erreur(Context context, @Nullable Exception e)
	{
		String message;//= ""; //$NON-NLS-1$
		if (e == null)
		{
			message = "null exception ?!?"; //$NON-NLS-1$
		}
		else
		{
			StringBuilder stack = new StringBuilder();

			StackTraceElement[] st = e.getStackTrace();
			for (int i = 0; i < st.length; i++)
			{
				String line = st[i].getMethodName() + ":" + st[i].getLineNumber(); //$NON-NLS-1$
				// Log.e(TAG, line);
				stack.append(line);
				stack.append("\n");
			}
			message = e.getLocalizedMessage() + "\n" + stack.toString(); //$NON-NLS-1$
		}

		Toast t = Toast.makeText(context, "Erreur\n" + message + "\n", Toast.LENGTH_LONG); //$NON-NLS-1$
		t.show();
	}

	/***
	 * Service detruit par le systeme
	 * On tente de le relancer si besoin
	 */
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		_report.log(Report.DEBUG, "Service.OnDestroy");
		if (ItinerairesDatabase.getInstance(this).getNbItinerairesEnregistrant() > 0)
			GPSRelaunchReceiver.restartService(this);
	}


}
