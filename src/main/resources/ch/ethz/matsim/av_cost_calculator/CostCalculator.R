#################################################################################################
# About this Code
#################################################################################################
# 
# This script reads the vehicle properties for both public transportation and fleet vehicles as well
# as the parameters defined in the "Parameters" tab of the Excel sheet. Based on the input, this
# script calculates the totals of each cost component for the given scenario using a separate function
# for each cost component. The individual functions are called and the results are combined to form a 
# data frame row by the main function at the end of the script. 
# 
# Following the different structure of operational models and available sources, line-based public
# transportation was again treated separately from other fleet and private vehicles.
# 
# The script returns a data frame with a single row. Each column presents the total of a cost
# component for the given scenario.
# 
#################################################################################################



#################################################################################################
# Read Excel input

vehicleCost <- na.omit(as.data.frame(read_excel("Input.xlsx","Vehicles", col_names=TRUE)))
ptCost <- na.omit(as.data.frame(read_excel("Input.xlsx","PT", col_names=TRUE)))
parameters <- na.omit(as.data.frame(read_excel("Input.xlsx","Parameters", col_names=TRUE)))

ptCost$Comment <- NULL
parameters$Comment <- NULL



#################################################################################################
#################################################################################################
# In the following there will be different functions for each cost component
#################################################################################################


#################################################################################################
# Interest

interestsum <- function(acquisition,interest=0.04,years=3,payfreq=1){
  q <- 1+interest/payfreq
  ann<-acquisition*(q)^(years*payfreq)*(q-1)/((q)^(years*payfreq)-1)
  
  return(ann*years*payfreq-acquisition)
}



#################################################################################################
# Fixed vehicle cost

fixedVehicleCostCalculator <- function(vehType, elec, autom, fleet, trips){
  
  cleaningPrice <- parameters[parameters$Name == "cleaningPrice_CHF",]$Value
  frequencyCleaning_priv <- parameters[parameters$Name == "frequencyCleaning_priv_Y",]$Value
  frequencyCleaning_prof_conv <- parameters[parameters$Name == "frequencyCleaning_prof_conv_Y",]$Value
  frequencyCleaning_prof_av <- parameters[parameters$Name == "frequencyCleaning_prof_av_PerTrip",]$Value
  
  
  # get vehicle cost structure from Excel readin
  t.vehicleCost <- vehicleCost[vehicleCost$Type == vehType,]
  
  # include effects of automation and electrification
  if (elec) {
      t.vehicleCost[3:dim(t.vehicleCost)[2]] <-
          t.vehicleCost[3:dim(t.vehicleCost)[2]] *
          (1+vehicleCost[vehicleCost$Type == "electric",3:dim(t.vehicleCost)[2]])
  }
  if (autom) {
      t.vehicleCost[3:dim(t.vehicleCost)[2]] <-
          t.vehicleCost[3:dim(t.vehicleCost)[2]] *
          (1+vehicleCost[vehicleCost$Type == "automated",3:dim(t.vehicleCost)[2]])
  }
   if (fleet) {
        #t.vehicleCost[3:dim(t.vehicleCost)[2]] <-
         ## t.vehicleCost[3:dim(t.vehicleCost)[2]] *
          #(1+vehicleCost[vehicleCost$Type == "fleet",3:dim(t.vehicleCost)[2]])
      
      vatmulti <- 1/(1+parameters[parameters$Name == "vat",]$Value)*vehicleCost[vehicleCost$Type=="vat_deductible",3:ncol(t.vehicleCost)]
      vatmulti[vatmulti==0] <- 1
      t.vehicleCost[3:dim(t.vehicleCost)[2]] <-
        t.vehicleCost[3:dim(t.vehicleCost)[2]] *
        (1+vehicleCost[vehicleCost$Type == "fleet",3:dim(t.vehicleCost)[2]])*vatmulti
  }
  
  # fixed capital costs
  if (fleet) {
      acq <- 0
      int <- 0      
      
  } else {
      # annual depreciation
      acq <- t.vehicleCost$Acquisition_L / parameters[parameters$Name == "VehicleLifetime_priv_Y",]$Value
      # annual interest
      int <- interestsum(t.vehicleCost$Acquisition_L,
                         interest=parameters[parameters$Name == "Interest_priv",]$Value,
                         years=parameters[parameters$Name == "Creditperiod_Y_priv",]$Value,
                         payfreq=12) / 
              parameters[parameters$Name == "VehicleLifetime_priv_Y",]$Value
      
  }
  
  # fixed cleaning costs
  cleaningCost = cleaningPrice
  if (!fleet) { # this means private
      cleaningCost = cleaningCost * frequencyCleaning_priv
  } else if (!autom) { # this means conventional
      cleaningCost = cleaningCost * frequencyCleaning_prof_conv
  } else { # this means autonomous
      cleaningCost = cleaningCost * frequencyCleaning_prof_av * trips * 365.25
  }
  
  # return values
  fixCost <- as.matrix(c(
      acq,
      int,
      t.vehicleCost$Insurance_Y,
      t.vehicleCost$Tax_Y,
      t.vehicleCost$Parking_Y,
      cleaningCost,
      t.vehicleCost$Other_Y)
      / 365.25)
  row.names(fixCost) <- c(
      "acquisition_fix",
      "interest_fix",
      "insurance",
      "tax",
      "parking",
      "cleaning",
      "other_fix")
  
   ##############################################
   # For debugging:
   # print(paste("Acquisition =", acq / 365.25))
   # print(paste("Interest =", int / 365.25))
   # print(paste("Insurance =", t.vehicleCost$Insurance_Y / 365.25))
   # print(paste("Tax =", t.vehicleCost$Tax_Y / 365.25))
   # print(paste("Parking =", t.vehicleCost$Parking_Y / 365.25))
   # print(paste("Other =", t.vehicleCost$Other_Y / 365.25))
   ##############################################
  
  totCost <- fixCost
  return(totCost)
}



