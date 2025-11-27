package it.univr.derivatives20252026.exercise2;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

import it.univr.derivatives.marketdataprovider.MarketDataProvider;
import net.finmath.exception.CalculationException;
import net.finmath.fouriermethod.calibration.BoundConstraint;
import net.finmath.fouriermethod.calibration.CalibratedModel;
import net.finmath.fouriermethod.calibration.ScalarParameterInformationImplementation;
import net.finmath.fouriermethod.calibration.CalibratedModel.OptimizationResult;
import net.finmath.fouriermethod.calibration.models.CalibratableHestonModel;
import net.finmath.fouriermethod.models.CharacteristicFunctionModel;
import net.finmath.fouriermethod.models.HestonModel;
import net.finmath.fouriermethod.products.EuropeanOption;
import net.finmath.fouriermethod.products.smile.EuropeanOptionSmileByCarrMadan;
import net.finmath.integration.SimpsonRealIntegrator;
import net.finmath.integration.TrapezoidalRealIntegrator;
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
import net.finmath.optimizer.SolverException;

public class Exercise2 {

	public static void main(String[] args) throws SolverException, CalculationException {
		/*
		 * Application of the Carr Madan formula for the
		 * static replication of a Variance swap.
		 */
		
		
		
		
		//Step 1 Create a Heston model
		
		
		
		double volatility = 0.15;
		double theta = 0.04;
		double kappa = 1.0;
		double xi = 0.5;
		double rho = -0.5;
		
		
		final double[] currentParameters = new double[] { volatility, theta, kappa, xi, rho};
		final double[] parameterStep = new double[] { 0.001,0.001,0.001,0.001,0.001};
		
		LocalDate localDate = LocalDate.of(2006, 2, 2).plusMonths(1);
		TreeMap<LocalDate, OptionSurfaceData> marketData = MarketDataProvider.getVolatilityDataContainer();
		OptionSurfaceData data = marketData.get(localDate);
		
		DiscountCurve discountCurve = data.getDiscountCurve();
		
		
		final DiscountCurve curveSpot = data.getEquityForwardCurve();
		final double[] maturities = data.getMaturities();
		final double scalarFactor = 100;
		final double initialValue = curveSpot.getValue(0.0)/scalarFactor;//valori scalati
		final double originalValue = curveSpot.getValue(0.0);
		/*in questo caso curveSpot deriva da data e data deriva da marketData cambia ogni giorno perchè è all'interno del ciclo
		 * for, qudini io otterò un prezzo inziale S0 doverso per ogni giorno di trading*/
		 
		final double[] fowardMaturities = new double[maturities.length+1];
		fowardMaturities[0] = 0.0;
		System.arraycopy(maturities, 0, fowardMaturities, 1, maturities.length);
		final double[] fowardFactor = new double[maturities.length+1];
		

		
		for(int i = 0; i < maturities.length+1; i++) {
			
			final double t = fowardMaturities[i];
			
			if(t == 0) {
				fowardFactor[i] = initialValue; 
				
			}else {
			
				final double fowardValue = originalValue/discountCurve.getValue(t);
				final double scalarFowardValue = fowardValue/scalarFactor; //valori scalati
				fowardFactor[i] = scalarFowardValue;
			}
			
			
		}
		
		
		final ExtrapolationMethod exMethod = ExtrapolationMethod.CONSTANT;
		final InterpolationMethod intMethod = InterpolationMethod.LINEAR;
		final InterpolationEntity intEntity = InterpolationEntity.LOG_OF_VALUE;
		
		
		final DiscountCurve equityFowardCurve = DiscountCurveInterpolation.createDiscountCurveFromDiscountFactors(
				"daxForwardCurve", 
				fowardMaturities, 
				fowardFactor,
				intMethod, 
				exMethod,
				intEntity);
		
		final OptionSmileData[] allSmile = new OptionSmileData[maturities.length];
		
		
		
		for(int i = 0; i < maturities.length; i++) {
			
			OptionSmileData smiles = data.getSmile(maturities[i]);
			final double[] strikeOriginal = smiles.getStrikes();	
			
			final double[] strikeScalar = new double[strikeOriginal.length];
			final double[] valuesOriginal = new double[strikeOriginal.length];
			
			for(int j = 0; j < strikeOriginal.length; j++) {
				strikeScalar[j] = strikeOriginal[j]/scalarFactor;
				valuesOriginal[j] = data.getValue(maturities[i], strikeOriginal[j],QuotingConvention.VOLATILITYLOGNORMAL)/scalarFactor;//valore della volatilità scalato
				
		
			}
			
			
			allSmile[i] = new OptionSmileData(
					"Dax", 
					localDate, 
					strikeScalar, 
					smiles.getMaturity(), 
					valuesOriginal,
					QuotingConvention.VOLATILITYLOGNORMAL);
			
		}
		
		OptionSurfaceData surface = new OptionSurfaceData(allSmile, discountCurve, equityFowardCurve);
	
		HestonModelDescriptor hestonModelDescription = new HestonModelDescriptor(
				localDate, 
				initialValue, 
				discountCurve,
				discountCurve,
				volatility, 
				theta, 
				kappa, 
				xi, 
				rho);
		
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

		final OptimizerFactory optimizerFactory = new OptimizerFactoryLevenbergMarquardt(300 /* maxIterations */, 2 /* maxThreads */);
		
		final double maturityExercise1 = 105.0/252;
		final EuropeanOptionSmileByCarrMadan pricer = new EuropeanOptionSmileByCarrMadan(maturityExercise1, new double [] {initialValue});
		
		
		
		final CalibratedModel problem = new CalibratedModel(surface, model, optimizerFactory, pricer , currentParameters, parameterStep);
	
		/*
		 * Get the calibrated parameters
		 */
		
		final OptimizationResult result = problem.getCalibration();
		final HestonModelDescriptor calibratedHestonDescriptor = (HestonModelDescriptor) result.getModel().getModelDescriptor();
		
		volatility = calibratedHestonDescriptor.getVolatility();
		theta = calibratedHestonDescriptor.getTheta();
		kappa = calibratedHestonDescriptor.getKappa();
		xi = calibratedHestonDescriptor.getXi();
		rho = calibratedHestonDescriptor.getRho();
		
		
		/*
		 * Use the closed-form formula for the price of the variance swap
		 */
		final double maturity = 30.0/252;
		final double term1 = 1 - Math.exp(-(kappa*maturity));
		
		final double fairVarSwapRate = ((term1/(kappa*maturity))*(volatility-theta))+theta;
		
		System.out.println("The fair Variance Swap Rate is " + fairVarSwapRate);
		
		
		
		
		
		/*
		 * Second approach
		 * 
		 */
		
		
		
		/*
		TrapezoidalRealIntegrator integratorCalls2 = 
				new TrapezoidalRealIntegrator(daxForwardPrice*1.01, 4*daxForwardPrice, 300);*/
		
		
		
		/*DoubleUnaryOperator integrandCalls2 = x ->
		
		{
			try {
				return new net.finmath.fouriermethod.products.
						EuropeanOption(maturity, x)
						.getValue(modelHS)/(x*x);
			} catch (CalculationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0.0;
		};*/
		
	}

}
