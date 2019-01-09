#################################################################################################
# About this Code
#################################################################################################
# 
# This script assumes the general cost structures from the given scenario and analyses the cost 
# per passenger km for a varying number of passengers departing at the same time in the same
# direction. The idea is to define demand thresholds for the various vehicle types.
# 
#################################################################################################



#################################################################################################
# Restrict the scenarios to be analyzed

occupancy.scenarios <- scenarios[scenarios$Area == "Urban",]



#################################################################################################
# Define different occupancy levels

# import the individual vehicle capacities from the Excel file
t.cst <- rbind( subset(ptCost, select = c("Type", "Capacity")), subset(vehicleCost, select = c("Type", "Capacity")) )
occupancy.scenarios["vehicleCapacity"] <- NA
for (i in 1:dim(occupancy.scenarios)[1]){ occupancy.scenarios$vehicleCapacity[i] <- t.cst[which(t.cst == occupancy.scenarios$VehicleType[i]),]$Capacity }
rm(t.cst)

occupancy.scenarios.full <- occupancy.scenarios
row.names(occupancy.scenarios.full) <- paste(row.names(occupancy.scenarios), 1, sep="_")

# define the range for the number of passengers to be studied
num.pass <- 1:75

# create one new scenario per combination of input scenario, number of passengers and technology (i.e. autonomous, electric, both or none)
for (i in 1:(length(num.pass)-1)+1){ 
  t.occupancy.scenarios <- occupancy.scenarios
  row.names(t.occupancy.scenarios) <- paste(row.names(occupancy.scenarios), i, sep="_")
  occupancy.scenarios.full <- rbind(occupancy.scenarios.full, t.occupancy.scenarios) 
  rm(t.occupancy.scenarios)
}

# add number of passengers and occupancy values for each scenario
occupancy.scenarios.full["numPass"] <- rep(num.pass, each = dim(occupancy.scenarios)[1])
occupancy.scenarios.full["numveh"] <- ceiling(occupancy.scenarios.full$numPass / occupancy.scenarios.full$vehicleCapacity)
occupancy.scenarios.full["av_occupancy"] <- occupancy.scenarios.full$numPass / occupancy.scenarios.full$numveh / occupancy.scenarios.full$vehicleCapacity

# given that only a generic origin-destination relation is covered, there is no temporal differentiation
occupancy.scenarios.full$ph_avOccupancy <- occupancy.scenarios.full$oph_avOccupancy <- occupancy.scenarios.full$ngt_avOccupancy <- occupancy.scenarios.full$av_occupancy



#################################################################################################
# Analyze different occupancy levels


# Define results data.frame

occupancy.resDF <- createResDF(0)


# calculate results using ScenarioCalculator.R

for (i in 1:dim(occupancy.scenarios.full)[1]){
  
  t.resDF.scen <- scenarioAnalyzer(occupancy.scenarios.full[i,])
  row.names(t.resDF.scen) <- row.names(occupancy.scenarios.full[i,])
  
  occupancy.resDF <- rbind(occupancy.resDF, t.resDF.scen)
  rm(t.resDF.scen)
}

# compile results
occupancy.resDF$acquisition <- occupancy.resDF$acquisition_var + occupancy.resDF$acquisition_fix
occupancy.resDF$acquisition_var <- occupancy.resDF$acquisition_fix <- NULL

occupancy.resDF["scen"] <- row.names(occupancy.resDF)
occupancy.resDF$scen <- str_split_fixed(occupancy.resDF$scen, "_", 2)[,1]

occupancy.resDF["RN"] <- row.names(occupancy.resDF)
occupancy.scenarios.full["RN"] <- row.names(occupancy.scenarios.full)
occupancy.resDF <- merge(occupancy.resDF, subset(occupancy.scenarios.full, select = c( "RN", "numPass", "numveh", "av_occupancy", "electric", "automated", "VehicleType" )), by="RN")



#################################################################################################
# Plot

size=22
text <- element_text(size=size)

p1 <- ggplot(data =
               occupancy.resDF[!occupancy.resDF$electric & !occupancy.resDF$automated,]) +
  geom_line(aes(x=numPass, y=PricePerPassKM, group=VehicleType, color=VehicleType), size = 1) +
  scale_x_continuous(name="Number of passengers") +
  scale_y_continuous(name="Price per passenger km [CHF]") +
  coord_cartesian(xlim=c(0,75), ylim=c(0,1.5)) +
  ggtitle("Conventional") +
  theme(axis.text= text,axis.title.x = text,axis.title.y = text,legend.text = text,legend.title = text,strip.text = text,
        panel.grid.major = element_blank(),
        panel.grid.minor = element_blank(),
        panel.border = element_blank(),
        panel.background = element_blank())  

p2 <- ggplot(data =
               occupancy.resDF[occupancy.resDF$electric & occupancy.resDF$automated,]) +
  geom_line(aes(x=numPass, y=PricePerPassKM, group=VehicleType, color=VehicleType), size = 1) +
  scale_x_continuous(name="Number of passengers") +
  scale_y_continuous(name="Price per passenger km [CHF]") +
  coord_cartesian(xlim=c(0,75), ylim=c(0,1.5)) +
  ggtitle("Autonomous-Electric") +
  theme(axis.text= text,axis.title.x = text,axis.title.y = text,legend.text = text,legend.title = text,strip.text = text,
        panel.grid.major = element_blank(),
        panel.grid.minor = element_blank(),
        panel.border = element_blank(),
        panel.background = element_blank())  

pdf("Plots/SensitivityAnalysis.pdf", width=16, height=6)
grid.arrange(p1, p2, ncol=2, nrow=1)
dev.off()



#################################################################################################
# Write results to file

write.table(occupancy.resDF, "results_occupancy.csv", sep=";", row.names=FALSE)