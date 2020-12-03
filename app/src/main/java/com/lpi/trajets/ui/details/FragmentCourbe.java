package com.lpi.trajets.ui.details;

import androidx.fragment.app.Fragment;

import com.lpi.trajets.courbes.HistogramView;

class FragmentCourbe extends Fragment
{
	public static final float MS_TO_KMH = 1.0f / 3.6f; // Multiplier les ms par 3.6 pour obtenir des km/h
	public static final float KMH_TO_MS = 3.6f; // Multiplier les ms par 3.6 pour obtenir des km/h

	/***
	 * Calcule un axe de vitesses entre vitesse min et vitesse max
	 * @param vitesseMin en m/s
	 * @param vitesseMax en m/s
	 */
	protected HistogramView.Axe calculeAxeVitesse(float vitesseMin, float vitesseMax)
	{
		HistogramView.Axe axe = new HistogramView.Axe();
		axe.min = vitesseMin;
		axe.max = vitesseMax;

		final float ecartKmh = (vitesseMax - vitesseMin) * MS_TO_KMH;
		float longueurLabels;
		int nbLabels;
		float tailleBarreau;
		String format;

		if (ecartKmh > 100)
		{
			nbLabels = (int) (ecartKmh / 10); // Un label tous les 10 kmh
			longueurLabels = 10;
			tailleBarreau = 10 * KMH_TO_MS;
			format = "%1$.0fkm/h";
		}
		else if (ecartKmh > 20)
		{
			nbLabels = (int) (ecartKmh / 5); // Un label tous les 5 kmh
			longueurLabels = 5;
			tailleBarreau = 5 * KMH_TO_MS;
			format = "%1$.0fkm/h";
		}
		else  if (ecartKmh > 10)
		{
			nbLabels = (int) (ecartKmh / 1); // Un label tous les 5 kmh
			longueurLabels = 1;
			tailleBarreau = 1 * KMH_TO_MS;
			format = "%1$.0fkm/h";
		}
		else if (ecartKmh > 1)
		{
			nbLabels = (int) (ecartKmh / 1); // Un label tous les 1 kmh
			longueurLabels = 1;
			tailleBarreau = 1 * KMH_TO_MS;
			format = "%1$.0fkm/h";
		}
		else if (ecartKmh > 0.1f)
		{
			nbLabels = (int) (ecartKmh / 0.1f); // Un label tous les 1 kmh
			longueurLabels = 0.1f;
			tailleBarreau = 0.1f * KMH_TO_MS;
			format = "%1$.2fkm/h";
		}
		else
		{
			nbLabels = (int) (ecartKmh / 1); // Un label tous les 0.1 kmh
			longueurLabels = 1;
			tailleBarreau = 1 * KMH_TO_MS;
			format = "%1$.2fkm/h";
		}

		axe.labels = new HistogramView.LabelAxe[nbLabels];
		float valeur = vitesseMin * MS_TO_KMH;
		for (int i = 0; i < nbLabels; i++)
		{
			axe.labels[i] = new HistogramView.LabelAxe();
			axe.labels[i].valeur = axe.min + (tailleBarreau * i);
			axe.labels[i].label = String.format(format, valeur);

			valeur += longueurLabels;
		}
		return axe;
	}

