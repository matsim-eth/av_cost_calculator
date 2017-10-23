#################################################################################################
# About this Code
#################################################################################################
# 
# In the following, public transportation (i.e. line-based buses and trains) are treated separately
# from fleets of taxis or private vehicles. This is due to both the substantial differences in the
# operational models and the structure of the data available for this analysis.
# 
# The function scenarioAnalyzer at the bottom of this script determines the corresponding treatment
# based on the vehicle type. Vehicle types listed in the "PT" tab of the Excel sheet are considered
# to belong to a form of public transportation, while vehicle types listed in the "Vehicles" tab are
# treated as fleet or private vehicles.
# 
# It is important to note that the CostCalculator.R only returns total values for each cost type for
# the given scenario. This means that any differentiation of cost types (cost per vehicle, cost per 
# passenger kilometer, cost per day, price per ...) have to be defined and calculated in this script.
# 
#################################################################################################



#################################################################################################
# create (empty) results data frame

createResDF <- function(n){
  nms <- c("TotalCost", "TotalCostPerVeh", "CostPerVehKM", "CostPerSeatKM", "CostPerPassKM", "PricePerPassKM",
           "overhead", "salaries", "acquisition_var", "deprecation", "maintenance", "cleaning", "tires", "fuel",
           "other_var", "acquisition_fix", "interest", "insurance", "tax", "parking", "other_fix", "acquisition","interest_var","interest_fix")
  resDF <- matrix(ncol = length(nms), nrow = n)
  resDF <- as.data.frame(resDF)
  colnames(resDF) <- nms
  return(resDF)
}


#################################################################################################
# Evaluate non-PT Scenarios

nonptScenarioAnalyzer <- function(t.scenario){
  
  resDF  <- createResDF(1)
  t.scenario[,colnames(t.scenario) %in% nonDouble == FALSE] <- as.double(as.character(t.scenario[,colnames(t.scenario) %in% nonDouble == FALSE]))
  
  # For autonomous vehicles, extended hours of operation can be defined in the excel sheet.
  # They override the (default) operation hours for conventional vehicles.
  if (t.scenario$automated) { 
    t.scenario$ph_operationHours <- t.scenario$ph_operationHours_av
    t.scenario$oph_operationHours <- t.scenario$oph_operationHours_av
    t.scenario$ngt_operationHours <- t.scenario$ngt_operationHours_av
  }
  
  # fleet attributes
  t.nveh <- t.scenario$FleetSize
  t.fleet <- t.scenario$fleetOperation
  
  # vehicle attributes
  t.type <- t.scenario$VehicleType
  t.cap <- vehicleCost[vehicleCost$Type == t.type,]$Capacity
  t.elec <- t.scenario$electric
  t.auto <- t.scenario$automated
  
  # define distances
  t.ph_KM <- (t.scenario$ph_operationHours * t.scenario$ph_relActiveTime * t.scenario$ph_avSpeed
                * (1 + t.scenario$ph_relEmptyRides)
                * (1 + t.scenario$ph_relMaintenanceRides))
  t.oph_KM <- (t.scenario$oph_operationHours * t.scenario$oph_relActiveTime * t.scenario$oph_avSpeed
                * (1 + t.scenario$oph_relEmptyRides)
                * (1 + t.scenario$oph_relMaintenanceRides))
  t.ngt_KM <- (t.scenario$ngt_operationHours * t.scenario$ngt_relActiveTime * t.scenario$ngt_avSpeed
                * (1 + t.scenario$ngt_relEmptyRides)
                * (1 + t.scenario$ngt_relMaintenanceRides))
  t.totKM <- sum(c(t.ph_KM, t.oph_KM, t.ngt_KM), na.rm=TRUE)
  t.ph_passKM <- t.ph_KM * t.scenario$ph_avOccupancy * t.cap
  t.oph_passKM <- t.oph_KM * t.scenario$oph_avOccupancy * t.cap
  t.ngt_passKM <- t.ngt_KM * t.scenario$ngt_avOccupancy * t.cap
  t.passKM <- sum(c(t.ph_passKM, t.oph_passKM, t.ngt_passKM), na.rm=TRUE)
  
  # number of trips
  t.trips <- sum(c(t.ph_passKM / t.scenario$ph_avTripLengthPass,
                   t.oph_passKM / t.scenario$oph_avTripLengthPass,
                   t.ngt_passKM / t.scenario$ngt_avTripLengthPass), na.rm = TRUE)
  
  # define hours of operation and active hours
  t.opshours <- (t.scenario$ph_operationHours
                 + t.scenario$oph_operationHours
                 + t.scenario$ngt_operationHours)
  t.acthours <- (t.scenario$ph_operationHours * t.scenario$ph_relActiveTime
                 + t.scenario$oph_operationHours * t.scenario$oph_relActiveTime
                 + t.scenario$ngt_operationHours * t.scenario$ngt_relActiveTime)
  
  # Calculate costs using CostCalculator.R
  t.costStruct_Veh <- mainCalculator(fleetSize = t.nveh,vehType =  t.type,dailyKM =  t.totKM,elec =  t.elec,autom =  t.auto,fleet =  t.fleet,ophours =  t.opshours,trips =  t.trips)
  t.totalCost_Veh <- sum(t.costStruct_Veh)
  t.costStruct_Pass <- t.costStruct_Veh / t.passKM
  
  # Derive the desired cost types
  # BE AWARE: in this setup, the individual cost components (acquisition, maintenance, fuel, ...) are given per passenger km (not per vehicle km)
  for(nm in colnames(t.costStruct_Pass)){
      resDF[1,nm] <- t.costStruct_Pass[1,nm]
      }
  resDF$TotalCost <- t.totalCost_Veh * t.nveh
  resDF$TotalCostPerVeh <- t.totalCost_Veh
  resDF$CostPerVehKM <- t.totalCost_Veh / t.totKM
  resDF$CostPerSeatKM <- resDF$CostPerVehKM / t.cap
  resDF$CostPerPassKM <- t.totalCost_Veh / t.passKM
  resDF$PricePerPassKM <- resDF$CostPerPassKM / (1-parameters[parameters$Name == "yield_on_sales",]$Value) * (1+parameters[parameters$Name == "vat",]$Value)
    
  resDF$acquisition <- resDF$acquisition_var + resDF$acquisition_fix
  resDF$interest <- resDF$interest_var + resDF$interest_fix

    
  rm(list=ls(pattern="^t."))
  return(resDF)
}


