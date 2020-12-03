package com.lpi.trajets;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.lpi.trajets.database.ItinerairesDatabase;
import com.lpi.trajets.itineraire.Itineraire;
import com.lpi.trajets.itineraire.ItineraireAdapter;
import com.lpi.trajets.permissions.PermissionsActivity;
import com.lpi.trajets.preferences.PreferencesActivity;
import com.lpi.trajets.report.ReportActivity;
import com.lpi.trajets.report.Report;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
{
	private static final int RESULT_REQUEST_PERMISSIONS = 10;
	@Nullable
	private ItineraireAdapter _adapterItineraire;
	private int _currentItemSelected = 0;
	public static void SignaleErreur(final String message, final Exception e)
	{
		//Toast.makeText(context, "La base de donnée a été dumpée dans les traces", Toast.LENGTH_SHORT).show();
	}

	public static void MessageNotification(@NonNull View v, @NonNull String message)
	{
		Snackbar.make(v, message, Snackbar.LENGTH_LONG).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				ajouterItineraire();
			}
		});

		InitItineraires();

		PermissionsActivity.checkPermissions(this, R.array.permissions, R.array.description_permissions, RESULT_REQUEST_PERMISSIONS);

	}


	/***
	 * Initialisation de la liste des profils a partir de la base de donnees
	 */
	////////////////////////////////////////////////////////////////////////////////////////////////
	private void InitItineraires()
	{
		ListView listView = findViewById(R.id.listView);
		listView.setEmptyView(findViewById(R.id.textViewEmpty));

		_adapterItineraire = new ItineraireAdapter(this, ItinerairesDatabase.getInstance(this).getCursor());
		listView.setAdapter(_adapterItineraire);
		registerForContextMenu(listView);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id)
			{
				view.setSelected(true);
				_currentItemSelected = position;
				parent.showContextMenuForChild(view);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}


	/***
	 * Menu principal
	 * @param item
	 * @return
	 */
	////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id)
		{
			case R.id.action_settings:
				PreferencesActivity.start(this);
				break;

			case R.id.action_database:
				ItinerairesDatabase.getInstance(this).dump();
				Toast.makeText(this, "La base de donnée a été dumpée dans les traces", Toast.LENGTH_SHORT).show();
				break;

			case R.id.action_rapport:
				startActivity(new Intent(this, ReportActivity.class));
				break;

			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	/**
	 * Dispatch incoming result to the correct fragment.
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED)
			return;

		switch (requestCode)
		{
			case RESULT_REQUEST_PERMISSIONS:
				// Retour de demande d'autorisation au systeme, verifier qu'on a bien toutes les permissions requises, reouvrir
				// l'ecran si besoins
				PermissionsActivity.checkPermissions(this, R.array.permissions, R.array.description_permissions, RESULT_REQUEST_PERMISSIONS);
				break;

		}
	}


	@Override
	public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.listView)
		{
			if (_currentItemSelected != -1)
			{
				Itineraire selectedItem = ItinerairesDatabase.getInstance(this).getItineraire(_adapterItineraire.get(_currentItemSelected).Id);
				if (selectedItem != null)
				{
					MenuInflater inflater = getMenuInflater();
					inflater.inflate(R.menu.menu_liste, menu);
					menu.setHeaderTitle(selectedItem.Nom);

					boolean details = (!selectedItem.Enregistre) && (ItinerairesDatabase.getInstance(this).getNbPositions(selectedItem.Id) > 0);

					menu.findItem(R.id.action_modifier).setEnabled(!selectedItem.Enregistre);
					menu.findItem(R.id.action_supprimer).setEnabled(!selectedItem.Enregistre);
					menu.findItem(R.id.action_details).setEnabled(details);
					//menu.findItem(R.id.action_carte).setEnabled(details );
					menu.findItem(R.id.action_exporter).setEnabled(details);
				}
			}
		}
	}

	@Override
	/***
	 * Choix d'un item dans le menu contextuel
	 */
	public boolean onContextItemSelected(@NonNull MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_modifier:
				modifierItineraire();
				return true;
			case R.id.action_supprimer:
				Supprime();
				return true;

			case R.id.action_details:
				DetailsActivity.start(this, _adapterItineraire.get(_currentItemSelected));
				return true;

			case R.id.action_exporter:
				//TODO: ExportItineraire.start(this, _adapterRandos.get(_currentItemSelected).Id);
				return true;


			default:
				return super.onContextItemSelected(item);
		}
	}

	/***
	 * Modifier un itineraire
	 */
	private void modifierItineraire()
	{
		Itineraire itineraire = _adapterItineraire.get(_currentItemSelected);
		EditItineraireActivity.modifieItineraire(this, itineraire, new EditItineraireActivity.EditItineraireListener()
		{
			@Override public void onModified(@NonNull final Itineraire itineraire)
			{
				final ItinerairesDatabase database = ItinerairesDatabase.getInstance(MainActivity.this);
				database.modifie(itineraire);
				_adapterItineraire.changeCursor(database.getCursor());
			}
		});
	}

	/***
	 * Ajouter un itineraire
	 */
	private void ajouterItineraire()
	{
		EditItineraireActivity.modifieItineraire(this, null, new EditItineraireActivity.EditItineraireListener()
		{
			@Override public void onModified(@NonNull final Itineraire itineraire)
			{
				final ItinerairesDatabase database = ItinerairesDatabase.getInstance(MainActivity.this);

				// Ajouter le profil
				itineraire.DateCreation = (int) Calendar.getInstance().getTimeInMillis();
				database.ajoute(itineraire);
				_adapterItineraire.changeCursor(database.getCursor());
				_currentItemSelected = -1;

				Report.getInstance(MainActivity.this).historique("Ajout '" + itineraire.Nom + "', Creation: " + itineraire.DateCreation);
			}
		});
	}

	private void Supprime()
	{
		if (_currentItemSelected == -1)
			return;

		final Itineraire objetASupprimer = _adapterItineraire.get(_currentItemSelected);
		if (objetASupprimer != null)
		{
			final ItinerairesDatabase database = ItinerairesDatabase.getInstance(MainActivity.this);
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setTitle("Supprimer");
			dialog.setMessage(getResources().getString(R.string.supprimer_itineraire, objetASupprimer.Nom, database.getNbPositions(objetASupprimer.Id)));
			dialog.setCancelable(false);
			dialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(android.R.string.ok),
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int buttonId)
						{
							Report.getInstance(MainActivity.this).historique("Suppression d'itinéraire'" + objetASupprimer.Nom + ", id=" + objetASupprimer.Id);

							// Supprimer
							if (database.supprime(objetASupprimer.Id))
								MessageNotification(findViewById(R.id.listView), "Itinéraire " + objetASupprimer.Nom + " supprimé");
							else
								SignaleErreur("Erreur lors de la suppression de " + objetASupprimer.Nom + ", itinéraire non supprimé", null);
							_adapterItineraire.changeCursor(database.getCursor());
							_currentItemSelected = -1;
						}
					});
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(android.R.string.cancel),
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int buttonId)
						{
							// Ne rien faire
						}
					});
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			dialog.show();
		}
	}

	/**
	 * Dispatch onResume() to fragments.  Note that for better inter-operation
	 * with older versions of the platform, at the point of this call the
	 * fragments attached to the activity are <em>not</em> resumed.  This means
	 * that in some cases the previous state may still be saved, not allowing
	 * fragment transactions that modify the state.  To correctly interact
	 * with fragments in their proper state, you should instead override
	 * {@link #onResumeFragments()}.
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		_adapterItineraire.changeCursor(ItinerairesDatabase.getInstance(this).getCursor());
	}
}