	/***
	 * Calcule un axe du temps entre deux temps
	 * @param debut
	 * @param fin
	 * @return
	 */
	protected HistogramView.Axe calculeAxeTemps(long debut, long fin)
	{
		HistogramView.Axe axe = new HistogramView.Axe();
		axe.min = debut;
		axe.max = fin;

		long duree = (fin - debut) / 1000; // Calcul en secondes
		int nbLabels = 0;
		float longueurLabels = 0;
		float tailleBarreau;
		String format;
		if (duree < 60)
		{
			// Moins d'une minute
			nbLabels = (int) (duree / 5); // Un label toutes les 5 secondes
			longueurLabels = 5;
			tailleBarreau = 5 * 1000; // les temps sont en millisecondes
			format = "%d\"";
		}
		else if (duree < 60 * 5)
		{
			// Moins de 5 minutes
			nbLabels = (int) (duree / 30); // Un label toutes les 30 secondes
			longueurLabels = 30;
			tailleBarreau = 30 * 1000; // les temps sont en millisecondes
			format = "%d\"";
		}
		else if (duree < 60 * 30)
		{
			// Moins d'une demi heure
			nbLabels = (int) (duree / 60); // Un label toutes les minutes
			longueurLabels = 1;
			tailleBarreau = 60 * 1000; // les temps sont en millisecondes
			format = "%d'";
		}
		else if (duree < 60 * 60 * 2)
		{
			// Moins de deux heures
			nbLabels = (int) (duree / (60 * 5)); // Un label toutes les 5 minutes
			longueurLabels = 5;
			tailleBarreau = 60 * 5 * 1000; // les temps sont en millisecondes
			format = "%d'";
		}
		else
		{
			// Plus de 2 heures
			nbLabels = (int) (duree / (60 * 15)); // Un label toutes les 15 minutes
			longueurLabels = 15;
			tailleBarreau = 60 * 15 * 1000; // les temps sont en millisecondes
			format = "%d'";
		}

		axe.labels = new HistogramView.LabelAxe[nbLabels];
		int valeur = 0;
		for (int i = 0; i < nbLabels; i++)
		{
			axe.labels[i] = new HistogramView.LabelAxe();
			axe.labels[i].valeur = axe.min + (tailleBarreau * i);
			axe.labels[i].label = String.format(format, valeur);

			valeur += longueurLabels;
		}

		return axe;
	}

	/**
	 * Calcule un axe pour des distances
	 * @param minMetres distance min en metres
	 * @param maxMetres distance max en metres
	 */
	protected HistogramView.Axe calculeAxeDistance(float minMetres, float maxMetres)
	{
		HistogramView.Axe axe = new HistogramView.Axe();
		axe.min = minMetres;
		axe.max = maxMetres;

		float distanceEnMetres = maxMetres - minMetres; // Calcul en secondes
		int nbLabels = 0;
		float longueurLabels = 0;
		float tailleBarreau;
		String format;
		if (distanceEnMetres < 10)
		{
			// Moins de 10 metres
			nbLabels = (int) (distanceEnMetres / 1); // Un label tous les metres
			longueurLabels = 1;
			tailleBarreau = 1;
			format = "%1$.0fm";
		}
		else if (distanceEnMetres < 100)
		{
			// Moins de 100 metres
			nbLabels = (int) (distanceEnMetres / 10); // Un label tous les 10 metres
			longueurLabels = 10;
			tailleBarreau = 10;
			format = "%1$.0fm";
		}
		else if (distanceEnMetres < 1000)
		{
			// Moins de 1 km
			nbLabels = (int) (distanceEnMetres / 100); // Un label tous les 100 metres
			longueurLabels = 100;
			tailleBarreau = 100;
			format = "%1$.0fm";
		}
		else if (distanceEnMetres < 10000)
		{
			// Moins de 10 km
			nbLabels = (int) (distanceEnMetres / 500); // Un label tous les 0.5 km
			longueurLabels = 0.5f;
			tailleBarreau = 500 ;
			format = "%1$.1fkm";
		}
		else
		if (distanceEnMetres < 100000)
		{
			// Moins de 100 km
			nbLabels = (int) (distanceEnMetres / 10000); // Un label tous les 10 km
			longueurLabels = 10;
			tailleBarreau = 10000 ;
			format = "%1$.0fkm";
		}
		else
		{
			// Moins de 100 km
			nbLabels = (int) (distanceEnMetres / 50000); // Un label tous les 50 km
			longueurLabels = 50;
			tailleBarreau = 50000 ;
			format = "%1$.0fkm";
		}

		nbLabels++;
		axe.labels = new HistogramView.LabelAxe[nbLabels];
		float valeur = minMetres;
		for (int i = 0; i < nbLabels; i++)
		{
			axe.labels[i] = new HistogramView.LabelAxe();
			axe.labels[i].valeur = axe.min + (tailleBarreau * i);
			axe.labels[i].label = String.format(format, valeur);

			valeur += longueurLabels;
		}
		return axe;
	}
}
