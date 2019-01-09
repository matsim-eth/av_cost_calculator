#################################################################################################
# Reproduce the plots from the paper
# Please run Main.R first
# -- Use Realizations_ZH instead of Realizations right below the comment Load Scenarios in Main.R

setwd("P:/_TEMP/Becker_Henrik/_AV-cost_comparison/Original_Suisse/CostCalculatorExtern/")


list.of.packages <- c("reshape","ggplot2","plyr","RColorBrewer","xtable")

new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
if(length(new.packages)) install.packages(new.packages)
(lapply(list.of.packages, require, character.only = TRUE))


#data for plots
scenariosplots <- cbind(ID=rownames(scenarios),scenarios[,c("Area","VehicleType")])
resDFplots <- resDF
colnames(resDFplots)[1] <- "ID"
dataplots <- merge(x = scenariosplots,y=resDFplots,by = "ID")

#Intended for cost structures that are independent of the region
#replicate for every available region in the given data
stardata<-dataplots[dataplots$Area=="*",]
dataplots<- dataplots[dataplots$Area!="*",]

totaldata <- as.data.frame(matrix(data=NA,nrow=0,ncol=ncol(stardata)))
for(area in unique(dataplots$Area)){
  stardata$Area <- area
  totaldata<-rbind(totaldata,stardata)
} 

#Due to the current set up of the cost calculator, only the costs of private operations are independent of the region
totaldata$Operation <- "Private"
dataplots$Operation <- "PT"
dataplots <- rbind(dataplots, totaldata)

#Add the columns Steering and Propulsion based on IDs
dataplots$Steering <- NA
dataplots$Steering[grepl("A",dataplots$ID)] <-"Autonomous"
dataplots$Steering[!grepl("A",dataplots$ID)] <-"Not autonomous"

dataplots$Propulsion[grepl("E",dataplots$ID)] <-"Electric"
dataplots$Propulsion[!grepl("E",dataplots$ID)] <-"Not electric"


dataplots$VehicleType[dataplots$VehicleType=="CityBus"]<-"Bus"
dataplots$VehicleType[dataplots$VehicleType=="RegBus"]<-"Bus"

dataplots[dataplots$VehicleType%in%c("Bus","Rail"),"CostPerVehKM"] <- dataplots[dataplots$VehicleType%in%c("Bus","Rail"),"TotalCost"]

simpleCap <- function(x) {
  s <- strsplit(x, " ")[[1]]
  paste(toupper(substring(s, 1,1)), substring(s, 2),
        sep="", collapse=" ")
}

colnames(dataplots)<-sapply(colnames(dataplots), simpleCap)

#Other_fix currently encompasses only tolls
colnames(dataplots)[colnames(dataplots)=="Other_fix"] <- "Tolls"
colnames(dataplots)[colnames(dataplots)=="Acquisition"] <- "Depreciation over Time"
colnames(dataplots)[colnames(dataplots)=="Deprecation"] <- "Depreciation per Kilometre"



dataplots[,"Maintenance and Wear"] <- dataplots$Maintenance + dataplots$Tires
dataplots$Maintenance <- NULL
dataplots$Tires <- NULL

#both are calculated per kilometre
dataplots[,"Depreciation"] <- dataplots$`Depreciation over Time`+dataplots$`Depreciation per Kilometre`
dataplots$`Depreciation over Time` <-  NULL
dataplots$`Depreciation per Kilometre` <- NULL


dataplots[,"Parking and Tolls"] <- dataplots$Parking + dataplots$Tolls
dataplots$Parking <- NULL
dataplots$Tolls <- NULL

dataplots$OperationDetailed<-dataplots$Operation
dataplots$OperationDetailed[dataplots$OperationDetailed=="PT" & dataplots$VehicleType=="Midsize" & grepl(pattern = "Taxi",x = dataplots$ID) ] <- "PT-NP"
dataplots$OperationDetailed[dataplots$OperationDetailed=="PT" & dataplots$VehicleType=="Solo" ] <- "PT-NP"
dataplots$OperationDetailed[dataplots$OperationDetailed=="PT"] <- "PT-P"

#--------------------------Table Appendix ------------------------------------

colsfortex <- c("VehicleType","Area","OperationDetailed","Steering","Propulsion","CostPerVehKM","CostPerSeatKM","CostPerPassKM")



tex<-dataplots[dataplots$Area!="Interregional",colsfortex]

colnames(tex)[3] <- "Operation"


tex<-tex[order(tex$VehicleType,tex$Area,tex$Operation,tex$Propulsion),]

