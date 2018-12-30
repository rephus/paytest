package paytest.model

import java.util.UUID

import slick.driver.H2Driver.api._

case class Account(id: Option[String] = Some(UUID.randomUUID().toString), accountName: String, accountNumber: BigDecimal, numberCode: String,
  accountType: Int, address: String, bankId: Int, bankIdCode: String, name: String)

class Accounts(tag: Tag) extends Table[Account](tag, "account") {

  def id = column[String]("id", O.PrimaryKey)

  def accountName = column[String]("account_name")
  def accountNumber = column[BigDecimal]("account_number")
  def numberCode = column[String]("number_code")
  def accountType = column[Int]("account_type")
  def address = column[String]("address")
  def bankId = column[Int]("bank_id")
  def bankIdCode = column[String]("bank_id_code")
  def name = column[String]("name")

  def * = (id.?, accountName, accountNumber, numberCode, accountType, address, bankId, bankIdCode, name) <>(Account.tupled, Account.unapply)
}
