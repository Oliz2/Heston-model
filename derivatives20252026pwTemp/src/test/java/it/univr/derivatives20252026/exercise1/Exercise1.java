package it.univr.derivatives20252026.exercise1;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Assert;

import it.univr.derivatives.marketdataprovider.MarketDataProvider;
import it.univr.derivatives.utils.TimeSeries;
import net.finmath.fouriermethod.calibration.BoundConstraint;
import net.finmath.fouriermethod.calibration.CalibratedModel;
import net.finmath.fouriermethod.calibration.ScalarParameterInformationImplementation;
import net.finmath.fouriermethod.calibration.CalibratedModel.OptimizationResult;
import net.finmath.fouriermethod.calibration.models.CalibratableHestonModel;
import net.finmath.fouriermethod.products.smile.EuropeanOptionSmileByCarrMadan;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveInterpolation;
import net.finmath.marketdata.model.curves.CurveInterpolation.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationEntity;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationMethod;
import net.finmath.marketdata.model.volatilities.OptionSmileData;
import net.finmath.marketdata.model.volatilities.OptionSurfaceData;
import net.finmath.marketdata.model.volatilities.VolatilitySurface.QuotingConvention;
import net.finmath.modelling.descriptor.HestonModelDescriptor;
import net.finmath.optimizer.OptimizerFactory;
import net.finmath.optimizer.OptimizerFactoryLevenbergMarquardt;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar. DateRollConvention; 
import net.finmath.time.businessdaycalendar. BusinessdayCalendarExcludingTARGETHolidays;

/**
 * Exercise 1
 * 
 * Perform a daily recalibration of the Heston model and plot
 * 
 * A time series of the calibration error
 * 
 * A time series for each of the 5 parameters of the model
 * 
 * Discuss the stability of the estimates
 * 
 * Is the market practice of daily recalibration consistent with the assumptions of the model?
 */
public class Exercise1 {