tex[,c("CostPerVehKM","CostPerSeatKM","CostPerPassKM")] <- round(tex[,c("CostPerVehKM","CostPerSeatKM","CostPerPassKM")],2)


tex[is.na(tex)]<- "-"

tex$Steering[tex$Steering=="Autonomous"] <- "Aut"
tex$Steering[tex$Steering=="Not autonomous"] <- "N. aut"

tex$Propulsion[tex$Propulsion=="Electric"] <- "Elec"
tex$Propulsion[tex$Propulsion=="Not electric"] <- "N. elec"


tex$Operation[tex$Operation=="Private"] <- "Priv"
tex$Area[tex$Area=="Regional"] <- "Reg"
tex$Area[tex$Area=="Urban"] <- "Urb"

tex$Area[tex$Operation=="Priv" & tex$Area=="Reg"] <- "*"
tex <- tex[!(tex$Operation=="Priv" & tex$Area=="Urb"),]


colnames(tex)[6:8] <- paste(c("CostVehKM //linebreak in CHF","CostSeatKM //linebreak in CHF","CostPassKM //linebreak in CHF"))


																					  

sink("tables/appendixcosts.tex")
print(xtable(tex),include.rownames = F,hline.after = NULL,only.contents = T,include.colnames = F)
sink()

#--------------------------Plot preparations ---------------------------------

size=22
text <- element_text(size=size,colour = "black",family="Times")

#costs depending on distance
																																  
										 
						 


#--------------------Figure 1: Cost structure comparison with (Autonomous) and without (Conv) vehicle automation for private vehicles (Private Car) and taxi fleet vehicles without pooling (Ind. Taxi).----------------------------

cond <-  dataplots$VehicleType%in%c("Midsize") & dataplots$Area =="Urban" & dataplots$Propulsion=="Not electric" & (dataplots$Operation=="Private"   | dataplots$OperationDetailed=="PT-NP" )
currdata<-dataplots[cond,]


													
																					
													
																									  
																		  
colnames(currdata)[which(colnames(currdata)=="Overhead")]<-"Overhead and Vehicle Operations"

#Cost summaries not needed
#currdata<-melt(currdata[,c(1,4,5,6,7,8,9,10,19)*-1], id.vars=c("Area", "VehicleType", "OperationDetailed", "Propulsion","Steering"))

currdata<-melt(currdata[,!(colnames(currdata)%in%c("ID","TotalCost","TotalCostPerVeh","CostPerVehKM","CostPerSeatKM","CostPerPassKM","PricePerPassKM","Operation"))], id.vars=c("Area", "VehicleType", "OperationDetailed", "Propulsion","Steering"))

currdata <- currdata[currdata$value!=0,]



currdata$variable <- factor(currdata$variable,levels=c("Overhead and Vehicle Operations","Salaries","Fuel","Cleaning","Parking and Tolls","Tax","Insurance","Depreciation", "Interest", "Maintenance and Wear"))

#Renaming the categories
currdata$NewCategory[currdata$Steering=="Autonomous" & currdata$OperationDetailed=="PT-NP"] <- "Ind. Taxi \n Autonomous"
currdata$NewCategory[currdata$Steering=="Autonomous" & currdata$OperationDetailed=="Private"] <- "Private Car \n Autonomous"
currdata$NewCategory[currdata$Steering!="Autonomous"& currdata$OperationDetailed=="Private" ] <- "Private Car \n Conv"
currdata$NewCategory[currdata$Steering!="Autonomous"& currdata$OperationDetailed=="PT-NP" ] <- "Ind. Taxi \n Conv"


currdata$NewCategory <- factor(currdata$NewCategory,levels=c("Private Car \n Conv","Private Car \n Autonomous","Ind. Taxi \n Conv","Ind. Taxi \n Autonomous"))


#Calculate percentage value for each transport alternative
currdata$Percentage <- NA
for(cat in unique(currdata$NewCategory)){
  cond1 <- currdata$NewCategory==cat
  currdata$Percentage[cond1] <- currdata$value[cond1]/sum(currdata$value[cond1])
  print(sum(currdata$value[cond1]))
}
currdata$label <- paste0(round(currdata$Percentage,2)*100,"%")
currdata$label[currdata$Percentage<0.05] <- " "

set.seed(42)


size=19
text1 <- text
text1$size=size

