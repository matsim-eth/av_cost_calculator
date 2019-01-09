#################################################################################################
# About this Code
#################################################################################################
# 
# The framework consists of three main scripts:
#   - This script (Main.R) reads the input from Input.xlsx on vehicle types and 
#     operational models. For each operational model, it creates four scenarios for the four 
#     different combinations of with/without automation and/or electrification. It passes the 
#     scenarios on to the script ScenarioAnalyzer.R for calculation of the cost structure.
#   - ScenarioAnalyzer.R takes the scenarios from Main.R, defines the different
#     response values (e.g. cost per vehicle kilometer, cost per passenger kilometer, ...).
#     It passes on the scenarios to CostCalculator.R for the calculation of the totals of each
#     cost component. It returns a data frame including the different response values for all 
#     scenarios.
#   - CostCalculator.R contains individual functions to calculate the various cost components.
#     It takes a scenario and returns a data frame object with one row. The columns are the
#     totals of each cost component for the given scenario.
# 
#################################################################################################



#################################################################################################
# Clear Workspace
rm(list = ls())



#################################################################################################
# Load/install packages

list.of.packages <- c("readxl", "stringr","ggplot2","gridExtra")

new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
if(length(new.packages)) install.packages(new.packages)
(lapply(list.of.packages, require, character.only = TRUE))



#################################################################################################
# Set working directory

setwd("P:/_TEMP/Becker_Henrik/_AV-cost_comparison/Original_Suisse/CostCalculatorExtern/")

#################################################################################################
# Load cost calculator scripts

source("CostCalculator.R")
source("ScenarioAnalyzer.R")



#################################################################################################
# Load Scenarios (from the Excel-tab "Realizations")

scen <- as.data.frame(read_excel("Input.xlsx","Realizations_ZH", col_names=TRUE))


# reformatting
scen <- scen[which(grepl("^d_", scen$Scenario)==FALSE),which(colnames(scen) != "")]
scen <- scen[which(scen$data == 1),]

scen <- scen[,scen[scen$Scenario == "VehicleType",] %in% c("VehicleType",vehicleCost$Type,ptCost$Type)]


scen <- t(scen)

colnames(scen) <- scen[1,]
scen <- as.data.frame(scen, stringsAsFactors=FALSE)
scen <- scen[row.names(scen) != "Scenario",]



#################################################################################################
# generate four scenarios
# with electrification / with vehicle automation / with both electrification and vehicle automation / none of the two

scen["electric"] <- 0
scen["automated"] <- 0

autom.scen <- scen
elect.scen <- scen
au.el.scen <- scen

autom.scen$automated <- 1
elect.scen$electric <- 1
au.el.scen$automated <- 1
au.el.scen$electric <- 1

row.names(autom.scen) <- paste("A", row.names(autom.scen), sep="-")
row.names(elect.scen) <- paste("E", row.names(elect.scen), sep="-")
row.names(au.el.scen) <- paste("AE", row.names(au.el.scen), sep="-")

scenarios <- rbind(scen, autom.scen, elect.scen, au.el.scen)
rm(scen, autom.scen, elect.scen, au.el.scen)

nonDouble <- c("VehicleType","electric","automated","fleetOperation", "Area")



#################################################################################################
# Recode input

scenarios$electric <- scenarios$electric == 1
scenarios$automated <- scenarios$automated == 1
scenarios$fleetOperation <- scenarios$fleetOperation == 1

scenarios$ph_avOccupancy <- as.double(as.character(scenarios$ph_avOccupancy)) / 100
scenarios$oph_avOccupancy <- as.double(as.character(scenarios$oph_avOccupancy)) / 100
scenarios$ngt_avOccupancy <- as.double(as.character(scenarios$ngt_avOccupancy)) / 100

scenarios$ph_relActiveTime <- as.double(as.character(scenarios$ph_relActiveTime)) / 100
scenarios$oph_relActiveTime <- as.double(as.character(scenarios$oph_relActiveTime)) / 100
scenarios$ngt_relActiveTime <- as.double(as.character(scenarios$ngt_relActiveTime)) / 100

scenarios$ph_relEmptyRides <- as.double(as.character(scenarios$ph_relEmptyRides)) / 100
scenarios$oph_relEmptyRides <- as.double(as.character(scenarios$oph_relEmptyRides)) / 100
scenarios$ngt_relEmptyRides <- as.double(as.character(scenarios$ngt_relEmptyRides)) / 100

scenarios$ph_relMaintenanceRides <- as.double(as.character(scenarios$ph_relMaintenanceRides)) / 100
scenarios$oph_relMaintenanceRides <- as.double(as.character(scenarios$oph_relMaintenanceRides)) / 100
scenarios$ngt_relMaintenanceRides <- as.double(as.character(scenarios$ngt_relMaintenanceRides)) / 100

scenarios$ph_relMaintenanceHours <- as.double(as.character(scenarios$ph_relMaintenanceHours)) / 100
scenarios$oph_relMaintenanceHours <- as.double(as.character(scenarios$oph_relMaintenanceHours)) / 100
scenarios$ngt_relMaintenanceHours <- as.double(as.character(scenarios$ngt_relMaintenanceHours)) / 100



#################################################################################################
# main analysis

# Define results data.frame

full.resDF <- createResDF(0)

# calculate results using ScenarioAnalyzer.R

for (i in 1:dim(scenarios)[1]){
  
  t.resDF.scen <- scenarioAnalyzer(scenario = scenarios[i,])
  row.names(t.resDF.scen) <- row.names(scenarios[i,])
  
  full.resDF <- rbind(full.resDF, t.resDF.scen)
  rm(t.resDF.scen)
}

# please ignore warning message "NAs introduced by coercion" as this is intended.

# The fixed and variable components of acquisition and interest are summarized in new variables.
# To avoid confusion, the original components are deleted
full.resDF$acquisition_var <- full.resDF$acquisition_fix <- full.resDF$interest_var <- full.resDF$interest_fix <- NULL

# write results to file 
full.resDF <- cbind(Scenario=row.names(full.resDF),full.resDF)
write.table(full.resDF, "results_main.csv", sep=";", row.names=FALSE)


#################################################################################################
# subsequent analyses

# recoding required for the following modules
resDF <- full.resDF
rm(full.resDF)