#################################################################################################
# Variable vehicle cost

variableVehicleCostCalculator <- function(vehType, dailyKM, elec, autom, fleet){
  
  # get vehicle cost structure from excel readin
  t.vehicleCost <- vehicleCost[vehicleCost$Type == vehType,]

  # for fleet operators: substract deductible VAT from the prices
  vat = parameters[parameters$Name == "vat",]$Value

  # include effects of automation and electrification
  if (elec) {
      t.vehicleCost[3:dim(t.vehicleCost)[2]] <-
          t.vehicleCost[3:dim(t.vehicleCost)[2]] *
          (1+vehicleCost[vehicleCost$Type == "electric",3:dim(t.vehicleCost)[2]])
  }
  if (autom) {
      t.vehicleCost[3:dim(t.vehicleCost)[2]] <-
          t.vehicleCost[3:dim(t.vehicleCost)[2]] *
          (1+vehicleCost[vehicleCost$Type == "automated",3:dim(t.vehicleCost)[2]])
  }
  if (fleet) {
      vatmulti <- 1/(1+parameters[parameters$Name == "vat",]$Value)*vehicleCost[vehicleCost$Type=="vat_deductible",3:ncol(t.vehicleCost)]
      vatmulti[vatmulti==0] <- 1
      t.vehicleCost[3:dim(t.vehicleCost)[2]] <-
          t.vehicleCost[3:dim(t.vehicleCost)[2]] *
          (1+vehicleCost[vehicleCost$Type == "fleet",3:dim(t.vehicleCost)[2]])*vatmulti
  }
  
  # variable capital costs
  if (fleet) {
      dep <- 0
      # depreciation by distance
      acq <- t.vehicleCost$Acquisition_L / parameters[parameters$Name == "VehicleLifetime_prof_KM",]$Value
      int <- interestsum(t.vehicleCost$Acquisition_L,
                         interest=parameters[parameters$Name == "Interest_comm",]$Value,
                         years=parameters[parameters$Name == "Creditperiod_Y_comm",]$Value,
                         payfreq=1) / 
      parameters[parameters$Name == "VehicleLifetime_prof_KM",]$Value
      
  } else {
      # Use and scale TCS assumptions by vehicle price: CHF 0.07/km for a vehicle with price CHF 35'000.-
      dep <- t.vehicleCost$Acquisition_L / parameters[parameters$Name == "referencePriceMidsizeCar_CHF",]$Value * parameters[parameters$Name == "variableDeprecationMidsizeCar_priv_KM",]$Value
      acq <- 0
      int <- 0
  }
  
  # return values
  varCost <- as.matrix(c(
        acq,
        int,
        dep,
        t.vehicleCost$Maintenance_KM,
        t.vehicleCost$Tires_KM,
        t.vehicleCost$Fuel_KM,
        t.vehicleCost$Other_KM))
  row.names(varCost) <- c(
        "acquisition_var",
        "interest_var",
        "deprecation",
        "maintenance",
        "tires",
        "fuel",
        "other_var")

   ##############################################
   # For debugging:
   # print(paste("Acquisition =", acq))
   # print(paste("Deprecation =",  t.vehicleCost$Acquisition_L / 35000 * 0.07))
   # print(paste("Maintenance =", t.vehicleCost$Maintenance_KM))
   # print(paste("Tires =", t.vehicleCost$Tires_KM))
   # print(paste("Fuel =", t.vehicleCost$Fuel_KM))
   # print(paste("Other =", t.vehicleCost$Other_KM))
   ##############################################
  
  totCost <- varCost * dailyKM
  return(totCost)
}



