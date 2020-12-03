package com.lpi.trajets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lpi.trajets.database.ItinerairesDatabase;
import com.lpi.trajets.itineraire.Itineraire;
import com.lpi.trajets.itineraire.TypeItineraire;

public class EditItineraireActivity extends AppCompatActivity
{
	public static final int RESULT_EDIT_RANDO = 1;
	public static final String ACTION_EDIT_RANDO_FINISHED = EditItineraireActivity.class.getCanonicalName() + ".EDITERANDO";
	public static final String EXTRA_OPERATION = EditItineraireActivity.class.getCanonicalName() + ".OPERATION";
	public static final String EXTRA_OPERATION_AJOUTE = EditItineraireActivity.class.getCanonicalName() + ".AJOUTE";
	public static final String EXTRA_OPERATION_MODIFIE = EditItineraireActivity.class.getCanonicalName() + ".MODIFIE";
	private Spinner _spType;
	@Nullable
	private Itineraire _itineraire;
	@Nullable
	String _operation;
	EditText _eNom;

	public interface EditItineraireListener
	{
		void onModified(@NonNull Itineraire itineraire);
	}

	/***
	 * Interface utilisateur pour ajouter ou modifier un itineraire
	 * @param activity
	 * @param itineraire
	 * @param listener
	 */
	public static void modifieItineraire(@NonNull Activity activity, @NonNull final Itineraire itineraire, @NonNull EditItineraireListener listener)
	{
		final AlertDialog dialogBuilder = new AlertDialog.Builder(activity).create();

		LayoutInflater inflater = activity.getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.activity_edit_itineraire, null);

		EditText eNom = dialogView.findViewById(R.id.editTextNom);
		Spinner spType = dialogView.findViewById(R.id.spinnerTypeRando);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity, R.array.types_itineraires, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spType.setAdapter(adapter);

		if (itineraire != null)
		{
			eNom.setText(itineraire.Nom);
			spType.setSelection(TypeItineraire.typeToInt(itineraire.Type));
		}
		else
			eNom.setText( activity.getResources().getString(R.string.rando_sans_nom, ItinerairesDatabase.getInstance(activity).nbItineraires() + 1));

		Button bOk = dialogView.findViewById(R.id.buttonOk);
		bOk.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(final View view)
			{
				Itineraire modifie;
				if (itineraire == null)
					modifie = new Itineraire();
				else
					modifie = itineraire;
				modifie.Nom = eNom.getText().toString();
				modifie.Type = TypeItineraire.intToType(spType.getSelectedItemPosition());

//				if (displayError("".equals(itineraire.Nom), eNom, "Donnez un nom à votre randonée"))
//					erreur = true;
//
//				if (erreur)
//					return;

				listener.onModified(modifie);
				dialogBuilder.dismiss();
			}
		});
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Afficher la fenetre
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		dialogBuilder.setView(dialogView);
		dialogBuilder.show();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Demarre l'activity pour modifier un itineraire
	 * @param activity, l'activity qui va recevoir la notification de fin de l'edition
	 */
	////////////////////////////////////////////////////////////////////////////////////////////////
	public static void startForEdit(@NonNull final Activity activity, @Nullable final Itineraire itineraire)
	{
		if (itineraire != null)
		{
			Intent intent = new Intent(activity, EditItineraireActivity.class);
			Bundle b = new Bundle();
			itineraire.toBundle(b);
			b.putString(EditItineraireActivity.EXTRA_OPERATION, EditItineraireActivity.EXTRA_OPERATION_MODIFIE);
			intent.putExtras(b);
			activity.startActivityForResult(intent, RESULT_EDIT_RANDO);
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////

	/***
	 * Affiche une erreur en mettant l'accent sur le champ concerné
	 * @param error
	 * @param v
	 * @param message
	 * @return
	 */
	////////////////////////////////////////////////////////////////////////////////////////////////
	private boolean displayError(boolean error, @NonNull View v, @NonNull String message)
	{
		if (error)
		{
			if (v instanceof EditText)
			{
				((TextView) v).setError(message);
			}
			else
			{
				final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				com.lpi.trajets.MainActivity.MessageNotification(v, message);
			}
		}
		else
		{
			if (v instanceof TextView)
				((TextView) v).setError(null);
		}

		return error;
	}

}
