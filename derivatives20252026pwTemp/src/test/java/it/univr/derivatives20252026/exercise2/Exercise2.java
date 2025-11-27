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
		
		
		final DiscountCurve discountCurve = data.getDiscountCurve();
		final DiscountCurve equityFowardCurve = data.getEquityForwardCurve();

		final double initialValue = equityFowardCurve.getValue(0.0);

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
		final double maturity = 30.0/252;
		final EuropeanOptionSmileByCarrMadan pricer = new EuropeanOptionSmileByCarrMadan(maturity, new double [] {initialValue});
		final CalibratedModel problem = new CalibratedModel(data, model, optimizerFactory, pricer , currentParameters,parameterStep);
		

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
