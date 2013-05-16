package controllers
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json

import models._
 
import play.api.Play.current
import com.typesafe.plugin._


object Application extends Controller {
 
  implicit val bookReads = Json.reads[Book]
  implicit val bookWrites = Json.writes[Book]

  val bookForm = Form(
    single(
      "name" -> nonEmptyText
    )
  )
 
  def index = Action {
    Ok(views.html.index(bookForm))
  }
 
  def addBook() = Action(parse.multipartFormData) { implicit request =>
    request.body.file("file").map { file =>
      import java.io.File
      val filename = file.filename 
      val contentType = file.contentType
      file.ref.moveTo(new File("/tmp/" + filename))
      bookForm.bindFromRequest.fold(
        errors => BadRequest(views.html.index(errors)),
        name => {
          Books.create(Book(None, name, filename))
          Redirect(routes.Application.index())
        }
      )
    }.getOrElse(Redirect(routes.Application.index).flashing("error" -> "Missing file"))
  }

  def listBooks() = Action {
    val books = Books.findAll()
    val booksJson = for ( book <- books) yield Json.toJson(book)
    val json = Json.toJson(booksJson)
    Ok(json).as(JSON)
  }
}
