# Heston Model Analysis: Calibration, Pricing, and Hedging

This project implements a comprehensive analysis of the **Heston Stochastic Volatility Model** applied to the DAX index. Developed as part of the "Derivatives" course (2025-2026), the software focuses on model calibration stability, static replication via Carr-Madan, and the empirical analysis of Delta Hedging errors in a stochastic volatility environment.

## 📌 Project Overview

The repository contains a Java-based implementation using the **Finmath** library to address three main quantitative finance problems:

1.  **Model Risk & Recalibration:** Analyzing the stability of Heston parameters over time.
2.  **Pricing:** Implementing the Carr-Madan static replication formula.
3.  **Hedging Strategy:** Simulating dynamic Delta Hedging and quantifying the hedging error caused by stochastic volatility.

## 🚀 Key Features

### 1. Heston Model Calibration
* **Objective:** Calibrate the Heston model parameters ($\nu_0, \theta, \kappa, \xi, \rho$) to market data.
* **Data:** DAX Index Implied Volatility Surfaces (Time series: 2006–2009).
* **Methodology:** Minimization of the squared error between model prices and market prices.
* **Output:** Analysis of parameter evolution and RMSE (Root Mean Square Error) over the specified time window to assess model stability.

### 2. Static Replication (Carr-Madan)
* **Objective:** Price European Call options using the Fast Fourier Transform (FFT) approach.
* **Methodology:** Implementation of the **Carr-Madan formula**, utilizing the characteristic function of the Heston Model with parameters calibrated in step 1.

### 3. Dynamic Delta Hedging
* **Objective:** Analyze the P&L of a Delta Hedging strategy under stochastic volatility conditions.
* **Scenario:**
    * **Underlying:** DAX Index.
    * **Instrument:** European Call Option (Maturity: 2 months, Moneyness: 95%).
    * **Start Date:** Jan 2, 2006.
* **Analysis:** The strategy performs daily rebalancing of the delta. Since the model accounts for stochastic volatility but the hedging strategy only covers the Delta (ignoring Vega/Vol risk), the project quantifies the resulting **Hedging Error** at maturity.

## 🛠️ Technology Stack

* **Language:** Java (JDK 11+)
* **Build Tool:** Maven
* **Quantitative Library:** [Finmath Lib](https://finmath.net/) (finmath-lib)
* **Data Processing:** Apache POI (for parsing Excel market data)
* **Additional Libs:** OpenGamma Strata, Commons Math

## 📂 Project Structure

```text
src/
├── main/
│   ├── java/it/univr/derivatives/
│   │   ├── calibration/      # Calibration logic and parameter extraction
│   │   ├── pricing/          # Carr-Madan implementation
│   │   ├── hedging/          # Delta hedging simulation and P&L calculation
│   │   └── marketdata/       # Data import from Excel (Apache POI)
│   └── resources/            # Market data files (Dax_Bloomi.xls)
└── test/                     # Unit tests