p <-   ggplot(currdata[order(currdata$variable, decreasing=TRUE), ], aes(x = NewCategory, y = Percentage, fill=variable, label=label)) + 
  geom_bar(stat = "identity") +  
  geom_text(size = size/3, position = position_stack(), vjust=2,family=text$family) + 
  ylab("Share of cost per passenger km") + 
  scale_y_continuous(breaks=c(0,0.25,0.5,0.75,1),labels=c("0%","25%","50%","75%","100%"))+
  xlab("Type of Car")+     
  scale_fill_manual(values=c("#f9743b", "#ffa782", "#fc28ea", "#fcaef5", "#ffe8fd", "#47e05c", "#9ddba6", "#0a90ff", "#77c2ff", "#d8edff")) +
  labs(fill="Cost Type") +
  theme(axis.text.x= text1,axis.text.y= text1,axis.title.x = text1,axis.title.y = text1,legend.text = text1,legend.title = text1,
        panel.grid.major = element_blank(),
        panel.grid.minor = element_blank(),
        panel.border = element_blank(),
        panel.background = element_blank()) 

  

#[sample(x = c(1:10),size = 10,replace = F)]



ggsave("Plots/Stacked-Midsize-PEvsPAVEvsSAVE.png",plot = p,width = 12,height = 8,device = "png")

#------------- Generate table 3: Cost structure comparison with (Autonomous) and without (Conv) vehicle automation for private vehicles (Private Car) and taxi fleet vehicles without pooling (Ind. Taxi). ------------------------------------

tex <- currdata

tex <- currdata[,c("NewCategory","variable","value","Percentage")]

tex <- tex[order(tex$NewCategory,tex$variable),]

tex$value <- round(tex$value,3)
tex$Percentage <- round(tex$Percentage,3)

colnames(tex) <- c("Mode","Cost Type","CostPassKM in CHF","Share of CostPassKM in CHF")
tex$Mode<-gsub(x = tex$Mode,pattern = "\\n ",replacement = "")

levels(tex$`Cost Type`)<-c(levels(tex$`Cost Type`),"Sum")
#transform to structure of table 3
for(cat in unique(tex$Mode)){
  if(cat!=unique(tex$Mode)[4]){
    tex[(max(which(tex$Mode==cat))+2):(nrow(tex)+1),]<-tex[(max(which(tex$Mode==cat))+1):nrow(tex),]
    tex[(max(which(tex$Mode==cat))+1),]<-c(cat,"Sum",sum(as.numeric(tex[tex$Mode==cat,]$`CostPassKM in CHF`)),1)
  }else{
    tex <- rbind(tex,c(cat,"Sum",sum(as.numeric(tex[tex$Mode==cat,]$`CostPassKM in CHF`)),1))
  }
  
}

tex1 <- as.data.frame(matrix(ncol=1,nrow=11))
tex1[,1] <- c(levels(currdata$variable),"Sum")
colnames(tex1) <- c("Cost Type")
starttex1 <- tex1
for(cat in unique(tex$Mode)){
  temp <- merge(x = starttex1,y = tex[tex$Mode==cat,-1],by = "Cost Type",sort = F,all.x = T)
  temp <- temp[match(levels(tex$`Cost Type`),temp$`Cost Type`),]
  tex1<- cbind(tex1,temp[,c(2:3)])
}
colnames(tex1) <- c("Cost Type",rep(c("CPKM in CHF","Share of CPKM"),4))
																					   

sink("tables/coststructures.tex")
print(xtable(tex1),include.rownames = F,hline.after = NULL,only.contents = T,include.colnames = F)
sink()


#--------------------------Plot 2: Cost comparison of different modes with and without autonomous vehicle technology.---------------------------------------

cond <-  dataplots$VehicleType%in%c("Midsize","Bus","Rail") & dataplots$Propulsion=="Not electric" & dataplots$Area%in%c("Urban","Regional")

currdata<-dataplots[cond,]

#tranforming to factors helps with ggplot
currdata$Steering<-factor(currdata$Steering,levels=c("Not autonomous","Autonomous"))

currdata$Area<-factor(currdata$Area,levels=c("Urban","Regional"))

#In the Zurich case, a midsized taxi is not available (only as a ridesharing option)
#The occupancy is therefore adjusted from 2.6 to 1.4
#As the cleaning costs depend on the number of passenger trips, they are no adjusted

																			   
									  

													  

																		  

									

#Renaming the categories for the plot

currdata$Newcategory <- currdata$Operation

currdata$Newcategory[currdata$OperationDetailed=="Private"] <- "Private Car"
currdata$Newcategory[currdata$OperationDetailed=="PT-NP" & currdata$VehicleType=="Midsize"] <- "Ind. Taxi"