#################################################################################################
# Evaluate PT Scenarios

ptScenarioAnalyzer <- function(t.scenario){
  
  resDF <- createResDF(1)
  t.scenario[,colnames(t.scenario) %in% nonDouble == FALSE] <- as.double(as.character(t.scenario[,colnames(t.scenario) %in% nonDouble == FALSE]))
  
  # fleet attributes
    # the cost structure of pt is already provided per vehicle ergo no fleet specifications required
  
  # vehicle attributes
  t.type <- t.scenario$VehicleType
  t.cap <- ptCost[ptCost$Type == t.type,]$Capacity
  t.elec <- t.scenario$electric
  t.auto <- t.scenario$automated
  
  # define distances
  t.totKM <- (t.scenario$ph_operationHours * t.scenario$ph_relActiveTime * t.scenario$ph_avSpeed
           * (1 + t.scenario$ph_relEmptyRides)
           * (1 + t.scenario$ph_relMaintenanceRides))
  t.passKM <- t.totKM * t.scenario$ph_avOccupancy * t.cap
  
  # define hours of operation and active hours
  t.opshours <- t.scenario$ph_operationHours
  t.acthours <- t.scenario$ph_operationHours * t.scenario$ph_relActiveTime 
  
  # Calculate costs using CostCalculator.R
  t.totalCost_vehKM <- ptVehilceCostCalculator(t.type, t.totKM, t.elec, t.auto)
  t.totalCost_Veh <- t.totalCost_vehKM * t.totKM

  # Derive the desired cost types
  resDF$TotalCost <- NA # as we don't have a fleet, we can't provide this
  resDF$TotalCostPerVeh <- t.totalCost_Veh
  resDF$CostPerVehKM <- t.totalCost_Veh / t.totKM
  resDF$CostPerSeatKM <- resDF$CostPerVehKM / t.cap
  resDF$CostPerPassKM <- t.totalCost_Veh / t.passKM
  resDF$PricePerPassKM <- resDF$CostPerPassKM / (1-parameters[parameters$Name == "yield_on_sales",]$Value) * (1+parameters[parameters$Name == "vat",]$Value) * (1-parameters[parameters$Name == "payment_transaction_fee",]$Value)
  resDF$acquisition <- resDF$acquisition_var + resDF$acquisition_fix
  
  
  rm(list=ls(pattern="^t."))
  return(resDF)
}


#################################################################################################
# Switch between PT and fleet/private (non-PT)

scenarioAnalyzer <- function(scenario){
  if (scenario$VehicleType %in% ptCost$Type == FALSE) {
    return(nonptScenarioAnalyzer(t.scenario = scenario))
  } else {
    return(ptScenarioAnalyzer(scenario))
  }  
}

