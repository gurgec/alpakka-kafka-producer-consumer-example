package util

import model.Books.Book
import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue, JsonWriter, RootJsonFormat}

object JsonParser extends DefaultJsonProtocol {

  implicit object BookJsonFormat extends RootJsonFormat[Book] {
    def write(b: Book) = JsObject(
      "average_rating" -> JsString(b.averageRating),
      "bookID" -> JsString(b.bookID),
      "ratings_count" -> JsString(b.ratingsCount),
      "authors" -> JsString(b.authors),
      "text_reviews_count" -> JsString(b.textReviewsCount),
      "title" -> JsString(b.title),
      "# num_pages" -> JsString(b.numPages),
      "isbn13" -> JsString(b.isbn13),
      "language_code" -> JsString(b.languageCode),
      "isbn" -> JsString(b.isbn)
    )

    def read(value: JsValue): Book = {
      value.asJsObject.getFields("average_rating", "bookID", "ratings_count", "authors", "text_reviews_count", "title", "# num_pages", "isbn13", "language_code", "isbn") match {
        case Seq(JsString(averageRating), JsString(bookID), JsString(ratingsCount), JsString(authors), JsString(textReviewsCount), JsString(title), JsString(numPages), JsString(isbn13), JsString(languageCode), JsString(isbn)) =>
          Book(averageRating, bookID, ratingsCount, authors, textReviewsCount, title, numPages, isbn13, languageCode, isbn)
        case _ => throw DeserializationException("Book expected")
      }
    }
  }

  def toJson(map: Map[String, String])(
    implicit jsWriter: JsonWriter[Map[String, String]]): JsValue = jsWriter.write(map)

}