#################################################################################################
# Service cost (i.e. labor cost)

salaryCostCalculator <- function(fleetSize, ophours, autom, fleet){
  
  driverSalary <- parameters[parameters$Name == "driverSalary_h",]$Value
  
  if (!autom & fleet) {
    totCost <- ophours * driverSalary
  } else {
    totCost <- 0
  }
  
  totCost <- as.matrix(totCost)
  row.names(totCost) <- c("salaries")
  return(totCost)
}



#################################################################################################
# Overhead cost

overheadCostCalculator <- function(fleetSize, fleet){

    overheadCost <- parameters[parameters$Name == "fleetOverhead_veh_d",]$Value # Overhead per vehicle
    operationsManagementCost_veh_d <- parameters[parameters$Name == "operationsManagementCost_veh_d",]$Value 
    fleetDefinitionSize <- parameters[parameters$Name == "fleetDefinitionSize",]$Value 
    
    
    totCost <- 0
    if (fleet) {
        if (fleetSize < fleetDefinitionSize) {
            stop("Please check fleet size.")
        } else {
            totCost <- overheadCost + operationsManagementCost_veh_d
        }
    }
    
    totCost <- as.matrix(totCost)
    row.names(totCost) <- c("overhead")
    return(totCost)
}



#################################################################################################
# Main Cost Calculator for non-PT

mainCalculator <- function(fleetSize, vehType, dailyKM, elec, autom, fleet, ophours, trips){
  
  totCost <- rbind(overheadCostCalculator(fleetSize, fleet),
                   salaryCostCalculator(fleetSize, ophours, autom, fleet),
                   variableVehicleCostCalculator(vehType = vehType,dailyKM =  dailyKM,elec =  elec,autom =  autom,fleet =  fleet),
                   fixedVehicleCostCalculator(vehType, elec, autom, fleet, trips))
    
  # print(paste("variable Cost =", variableVehicleCostCalculator(vehType, dailyKM, elec, autom, fleet)))
  # print(paste("fixed Cost =", fixedVehicleCostCalculator(vehType, elec, autom, fleet)))
    
  totCost <- t(totCost)
  return(totCost)
}



#################################################################################################
# Main Cost Calculator for PT

ptVehilceCostCalculator <- function(ptType, dailyKM, elec, autom){
    
    t.ptCost <- ptCost[ptCost$Type == ptType,]
    
    if(elec){ t.ptCost$Variable <-  t.ptCost$Variable * (1 + t.ptCost$electric) }
    if(autom){ t.ptCost$Variable <-  t.ptCost$Variable * (1 + t.ptCost$automated) }
    
    totCost <- t.ptCost$Variable
    
    return(totCost)
}



