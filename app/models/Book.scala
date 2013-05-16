package models
 
import play.api.db._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._ 
import play.api.libs.json.Json

import java.io._
import org.apache.pdfbox.pdmodel._
import org.apache.pdfbox.util._
 
case class Book(id: Option[Int], name: String, filename: String)

object Books extends Table[Book]("books") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def filename = column[String]("filename")

    def * = id.? ~ name ~ filename <> (Book, Book.unapply _)
    def forInsert = name ~ filename <> (
        { t => Book(None, t._1, t._2) },
        { (d: Book) => Some((d.name, d.filename)) })
 
  lazy val database = Database.forDataSource(DB.getDataSource())

 
  def findAll(): Seq[Book] = {
    database withSession { implicit session : Session =>
      Query(Books).list
    }
  }
 
  def create(book: Book): Unit = {
    database withSession { implicit session : Session =>
      Books.forInsert.insert(book)
    }
    // create the text file
    pdfToText(book)
  }

  def delete(book: Book): Unit = {
    database withSession { implicit session : Session =>
      val b = Query(Books).filter(_.id === book.id)
      b.delete
    }
  }
  
  def pdfToText(book: Book): Unit = {
    //val userHome = System.getProperty("user.home")
    val input = new File("/tmp/" + book.filename)
    val output = new File("/tmp/" + book.filename + ".txt")
    val pd = PDDocument.load(input)
    val stripper = new PDFTextStripper()
    val wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)))
    stripper.writeText(pd, wr)
    if (pd != null) {
       pd.close();
    }
    wr.close();
  }
}