currdata$Newcategory[currdata$OperationDetailed=="PT-P" & currdata$VehicleType=="Midsize"]<-"Pooled Taxi"

currdata$Newcategory[currdata$VehicleType%in%c("Bus","Rail")]<-currdata$VehicleType[currdata$VehicleType%in%c("Bus","Rail")]

currdata$Newcategory<-factor(currdata$Newcategory,levels=c("Private Car","Pooled Taxi","Ind. Taxi","Bus","Rail"))
currdata$CostPerPassKM <- round(currdata$CostPerPassKM,2)


currdata$labelCostNotAV <- 0
currdata$labelCostNotAV[currdata$Steering == "Not autonomous"] <- currdata[currdata$Steering == "Not autonomous",]$CostPerPassKM
 
currdata[currdata$Steering == "Not autonomous",]$CostPerPassKM <- -currdata[currdata$Steering == "Not autonomous",]$CostPerPassKM


p2 <- 
  ggplot(currdata, aes(x = Newcategory, group=Steering, fill=Steering, y = CostPerPassKM)) + 
  geom_bar(data = currdata[currdata$Steering == "Autonomous",], aes(x = Newcategory, group=Steering, fill=Steering, y = CostPerPassKM), stat="identity", width = 0.75, position = "dodge") +
  geom_bar(data = currdata[currdata$Steering == "Not autonomous",], aes(x = Newcategory, group=Steering, fill=Steering, y = CostPerPassKM), stat="identity", width = 0.75, position = "dodge") +
  scale_fill_manual(values=c( "#9f9b9b","#454444")) + 
  ylab("CHF per passenger kilometer") +
  xlab("") +
  labs(colour = "Steering") +
  facet_wrap(~Area,nrow=1, ncol=2) + 

  geom_text(data = currdata[currdata$Steering == "Autonomous",], aes(label=CostPerPassKM), position=position_dodge(width=0.5), size=size/2.5, hjust = -0.3,family=text$family) +
  geom_text(data = currdata[currdata$Steering == "Not autonomous",], aes(label=labelCostNotAV), position=position_dodge(width=0.5), size=size/2.5, hjust = +1.2,family=text$family) + scale_y_continuous(breaks=c(-2,0,2), labels = c("2", "0", "2"),limits = c(-3.5,3.5))+  coord_flip() +
  theme(axis.text.x= text,axis.text.y= text,axis.title.x = text,axis.title.y = text,legend.text = text,legend.title = text,strip.text = text,
        panel.grid.major = element_blank(),
        panel.grid.minor = element_blank(),
        panel.border = element_blank(),
        panel.background = element_blank()) 






#Adjust the given path:

ggsave("Plots/NotAVvsAV2.png",plot = p2,width=16,height=6,device = "png")



currdata[currdata$Steering == "Not autonomous",]$CostPerPassKM <- -currdata[currdata$Steering == "Not autonomous",]$CostPerPassKM



#----------------Figure 3: Future competitive situation with autonomous vehicle technology in an urban and a regional setting. a)--------

cond <-   dataplots$Propulsion=="Electric" & dataplots$Steering =="Autonomous" & !(dataplots$Operation=="Private" & dataplots$VehicleType!="Midsize") & !(dataplots$VehicleType%in%c("Van","Minibus","Rail")) & !(dataplots$OperationDetailed=="PT-P" & dataplots$VehicleType=="Midsize")
currdata <-dataplots[cond,]

#Rename categories
currdata$Newcategory <- currdata$VehicleType
currdata$Newcategory[currdata$VehicleType=="Midsize"&currdata$Operation=="Private"]<-"Private aCar"
currdata$Newcategory[currdata$VehicleType=="Solo"] <- "Shared \n aSolo"
currdata$Newcategory[currdata$VehicleType=="Midsize" &currdata$OperationDetailed=="PT-NP"] <- "aTaxi"
currdata$Newcategory[currdata$VehicleType=="Minibus"] <- "aMinibus"
currdata$Newcategory[currdata$VehicleType=="Bus"] <- "aCityBus"


currdata$Newcategory<-factor(currdata$Newcategory,levels=c("Private aCar","Shared \n aSolo","aTaxi","aMinibus","aCityBus"))#,"Bus","Rail"))

currdata$Area<-factor(currdata$Area,levels=c("Urban","Regional","Interregional"))


																	
									  
																	  
																							  