	public static void main(String[] args) throws Exception {
		/*
		 * Load Market Data
		 */
		TreeMap<LocalDate, OptionSurfaceData> marketData = MarketDataProvider.getVolatilityDataContainer();
		
		//all available dates
		Set<LocalDate> keys = marketData.keySet();
		
		
			
		TimeSeries volatilityTimeSeries = new TimeSeries();
		TimeSeries thetaTimeSeries = new TimeSeries();
		TimeSeries kappaTimeSeries = new TimeSeries();
		TimeSeries xiTimeSeries = new TimeSeries();
		TimeSeries rhoTimeSeries = new TimeSeries();
		TimeSeries rmseTimeSeries = new TimeSeries();
		
		/*
		 * These are just initial guess to get the calibration started.
		 */
		/*
		 * The parameters we specify here do not have an impact on the starting point of the calibration.
		 * The true initial condition is fixed by optimizer factory.
		 *
		 */
		final double volatility = 0.15;
		
		/* V0: È il livello della varianza (volatilità al quadrato) nel momento attuale ($t=0$). 
		 * È il punto di partenza del processo stocastico della varianza.
		 */
		final double theta = 0.04;
		/*θ: Rappresenta il livello medio a cui la varianza Vt tende 
		 * a tornare nel tempo (la sua media di lungo periodo). Determina 
		 * l'altezza della Curva di Volatilità per le scadenze lunghe.
		 */
		final double kappa = 1.0;
		/* k: Determina la velocità con cui la varianza Vt si "aggancia" e 
		 * torna verso il suo livello di lungo termine θ. Un K
		 * alto implica che la varianza è molto stabile e torna velocemente alla sua media.
		 */
		final double xi = 0.5;
		/* ξ: Misura quanto il processo di varianza è volatile o "rumoroso". 
		 * Controlla la pendenza e lo spessore dello Smile di Volatilità per una data scadenza.
		 */
		
		final double rho = -0.5;
		/*
		 * Misura la correlazione tra gli shock casuali che influenzano il prezzo dell'asset St e 
		 * quelli che influenzano la sua varianza 
		 * Vt. È il parametro che modella l'asimmetria (o skew) 
		 * della volatilità: un ρ negativo (tipico delle azioni/indici) 
		 * fa sì che la volatilità aumenti quando il prezzo scende.
		 */

		
		
	
		/*
		 * decide when to stop with the experiment
		 */
		LocalDate limit = LocalDate.of(2006, 2, 2);
		
		/*
		 * main loop
		 */
		
		BusinessdayCalendar targetCalendar = new BusinessdayCalendarExcludingTARGETHolidays();

	
	
		final double[] currentParameters = new double[] { volatility, theta, kappa, xi, rho};
		final double[] parameterStep = new double[] { 0.001,0.001,0.001,0.001,0.001};
		
	
		
		int conteggio = 0; 
		for(LocalDate today : keys) {
	
		
			
		
			if(!targetCalendar.isBusinessday(today)) {
				System.out.println("Skipping buisness day" + today);
				continue;
			}
			
			
			System.out.println(today);
			if(today.isAfter(limit)) {
				break;
			}
			
			OptionSurfaceData data = marketData.get(today);
			
			/*tramite la curva surface io mi sto andando a prendere tutti i dati di cui io ho bosgno
			 * riga per riga.
			 */
			/*riesce a crearmi una curva dei prezzi scontata per per tutti i miei dati all'intern del file,
			 * in modo orizzontale qeuesta funzione mi prende tutti i miei dati e mi crea la curva di sconto per 
			 * ogni scadenza T.
			 * in modo verticale va a ripetermi il procedimento per un intero mese di dati
			 * 
			 * 
			 * 
			 * Recupera la Curva di Sconto (DC), che rappresenta i tassi risk-free
			 * (collateralizzati/OIS) per la data di osservazione (e.g., 02/02/2006).
			 *  Questa funzione ci fornisce la DC per l'intera STRUTTURA A TERMINE,
			 *  cioè per TUTTE le scadenze (tenor) delle opzioni presenti nel file,
			 *   e non solo per il primo mese.
			 *    Il processo si ripete, ottenendo una nuova DC per ogni giorno di analisi.
			 */
			
			/*per creare la curva dei foward non c'è un modo univoco come per la curva di sconto.
			 * in qeusto caso dovrò utilizare la seguente formula S0/DC(t) e ripeto qeusto procedimento
			 * per l'intero mesi di dati, dopodichè tramite la funaione equityfoward vado a interpolarizzare i miei 
			 * dati e creo la curva
			 */
			
			
			
			final DiscountCurve discountCurve = data.getDiscountCurve();
			final DiscountCurve equityFowardCurve = data.getEquityForwardCurve();

			final double initialValue = equityFowardCurve.getValue(0.0);
			/*in questo caso curveSpot deriva da data e data deriva da marketData cambia ogni giorno perchè è all'interno del ciclo
			 * for, qudini io otterò un prezzo inziale S0 doverso per ogni giorno di trading*/
			
			
			
			
			
			
			/* Questa funzioen serve essenzialmente per darmi la struttura del modello di Heston ceh poi successivamente andrò 
			 * acalibrare
			 */
			HestonModelDescriptor hestonModelDescription = new HestonModelDescriptor(
					today, 
					initialValue, 
					discountCurve,
					discountCurve,
					volatility, 
					theta, 
					kappa, 
					xi, 
					rho);
			/*
			 * In questo passaggio, il HestonModelDescriptor viene inizializzato con i parametri
			 * (volatility V0, theta, kappa, xi, rho) come "initial guess" (valori di partenza).
			 * * L'algoritmo di calibrazione (ottimizzazione) partirà da questi valori fissi
			 * e cercherà il set di parametri ottimale per minimizzare l'errore rispetto ai dati
			 * di mercato (la OptionSurfaceData) di "today".
			 * * Il processo si ripete giorno per giorno (verticalmente), garantendo che la calibrazione
			 * utilizzi i tassi e lo Spot S0 coerenti per la giornata di trading corrente.
			 */
			
			
			/* ScalarParameter mi serve per far capire al programma quale delle varibile devono essere calibrate e all'interno di quael range
			 * */
			final ScalarParameterInformationImplementation volatilityInformation = new ScalarParameterInformationImplementation(true, new BoundConstraint(0.01,1.0));
			final ScalarParameterInformationImplementation thetaInformation = new ScalarParameterInformationImplementation(true, new BoundConstraint(0.01,1.0));
			final ScalarParameterInformationImplementation kappaInformation = new ScalarParameterInformationImplementation(true, new BoundConstraint(0.01,20.0));
			final ScalarParameterInformationImplementation xiInformation = new ScalarParameterInformationImplementation(true, new BoundConstraint(0.01,5));
			final ScalarParameterInformationImplementation rhoInformation = new ScalarParameterInformationImplementation(true, new BoundConstraint(-0.99,0.99));
			
			final CalibratableHestonModel model = new CalibratableHestonModel( 
					hestonModelDescription,
					volatilityInformation,
					thetaInformation,
					kappaInformation,
					xiInformation,
					rhoInformation,
					false);
			
			/*Adesso andiamo a utilizzare una funzioen che ci servirà per ottimizzare il modello.
			 * L'algoritmo Levenberg-Marquardt ci peremtte di minizzare la differenza di prezzo tra prezzo second il modello
			 * e prezzo di mercato. In sostanza torva i 5 parametri ceh li rednon più aderenti all rraltà
			 */
		
			final OptimizerFactory optimizerFactory = new OptimizerFactoryLevenbergMarquardt(300 /* maxIterations */, 2 /* maxThreads */);
			/*MaxIteration: Definisce il numero massimo di iterazioni che l'algoritmo può eseguire per l'operazioen finale.
			 * il massimo numero di iterazioni è 300, se l'algoritmo non tova la solzuine ntro qeusti passi il risultato viene 
			 * definito non convergente.
			 */
			
			//final double[] intialParameter = new double[] {0.0423, 0.018, 0.8455, 0.4639, -0.4};
			/*L'array initialParameters è semplicemente una lista dei valori iniziali che l'algoritmo di 
			 * ottimizzazione utilizzerà come punto di partenza per la ricerca della soluzione ottimale.
			 */
			
			
			/*L'algoritmo LM deve sapere in quale direzione muoversi nello spazio dei parametri per minimizzare l'errore. Per farlo, 
			 * calcola la derivata parziale (il gradiente) della funzione di errore rispetto a 
			 * ciascun parametro, usando un metodo chiamato differenze finite.
			 */
			
			final double maturity = 105.0/252;
			final EuropeanOptionSmileByCarrMadan pricer = new EuropeanOptionSmileByCarrMadan(maturity, new double [] {initialValue});
			/*La classe EuropeanOption funge solo da punto di partenza per il nostro modello. il etodo ceh stiamo utilizzando
			 * è il FFT, che è il metodo più efficace per calocolare gli smile attraverso una funzioen caratteristica.
			 * quando il modello di calibrazione propone un nuovo set teorico di prezzi del nostro modello questo oggetto pricer 
			 * viene chiamto come punto di partenza per la creazione di qeusti parametri. 
			 */

			final CalibratedModel problem = new CalibratedModel(data, model, optimizerFactory, pricer , currentParameters,parameterStep);
			
			System.out.println("Calibration started");

			final long startMillis	= System.currentTimeMillis();
			final OptimizationResult result = problem.getCalibration();
			final long endMillis		= System.currentTimeMillis();

			final double calculationTime = ((endMillis-startMillis)/1000.0);

			System.out.println("Calibration completed in: " +calculationTime + " seconds");

			System.out.println("The solver required " + result.getIterations() + " iterations.");
			System.out.println("RMSQE " +result.getRootMeanSquaredError());
			
			
			final double rmse = result.getRootMeanSquaredError();
		
			final HestonModelDescriptor hestonDescriptor = (HestonModelDescriptor) result.getModel().getModelDescriptor();
		
			currentParameters[0] = hestonDescriptor.getVolatility();
			currentParameters[1]= hestonDescriptor.getTheta();
			currentParameters[2] = hestonDescriptor.getKappa();
			currentParameters[3]= hestonDescriptor.getXi();
			currentParameters[4]= hestonDescriptor.getRho();
			
		
			System.out.println(hestonDescriptor.getVolatility());
			System.out.println(hestonDescriptor.getTheta());
			System.out.println(hestonDescriptor.getKappa());
			System.out.println(hestonDescriptor.getXi());
			System.out.println(hestonDescriptor.getRho());
		
			
			final double volatilitySeries = hestonDescriptor.getVolatility();
		
			final double thetaSeries = hestonDescriptor.getTheta();
	
			final double kappaSeries = hestonDescriptor.getKappa();
		
			final double xiSeries = hestonDescriptor.getXi();
			
			final double rhoSeries = hestonDescriptor.getRho();
			
			
		
			volatilityTimeSeries.add(today, volatilitySeries);
			thetaTimeSeries.add(today, thetaSeries);
			kappaTimeSeries.add(today, kappaSeries);
			xiTimeSeries.add(today, xiSeries);
			rhoTimeSeries.add(today, rhoSeries);
			rmseTimeSeries.add(today, rmse);
			
			System.out.println("----------------------------------");
			
			
			
			ArrayList<String> errorsOverview = result.getCalibrationOutput();

			
			for(final String myString : errorsOverview) {
				System.out.println(myString);
			}
			
			
			Assert.assertTrue(result.getRootMeanSquaredError() < 1.0);
		
		volatilityTimeSeries.plot("VolatilityIndex");
		thetaTimeSeries.plot("ThetaIndex");
		kappaTimeSeries.plot("kappaIndex");
		xiTimeSeries.plot("xiIndex");
		rhoTimeSeries.plot("rhoIndex");
		rmseTimeSeries.plot("rmseTimeSeries");
		
	
			
			
		}
	}
}

		
		

	
	


