package model

object Books {
   case class Book(averageRating: String,
                    bookID: String,
                    ratingsCount: String,
                    authors: String,
                    textReviewsCount: String,
                    title: String,
                    numPages: String,
                    isbn13: String,
                    languageCode: String,
                    isbn: String)

}