currdata[,"Cost_Type"] <- NA
currdata[currdata$Newcategory!="Private aCar","Cost_Type"] <- "Price"

add <- currdata[currdata$Newcategory=="Private aCar",]
add[,"Cost_Type"]<-"Fixed Costs"

currdata[currdata$Newcategory=="Private aCar","Cost_Type"] <-"Variable Costs"

currdata <- rbind(currdata,add)

currdata$Costs = 0 
currdata$Costs[currdata$Newcategory!="Private aCar"] <- currdata$PricePerPassKM[currdata$Newcategory!="Private aCar"]


cond1 <- currdata$Newcategory=="Private aCar"&currdata$Cost_Type=="Variable Costs"
currdata$Costs[cond1] <- round(currdata$Cleaning[cond1] + currdata$Fuel[cond1] + currdata$`Maintenance and Wear`[cond1]+currdata$`Parking and Tolls`[cond1],2)
cond2 <- currdata$Newcategory=="Private aCar"&currdata$Cost_Type=="Fixed Costs"
currdata$Costs[cond2] <- round(currdata$Depreciation[cond2] + currdata$Interest[cond2] + currdata$Insurance[cond2] + currdata$Tax[cond2],2)

currdata$Cost_Type <- factor(currdata$Cost_Type,levels=c("Fixed Costs","Variable Costs","Price"))
																								 
currdata$Costs<-round(currdata$Costs,2)




regdata <- currdata[currdata$Area=="Urban",]



totals <- regdata[,c("Newcategory","Costs")]
totals[which(totals$Newcategory=="Private aCar")[2],"Costs"]<- sum(totals$Costs[totals$Newcategory=="Private aCar"])

colnames(totals)<-c("Newcategory","totals1")

p <- ggplot(data=regdata)+ aes(x = Newcategory, y = Costs,fill=Cost_Type) + 
  geom_bar(stat = "identity", width = 0.5) + 
  ylab("CHF per passenger km ") + 
  xlab("")  + 
  scale_fill_manual(values=c("#cccccc","#454545",  "#7f7f7f"), name="") +
  theme(axis.text.x= text,axis.text.y= text,axis.title.x = text,axis.title.y = text,legend.text = text,legend.title = text,strip.text = text,
        #axis.line = element_line(colour = "black"),
        panel.grid.major = element_blank(),
        panel.grid.minor = element_blank(),
        panel.border = element_blank(),
        panel.background = element_blank()
        ) +  geom_text(aes(x = Newcategory,y = totals1,label=totals1,fill=NULL,family=text$family),data=totals, vjust=-1,size=size/2.5)+ ylim(0, max(totals$totals1)*1.2)
  


																					  


ggsave("Plots/AVE-models_fix_var_urban.png",width=12,height=6,plot = p,device = "png")
#----------------Figure 3: Future competitive situation with autonomous vehicle technology in an urban and a regional setting. b)--------
#Now cut out data for regional setting

regdata <- currdata[currdata$Area=="Regional",]
regdata$Newcategory <- as.character(regdata$Newcategory)

regdata$Newcategory[regdata$Newcategory=="aCityBus"] <- "aRegBus"

regdata$Newcategory<-factor(regdata$Newcategory,levels=c("Private aCar","Shared \n aSolo","aTaxi","aRegBus"))#,"Bus","Rail"))


totals <- regdata[,c("Newcategory","Costs")]
totals[which(totals$Newcategory=="Private aCar")[2],"Costs"]<- sum(totals$Costs[totals$Newcategory=="Private aCar"])

colnames(totals)<-c("Newcategory","totals1")


p <- ggplot(data=regdata)+ aes(x = Newcategory, y = Costs,fill=Cost_Type) + 
  geom_bar(stat = "identity", width = 0.5) + 
  ylab("CHF per passenger km ") + 
  xlab("")  + 
  scale_fill_manual(values=c("#cccccc","#454545",  "#7f7f7f"), name="") +
  theme(axis.text.x= text,axis.text.y= text,axis.title.x = text,axis.title.y = text,legend.text = text,legend.title = text,strip.text = text,
        panel.grid.major = element_blank(),
        panel.grid.minor = element_blank(),
        panel.border = element_blank(),
        panel.background = element_blank())  + geom_text(aes(x = Newcategory,y = totals1,label=totals1,fill=NULL,family=text$family),data=totals, vjust=-1,size=size/2.5)+ ylim(0, max(totals$totals1)*1.2)



ggsave("Plots/AVE-models_fix_var_regional.png",width=12,height=6,plot = p,device = "png")



