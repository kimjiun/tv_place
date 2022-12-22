needs(dplyr)
needs(data.table)
needs(reshape2)
needs(recommenderlab)
needs(ggplot2)
needs(jsonlite)
needs(magrittr)
needs(RMySQL)
needs(DBI)

mySeq <- input[[1]]

#mydf <- fromJSON(input[[1]])
#class(mydf)


con <- dbConnect(MySQL(), host = "localhost", dbname = "tvinfo", user = "root", password = "bestfood", port = 3306)
dbListTables(con)

query <- ("SELECT member_seq, item_seq, rate FROM tvinfo_rate")
result <- dbGetQuery(conn = con, statement = query)
result

tvinfo_ratings <- acast(result, member_seq ~ item_seq, value.var = "rate")
tvinfo_ratings <- as.matrix(tvinfo_ratings)
tvinfo_ratings<-as(tvinfo_ratings, "realRatingMatrix")
tvinfo_ratings

which_set <- sample(x = 1:5, size = nrow(tvinfo_ratings), replace = TRUE)
for(i_model in 1:5){
  which_train <-which_set == i_model
  recc_tvinfo_test <- tvinfo_ratings[which_train, ]
  recc_tvinfo_train <- tvinfo_ratings[!which_train, ]
}

recc_model <- Recommender(data=recc_tvinfo_train, method = "UBCF")
n_recommended <- 10
recc_predicted <- predict(object = recc_model, newdata = recc_tvinfo_test, n = n_recommended)
recc_predicted
recc_predicted@items[1]

my_recc_predicted <- recc_predicted@items[[mySeq]]
my_recc_predicted

filePath <- paste("D:\\bestfood\\web\\rscript\\data\\user_based\\user",mySeq,".txt", sep="")

write.table(my_recc_predicted, file = filePath, sep=",", row.names = FALSE, col.names = FALSE, quote = FALSE)

#
#my_recc_predicted
#paste_recc <- paste(my_recc_predicted, collapse = ",")

#print(get('paste_recc'))
#print(get('my_recc_predicted'))
#print("end recommend system!!")
eval_sets <- evaluationScheme(data = recc_tvinfo_train,method = "cross-validation", train = 0.7, k = 10, goodRating = 3, given = 30)

recomm_eval <- Recommender(data = getData(eval_sets, "train"), method = "UBCF", parameter = NULL)

pred_eval <- predict(recomm_eval, newdata = getData(eval_sets, "known"), n = 10, type = "ratings")

accuracy_eval <- calcPredictionAccuracy(x = pred_eval, data = getData(eval_sets, "unknown"), byUser = TRUE)
print(colMeans(accuracy_eval))

#all_cons <- dbListConnections(MySQL())
#for (con in all_cons)
#  dbDisconnect(con)